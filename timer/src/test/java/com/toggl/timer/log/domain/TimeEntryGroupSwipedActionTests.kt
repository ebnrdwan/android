package com.toggl.timer.log.domain

import com.toggl.models.common.SwipeDirection
import com.toggl.repository.interfaces.StartTimeEntryResult
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.toSettableValue
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec
import io.mockk.coEvery
import io.mockk.mockk

class TimeEntryGroupSwipedActionTests : FreeSpec({

    val repository = mockk<TimeEntryRepository>()
    val entryInDatabase = createTimeEntry(1, "test")
    val entryToBeStarted = createTimeEntry(2, "test")
    coEvery { repository.startTimeEntry(1, "test") } returns StartTimeEntryResult(entryToBeStarted, null)
    val reducer = TimeEntriesLogReducer(repository)

    "The TimeEntryGroupSwiped action" - {
        "should throw when there are no time entries" - {
            "with the matching ids" {
                val initialState = createInitialState(listOf(entryInDatabase))
                var state = initialState
                val settableValue = state.toSettableValue { state = it }

                shouldThrow<IllegalStateException> {
                    reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryGroupSwiped(listOf(2, 3), SwipeDirection.Left))
                }
            }

            "at all" {
                val initialState = createInitialState()

                assertAll(fn = { id: Long ->
                    var state = initialState
                    val settableValue = state.toSettableValue { state = it }
                    shouldThrow<IllegalStateException> {
                        reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryGroupSwiped(listOf(id), SwipeDirection.Left))
                    }
                })
            }
        }

        "when swiping right" - {
            "should continue the swiped time entries" {
                val initialState = createInitialState(listOf(entryInDatabase))
                var state = initialState
                val settableValue = state.toSettableValue { state = it }
                val action = TimeEntriesLogAction.TimeEntryGroupSwiped(listOf(1, 2), SwipeDirection.Right)
                val effects = reducer.reduce(settableValue, action)
                val startedTimeEntry = (effects.single().execute() as TimeEntriesLogAction.TimeEntryStarted).startedTimeEntry
                startedTimeEntry shouldBe entryToBeStarted
            }
        }

        "when swiping left" - {
            "should delete the swiped time entries" {
                val timeEntries = (1L..10L).map { createTimeEntry(it, "testing") }
                val timeEntriesToDelete = timeEntries.take(4)
                coEvery { repository.deleteTimeEntries(timeEntriesToDelete) } returns timeEntriesToDelete.map { it.copy(isDeleted = true) }.toHashSet()
                val initialState = createInitialState(timeEntries)
                var state = initialState
                val settableValue = state.toSettableValue { state = it }
                val action = TimeEntriesLogAction.TimeEntryGroupSwiped(timeEntriesToDelete.map { it.id }, SwipeDirection.Left)
                val effectAction = reducer.reduce(settableValue, action).single().execute() as TimeEntriesLogAction.TimeEntriesDeleted
                val deletedTimeEntries = effectAction.deletedTimeEntries
                action.ids shouldContainAll deletedTimeEntries.map { it.id }
            }
        }
    }
})