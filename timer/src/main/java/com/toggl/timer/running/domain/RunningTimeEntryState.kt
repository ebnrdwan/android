package com.toggl.timer.running.domain

import arrow.optics.optics
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.TimerState

@optics
data class RunningTimeEntryState(
    val editableTimeEntry: EditableTimeEntry?,
    val timeEntries: Map<Long, TimeEntry>
) {
    companion object {
        fun fromTimerState(timerState: TimerState) =
            RunningTimeEntryState(
                editableTimeEntry = timerState.localState.editableTimeEntry,
                timeEntries = timerState.timeEntries
            )

        fun toTimerState(timerState: TimerState, runningTimeEntryState: RunningTimeEntryState) =
            timerState.copy(
                timeEntries = runningTimeEntryState.timeEntries,
                localState = timerState.localState.copy(
                    editableTimeEntry = runningTimeEntryState.editableTimeEntry
                )
            )
    }
}