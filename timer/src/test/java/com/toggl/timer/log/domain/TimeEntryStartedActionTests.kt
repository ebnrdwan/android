package com.toggl.timer.log.domain

import com.toggl.architecture.extensions.noEffect
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.toMutableValue
import io.kotlintest.properties.assertAll
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.time.Duration

@ExperimentalCoroutinesApi
class TimeEntryStartedActionTests : FreeCoroutineSpec() {
    init {
        val repository = mockk<TimeEntryRepository>()
        val reducer = TimeEntriesLogReducer(repository, dispatcherProvider)

        "The TimeEntryStarted action" - {
            "updates the state to add the time entry" - {
                var state = createInitialState()
                val mutableValue = state.toMutableValue { state = it }

                assertAll(fn = { id: Long ->
                    val timeEntry = createTimeEntry(id, "test")
                    val action = TimeEntriesLogAction.TimeEntryStarted(timeEntry, null)
                    val effect = reducer.reduce(mutableValue, action)
                    effect shouldBe noEffect()
                    state.timeEntries[timeEntry.id] shouldNotBe null
                })
            }
            "updates the state to update the stopped entry" - {
                val timeEntryToBeStopped = createTimeEntry(321, "stopped")
                val stoppedTimeEntry = timeEntryToBeStopped.copy(duration = Duration.ofHours(1))
                var state = createInitialState(timeEntries = listOf(timeEntryToBeStopped))
                val mutableValue = state.toMutableValue { state = it }

                val action =
                    TimeEntriesLogAction.TimeEntryStarted(createTimeEntry(1, ""), stoppedTimeEntry)
                val effect = reducer.reduce(mutableValue, action)
                effect shouldBe noEffect()
                state.timeEntries[stoppedTimeEntry.id] shouldNotBe null
                state.timeEntries[stoppedTimeEntry.id]?.duration shouldBe stoppedTimeEntry.duration
            }
        }
    }
}