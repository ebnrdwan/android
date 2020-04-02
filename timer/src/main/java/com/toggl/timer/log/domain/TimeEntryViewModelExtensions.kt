package com.toggl.timer.log.domain

import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.TimeEntry
import org.threeten.bp.Duration

fun TimeEntry.toFlatTimeEntryViewModel(projects: Map<Long, Project>, clients: Map<Long, Client>) =
    FlatTimeEntryViewModel(
        id = id,
        description = description,
        startTime = startTime,
        duration = duration
            ?: throw IllegalStateException("Running time entries are not supported"),
        project = projects.getProjectViewModelFor(this, clients),
        billable = billable
    )

fun List<TimeEntry>.toTimeEntryGroupViewModel(
    groupId: Long,
    isExpanded: Boolean,
    projects: Map<Long, Project>,
    clients: Map<Long, Client>
) =
    TimeEntryGroupViewModel(
        groupId = groupId,
        timeEntryIds = map(TimeEntry::id),
        isExpanded = isExpanded,
        description = first().description,
        duration = totalDuration(),
        project = projects.getProjectViewModelFor(this.first(), clients),
        billable = first().billable
    )

fun Project.toProjectViewModel(clients: Map<Long, Client>) = ProjectViewModel(id, name, color, clients[clientId]?.name)

fun List<TimeEntry>.totalDuration(): Duration =
    fold(Duration.ZERO) { acc, timeEntry -> acc + timeEntry.duration }

private fun Map<Long, Project>.getProjectViewModelFor(timeEntry: TimeEntry, clients: Map<Long, Client>): ProjectViewModel? {
    val projectId = timeEntry.projectId
    return if (projectId == null) null

    else this[projectId]?.toProjectViewModel(clients)
}
