package com.toggl.timer.log.domain

import com.toggl.models.common.SwipeDirection
import com.toggl.repository.timeentry.StartTimeEntryResult
import com.toggl.repository.timeentry.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec
import io.mockk.coEvery
import io.mockk.mockk

class TimeEntrySwipedActionTests : FreeSpec({

    val repository = mockk<TimeEntryRepository>()
    val entryInDatabase = createTimeEntry(1, "test")
    val entryToBeStarted = createTimeEntry(2, "test")
    coEvery { repository.startTimeEntry("test") } returns StartTimeEntryResult(entryToBeStarted, null)
    coEvery { repository.deleteTimeEntries(listOf(entryInDatabase)) } returns hashSetOf(entryInDatabase.copy(isDeleted = true))
    val reducer = createTimeEntriesLogReducer(repository)

    "The TimeEntrySwiped action" - {
        "should throw when there are no time entries" - {
            "with the matching id" {
                val initialState = createInitialState(listOf(entryInDatabase))
                var state = initialState
                val settableValue = state.toSettableValue { state = it }

                shouldThrow<IllegalStateException> {
                    reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntrySwiped(2, SwipeDirection.Left))
                }
            }

            "at all" {
                val initialState = createInitialState()

                assertAll(fn = { id: Long ->
                    var state = initialState
                    val settableValue = state.toSettableValue { state = it }
                    shouldThrow<IllegalStateException> {
                        reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntrySwiped(id, SwipeDirection.Left))
                    }
                })
            }
        }

        "when swiping right" - {
            "should continue the swiped time entry" {
                val initialState = createInitialState(listOf(entryInDatabase))
                var state = initialState
                val settableValue = state.toSettableValue { state = it }
                val action = TimeEntriesLogAction.TimeEntrySwiped(1, SwipeDirection.Right)
                val effectAction = reducer.reduce(settableValue, action).execute() as TimeEntriesLogAction.TimeEntryStarted
                val startedTimeEntry = effectAction.startedTimeEntry
                startedTimeEntry shouldBe entryToBeStarted
            }
        }

        "when swiping left" - {
            "should delete the swiped time entry" {
                val initialState = createInitialState(listOf(entryInDatabase))
                var state = initialState
                val settableValue = state.toSettableValue { state = it }
                val action = TimeEntriesLogAction.TimeEntrySwiped(1, SwipeDirection.Left)
                val effectAction = reducer.reduce(settableValue, action).execute() as TimeEntriesLogAction.TimeEntriesDeleted
                val deletedTimeEntries = effectAction.deletedTimeEntries
                deletedTimeEntries.single() shouldBe entryInDatabase.copy(isDeleted = true)
            }
        }
    }
})