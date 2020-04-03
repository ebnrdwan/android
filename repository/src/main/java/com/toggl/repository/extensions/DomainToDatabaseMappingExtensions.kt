package com.toggl.repository.extensions

import com.toggl.database.models.DatabaseTimeEntry
import com.toggl.database.models.DatabaseTimeEntryWithTags
import com.toggl.models.domain.TimeEntry

fun TimeEntry.toDatabaseModel() = DatabaseTimeEntryWithTags(
    toDatabaseTimeEntry(),
    tagIds
)

fun TimeEntry.toDatabaseTimeEntry() = DatabaseTimeEntry(
    id,
    description,
    startTime,
    duration,
    billable,
    workspaceId,
    projectId,
    taskId,
    isDeleted
)