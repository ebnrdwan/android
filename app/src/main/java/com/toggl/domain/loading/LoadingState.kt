package com.toggl.domain.loading

import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.Tag
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace

data class LoadingState(
    val workspaces: Collection<Workspace>,
    val projects: Collection<Project>,
    val clients: Collection<Client>,
    val tags: Collection<Tag>,
    val timeEntries: Collection<TimeEntry>
)