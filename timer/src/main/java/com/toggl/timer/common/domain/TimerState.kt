package com.toggl.timer.common.domain

import arrow.optics.optics
import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace

@optics
data class TimerState(
    val timeEntries: Map<Long, TimeEntry>,
    val projects: Map<Long, Project>,
    val clients: Map<Long, Client>,
    val workspaces: Map<Long, Workspace>,
    val localState: LocalState
) {
    data class LocalState internal constructor(
        internal val editViewTimeEntry: EditableTimeEntry?,
        internal val startViewTimeEntry: EditableTimeEntry,
        internal val expandedGroupIds: Set<Long>
    ) {
        constructor(defaultWorkspace: Workspace) : this(
            editViewTimeEntry = null,
            startViewTimeEntry = EditableTimeEntry.empty(defaultWorkspace.id),
            expandedGroupIds = setOf()
        )

        companion object
    }

    companion object
}

fun TimerState.LocalState.getRunningTimeEntryWorkspaceId() =
    this.startViewTimeEntry.workspaceId
