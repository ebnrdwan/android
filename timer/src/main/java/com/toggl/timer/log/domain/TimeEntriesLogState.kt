package com.toggl.timer.log.domain

import arrow.optics.optics
import com.toggl.models.domain.Project
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.TimerState

@optics
data class TimeEntriesLogState(
    val timeEntries: Map<Long, TimeEntry>,
    val projects: Map<Long, Project>,
    val editableTimeEntry: EditableTimeEntry?
) {
    companion object {
        fun fromTimerState(timerState: TimerState) =
            TimeEntriesLogState(
                timerState.timeEntries,
                timerState.projects,
                timerState.localState.editViewTimeEntry
            )

        fun toTimerState(timerState: TimerState, timeEntriesLogState: TimeEntriesLogState) =
            timerState.copy(
                timeEntries = timeEntriesLogState.timeEntries,
                localState = timerState.localState.copy(
                    editViewTimeEntry = timeEntriesLogState.editableTimeEntry
                )
            )
    }
}
