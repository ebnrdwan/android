package com.toggl.repository.extensions

import com.toggl.database.models.DatabaseClient
import com.toggl.database.models.DatabaseProject
import com.toggl.database.models.DatabaseTag
import com.toggl.database.models.DatabaseTimeEntry
import com.toggl.database.models.DatabaseTimeEntryWithTags
import com.toggl.database.models.DatabaseWorkspace
import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.Tag
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace

fun DatabaseTimeEntryWithTags.toModel() = TimeEntry(
    timeEntry.id,
    timeEntry.description,
    timeEntry.startTime,
    timeEntry.duration,
    timeEntry.billable,
    timeEntry.workspaceId,
    timeEntry.projectId,
    timeEntry.taskId,
    timeEntry.isDeleted,
    tags
)

fun DatabaseTimeEntry.toModel() = TimeEntry(
    id,
    description,
    startTime,
    duration,
    billable,
    workspaceId,
    projectId,
    taskId,
    isDeleted,
    emptyList()
)

fun DatabaseProject.toModel() = Project(
    id,
    name,
    color,
    active,
    isPrivate,
    billable,
    workspaceId,
    clientId
)

fun DatabaseTag.toModel() = Tag(
    id,
    name,
    workspaceId
)

fun DatabaseWorkspace.toModel() = Workspace(
    id,
    name,
    features
)

fun DatabaseClient.toModel() = Client(
    id,
    name,
    workspaceId
)