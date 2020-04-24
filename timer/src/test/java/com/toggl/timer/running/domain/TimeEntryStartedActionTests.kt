package com.toggl.timer.running.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

import org.threeten.bp.Duration

@ExperimentalCoroutinesApi
class TimeEntryStartedActionTests : FreeCoroutineSpec() {
    init {
        val repository = mockk<TimeEntryRepository>()
        val workspace = mockk<Workspace>()
        coEvery { workspace.id } returns 1
        val timeEntries = mapOf(
            1L to createTimeEntry(1, "first", duration = Duration.ofHours(1)),
            2L to createTimeEntry(2, "second", duration = null)
        )
        val editableTimeEntry = EditableTimeEntry.empty(workspace.id)
        val initState = createInitialState(
            editableTimeEntry = editableTimeEntry,
            timeEntries = timeEntries
        )
        val started = createTimeEntry(3, "started", duration = null)
        val stopped = timeEntries[2L]!!.copy(duration = Duration.ofHours(2))
        val reducer = RunningTimeEntryReducer(repository, dispatcherProvider, mockk())

        "The TimeEntryStarted action" - {
            "with stopped entry" - {
                reducer.testReduce(
                    initialState = initState,
                    action = RunningTimeEntryAction.TimeEntryStarted(started, stopped)
                ) { state, effect ->
                    "should start started time entry" {
                        state.timeEntries.shouldContain(3L to started)
                    }
                    "should stop stopped time entry" {
                        state.timeEntries.shouldContain(2L to stopped)
                    }
                    "shouldn't return any effect" {
                        effect.shouldBeEmpty()
                    }
                }
            }
            "without stopped entry" - {
                reducer.testReduce(
                    initialState = initState,
                    action = RunningTimeEntryAction.TimeEntryStarted(started, null)
                ) { state, effect ->
                    "should start started time entry" {
                        state.timeEntries.shouldContain(3L to started)
                    }
                    "shouldn't change any other time entry than the started one" {
                        state.timeEntries.filterKeys { key -> key != 3L } shouldBe initState.timeEntries.filterKeys { key -> key != 3L }
                    }
                    "shouldn't return any effect" {
                        effect.shouldBeEmpty()
                    }
                }
            }
        }
    }
}