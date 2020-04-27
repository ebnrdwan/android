package com.toggl.timer.startedit.domain

import arrow.optics.optics
import com.toggl.models.common.AutocompleteSuggestion
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.TimerState

@optics
data class StartEditState(
    val timeEntries: Map<Long, TimeEntry>,
    val workspaces: Map<Long, Workspace>,
    val editableTimeEntry: EditableTimeEntry?,
    val autocompleteSuggestions: List<AutocompleteSuggestion>,
    val dateTimePickMode: DateTimePickMode
) {
    companion object {
        fun fromTimerState(timerState: TimerState) =
            StartEditState(
                timeEntries = timerState.timeEntries,
                workspaces = timerState.workspaces,
                editableTimeEntry = timerState.localState.editableTimeEntry,
                autocompleteSuggestions = timerState.localState.autocompleteSuggestions,
                dateTimePickMode = timerState.localState.dateTimePickMode
            )

        fun toTimerState(timerState: TimerState, startEditState: StartEditState) =
            timerState.copy(
                timeEntries = startEditState.timeEntries,
                localState = timerState.localState.copy(
                    editableTimeEntry = startEditState.editableTimeEntry,
                    autocompleteSuggestions = startEditState.autocompleteSuggestions,
                    dateTimePickMode = startEditState.dateTimePickMode
                )
            )
    }
}
