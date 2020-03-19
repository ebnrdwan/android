package com.toggl.timer.log.domain

import com.toggl.architecture.extensions.noEffect
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.toSettableValue
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.mockk

class TimeEntriesDeletedActionTests : FreeSpec({

    val repository = mockk<TimeEntryRepository>()
    val entriesInDatabase = (1L..10L).map { createTimeEntry(it, "test") }
    val reducer = TimeEntriesLogReducer(repository)

    "The TimeEntriesDeleted action updates the state to remove" - {
        "a single time entry from the time entry list" - {
            var state = createInitialState(entriesInDatabase)
            val settableValue = state.toSettableValue { state = it }

            val timeEntry = entriesInDatabase.first()
            val action = TimeEntriesLogAction.TimeEntriesDeleted(hashSetOf(timeEntry))
            val effect = reducer.reduce(settableValue, action)
            effect shouldBe noEffect()
            state.timeEntries[timeEntry.id] shouldBe null
        }

        "multiple time entries from the time entry list" - {
            var state = createInitialState(entriesInDatabase)
            val settableValue = state.toSettableValue { state = it }

            val timeEntriesToRemove = entriesInDatabase.take(3)
            val action = TimeEntriesLogAction.TimeEntriesDeleted(timeEntriesToRemove.toHashSet())
            val effect = reducer.reduce(settableValue, action)
            effect shouldBe noEffect()

            timeEntriesToRemove.forEach {
                state.timeEntries[it.id] shouldBe null
            }
        }

        "nothing deleted time entry wasn't there in the first place" - {
            var state = createInitialState(entriesInDatabase)
            val settableValue = state.toSettableValue { state = it }

            val nonExistingTimeEntry = createTimeEntry(321, "I don't exist")
            val action = TimeEntriesLogAction.TimeEntriesDeleted(hashSetOf(nonExistingTimeEntry))
            val effect = reducer.reduce(settableValue, action)
            effect shouldBe noEffect()
            state.timeEntries shouldBe entriesInDatabase.associateBy { it.id }
        }
    }
})