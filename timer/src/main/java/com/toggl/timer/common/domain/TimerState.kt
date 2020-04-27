package com.toggl.timer.common.domain

import arrow.optics.Lens
import arrow.optics.optics
import com.toggl.models.common.AutocompleteSuggestion
import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace
import com.toggl.timer.startedit.domain.DateTimePickMode

@optics
data class TimerState(
    val timeEntries: Map<Long, TimeEntry>,
    val projects: Map<Long, Project>,
    val clients: Map<Long, Client>,
    val workspaces: Map<Long, Workspace>,
    val localState: LocalState
) {
    data class LocalState internal constructor(
        internal val editableTimeEntry: EditableTimeEntry?,
        internal val expandedGroupIds: Set<Long>,
        internal val entriesPendingDeletion: Set<Long>,
        internal val autocompleteSuggestions: List<AutocompleteSuggestion>,
        internal val dateTimePickMode: DateTimePickMode
    ) {
        constructor() : this(
            editableTimeEntry = null,
            expandedGroupIds = setOf(),
            entriesPendingDeletion = setOf(),
            autocompleteSuggestions = emptyList(),
            dateTimePickMode = DateTimePickMode.None
        )

        companion object {
            internal val editableTimeEntry: Lens<LocalState, EditableTimeEntry> = Lens(
                get = { it.editableTimeEntry!! },
                set = { localState, editableTimeEntry -> localState.copy(editableTimeEntry = editableTimeEntry) }
            )
        }
    }

    companion object
}

fun TimerState.LocalState.getRunningTimeEntryWorkspaceId() =
    this.editableTimeEntry?.workspaceId