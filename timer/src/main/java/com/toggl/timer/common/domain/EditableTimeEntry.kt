package com.toggl.timer.common.domain

import arrow.optics.optics
import com.toggl.models.domain.TimeEntry

@optics
data class EditableTimeEntry(
    val ids: List<Long>,
    val workspaceId: Long,
    val description: String,
    val billable: Boolean
) {
    companion object {
        fun empty(workspaceId: Long) = EditableTimeEntry(listOf(), workspaceId, "", false)

        fun fromSingle(timeEntry: TimeEntry) =
            EditableTimeEntry(
                ids = listOf(timeEntry.id),
                workspaceId = timeEntry.workspaceId,
                description = timeEntry.description,
                billable = timeEntry.billable
            )

        fun fromGroup(ids: List<Long>, groupSample: TimeEntry) =
            EditableTimeEntry(
                ids = ids,
                workspaceId = groupSample.workspaceId,
                description = groupSample.description,
                billable = groupSample.billable
            )
    }
}
