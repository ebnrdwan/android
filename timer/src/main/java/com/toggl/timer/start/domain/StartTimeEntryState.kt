package com.toggl.timer.start.domain

import arrow.optics.optics
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.TimerState

@optics
data class StartTimeEntryState(
    val timeEntries: Map<Long, TimeEntry>,
    val workspaces: Map<Long, Workspace>,
    val editableTimeEntry: EditableTimeEntry?
) {
    companion object {
        fun fromTimerState(timerState: TimerState) =
            StartTimeEntryState(
                timeEntries = timerState.timeEntries,
                workspaces = timerState.workspaces,
                editableTimeEntry = timerState.localState.editableTimeEntry
            )

        fun toTimerState(timerState: TimerState, startTimeEntryState: StartTimeEntryState) =
            timerState.copy(
                timeEntries = startTimeEntryState.timeEntries,
                localState = timerState.localState.copy(
                    editableTimeEntry = startTimeEntryState.editableTimeEntry
                )
            )
    }
}
