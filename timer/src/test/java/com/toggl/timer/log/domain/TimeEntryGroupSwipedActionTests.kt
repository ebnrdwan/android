package com.toggl.timer.log.domain

import com.toggl.models.common.SwipeDirection
import com.toggl.repository.timeentry.StartTimeEntryResult
import com.toggl.repository.timeentry.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.single

class TimeEntryGroupSwipedActionTests : FreeSpec({

    val repository = mockk<TimeEntryRepository>()
    val entryInDatabase = createTimeEntry(1, "test")
    val entryToBeStarted = createTimeEntry(2, "test")
    coEvery { repository.startTimeEntry("test") } returns StartTimeEntryResult(entryToBeStarted, null)
    val reducer = createTimeEntriesLogReducer(repository)

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
                val effect = reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryGroupSwiped(listOf(1, 2), SwipeDirection.Right))
                val startedTimeEntry = (effect.single() as TimeEntriesLogAction.TimeEntryStarted).startedTimeEntry
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
                val effect = reducer.reduce(settableValue, action)
                val deletedTimeEntries = (effect.single() as TimeEntriesLogAction.TimeEntriesDeleted).deletedTimeEntries
                action.ids shouldContainAll deletedTimeEntries.map { it.id }
            }
        }
    }
})