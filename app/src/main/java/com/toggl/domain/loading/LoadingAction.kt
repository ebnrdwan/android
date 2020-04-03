package com.toggl.domain.loading

import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.Tag
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace

sealed class LoadingAction {
    object StartLoading : LoadingAction()
    data class TagsLoaded(val tags: List<Tag>) : LoadingAction()
    data class WorkspacesLoaded(val workspaces: List<Workspace>) : LoadingAction()
    data class ProjectsLoaded(val projects: List<Project>) : LoadingAction()
    data class ClientsLoaded(val clients: List<Client>) : LoadingAction()
    data class TimeEntriesLoaded(val timeEntries: List<TimeEntry>) : LoadingAction()
}

fun LoadingAction.formatForDebug() =
    when (this) {
        LoadingAction.StartLoading -> "Entities started loading"
        is LoadingAction.WorkspacesLoaded -> "Loaded ${workspaces.size} Workspaces"
        is LoadingAction.TimeEntriesLoaded -> "Loaded ${timeEntries.size} Time Entries"
        is LoadingAction.ProjectsLoaded -> "Loaded ${projects.size} projects"
        is LoadingAction.ClientsLoaded -> "Loaded ${clients.size} clients"
        is LoadingAction.TagsLoaded -> "Loaded ${tags.size} tags"
    }
