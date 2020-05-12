package com.toggl.timer.common.domain

import arrow.optics.optics
import com.toggl.models.domain.TimeEntry
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

@optics
data class EditableTimeEntry(
    val ids: List<Long> = listOf(),
    val workspaceId: Long,
    val description: String = "",
    val startTime: OffsetDateTime? = null,
    val duration: Duration? = null,
    val billable: Boolean = false,
    val projectId: Long? = null,
    val taskId: Long? = null,
    val tagIds: List<Long> = listOf(),
    val editableProject: EditableProject? = null
) {
    companion object {
        fun empty(workspaceId: Long) = EditableTimeEntry(workspaceId = workspaceId)

        fun fromSingle(timeEntry: TimeEntry) =
            EditableTimeEntry(
                ids = listOf(timeEntry.id),
                workspaceId = timeEntry.workspaceId,
                description = timeEntry.description,
                startTime = timeEntry.startTime,
                duration = timeEntry.duration,
                billable = timeEntry.billable,
                projectId = timeEntry.projectId,
                tagIds = timeEntry.tagIds,
                taskId = timeEntry.taskId,
                editableProject = null
            )

        fun fromGroup(timeEntries: Collection<TimeEntry>): EditableTimeEntry {
            val sample = timeEntries.first()
            return EditableTimeEntry(
                ids = timeEntries.map { it.id },
                workspaceId = sample.workspaceId,
                description = sample.description,
                duration = timeEntries.fold(Duration.ZERO) { totalTime, timeEntry -> totalTime + timeEntry.duration },
                billable = sample.billable,
                projectId = sample.projectId,
                tagIds = sample.tagIds,
                taskId = sample.taskId,
                editableProject = null
            )
        }
    }
}
