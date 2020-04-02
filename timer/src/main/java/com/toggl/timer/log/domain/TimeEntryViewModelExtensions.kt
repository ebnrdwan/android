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
        project = projects.getProjectViewModelFor(this),
        client = clients.getClientViewModelFor(this, projects),
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
        project = projects.getProjectViewModelFor(this.first()),
        client = clients.getClientViewModelFor(this.first(), projects),
        billable = first().billable
    )

fun Project.toProjectViewModel() = ProjectViewModel(id, name, color)
fun Client.toClientViewModel() = ClientViewModel(id, name)

fun List<TimeEntry>.totalDuration(): Duration =
    fold(Duration.ZERO) { acc, timeEntry -> acc + timeEntry.duration }

private fun Map<Long, Project>.getProjectViewModelFor(timeEntry: TimeEntry): ProjectViewModel? {
    val projectId = timeEntry.projectId
    return if (projectId == null) null
    else this[projectId]?.run(Project::toProjectViewModel)
}

private fun Map<Long, Client>.getClientViewModelFor(
    timeEntry: TimeEntry,
    projects: Map<Long, Project>
): ClientViewModel? {
    val clientId = timeEntry.projectId?.let { projects[it]?.clientId } ?: return null

    return this[clientId]?.run(Client::toClientViewModel)
}