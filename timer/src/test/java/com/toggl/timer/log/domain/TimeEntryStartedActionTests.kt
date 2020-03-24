package com.toggl.timer.log.domain

import com.toggl.architecture.extensions.noEffect
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.toSettableValue
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.mockk
import org.threeten.bp.Duration

class TimeEntryStartedActionTests : FreeSpec({

    val repository = mockk<TimeEntryRepository>()
    val reducer = TimeEntriesLogReducer(repository)

    "The TimeEntryStarted action" - {
        "updates the state to add the time entry" - {
            var state = createInitialState()
            val settableValue = state.toSettableValue { state = it }

            assertAll(fn = { id: Long ->
                val timeEntry = createTimeEntry(id, "test")
                val action = TimeEntriesLogAction.TimeEntryStarted(timeEntry, null)
                val effect = reducer.reduce(settableValue, action)
                effect shouldBe noEffect()
                state.timeEntries[timeEntry.id] shouldNotBe null
            })
        }
        "updates the state to update the stopped entry" - {
            val timeEntryToBeStopped = createTimeEntry(321, "stopped")
            val stoppedTimeEntry = timeEntryToBeStopped.copy(duration = Duration.ofHours(1))
            var state = createInitialState(timeEntries = listOf(timeEntryToBeStopped))
            val settableValue = state.toSettableValue { state = it }

            val action = TimeEntriesLogAction.TimeEntryStarted(createTimeEntry(1, ""), stoppedTimeEntry)
            val effect = reducer.reduce(settableValue, action)
            effect shouldBe noEffect()
            state.timeEntries[stoppedTimeEntry.id] shouldNotBe null
            state.timeEntries[stoppedTimeEntry.id]?.duration shouldBe stoppedTimeEntry.duration
        }
    }
})