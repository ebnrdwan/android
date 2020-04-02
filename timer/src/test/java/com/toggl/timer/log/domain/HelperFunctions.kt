package com.toggl.timer.log.domain

import com.toggl.models.domain.TimeEntry

fun createInitialState(timeEntries: List<TimeEntry> = listOf(), expandedGroupIds: Set<Long> = setOf()) =
    TimeEntriesLogState(
        timeEntries = timeEntries.associateBy { it.id },
        projects = mapOf(),
        clients = mapOf(),
        editableTimeEntry = null,
        expandedGroupIds = expandedGroupIds
    )