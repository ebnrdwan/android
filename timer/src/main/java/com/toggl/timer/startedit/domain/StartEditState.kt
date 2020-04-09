package com.toggl.timer.startedit.domain

import arrow.optics.optics
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.TimerState

@optics
data class StartEditState(
    val timeEntries: Map<Long, TimeEntry>,
    val workspaces: Map<Long, Workspace>,
    val editableTimeEntry: EditableTimeEntry?
) {
    companion object {
        fun fromTimerState(timerState: TimerState) =
            StartEditState(
                timeEntries = timerState.timeEntries,
                workspaces = timerState.workspaces,
                editableTimeEntry = timerState.localState.editableTimeEntry
            )

        fun toTimerState(timerState: TimerState, startEditState: StartEditState) =
            timerState.copy(
                timeEntries = startEditState.timeEntries,
                localState = timerState.localState.copy(
                    editableTimeEntry = startEditState.editableTimeEntry
                )
            )
    }
}