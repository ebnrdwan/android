package com.toggl.timer.log.domain

import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.toSettableValue
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec
import io.mockk.mockk

class TimeEntryGroupTappedActionTests : FreeSpec({

    val repository = mockk<TimeEntryRepository>()
    val reducer = TimeEntriesLogReducer(repository)
    val testTe = createTimeEntry(1, "test")

    "The TimeEntryGroupTapped action" - {
        "should thrown when there are no time entries" - {
            "with the matching id" {
                val initialState = createInitialState(listOf(testTe))
                var state = initialState
                val settableValue = state.toSettableValue { state = it }
                shouldThrow<IllegalStateException> {
                    reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryGroupTapped(listOf(2)))
                }
            }

            "at all" {
                val initialState = createInitialState()
                assertAll(fn = { id: Long ->
                    var state = initialState
                    val settableValue = state.toSettableValue { state = it }
                    shouldThrow<IllegalStateException> {
                        reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryGroupTapped(listOf(id)))
                    }
                })
            }
        }

        "set the editing time entry property when the time entry exists" {
            val initialState = createInitialState(listOf(testTe))

            var state = initialState
            val settableValue = state.toSettableValue { state = it }
            val idsToEdit = listOf(1L, 2L)
            reducer.reduce(settableValue, TimeEntriesLogAction.TimeEntryGroupTapped(idsToEdit))
            state.editableTimeEntry!!.ids shouldBe idsToEdit
        }
    }
})
