package com.toggl.timer.log.domain

import com.toggl.architecture.core.SettableValue
import com.toggl.models.domain.TimeEntry

fun createInitialState(timeEntries: List<TimeEntry> = listOf()) =
    TimeEntriesLogState(
        timeEntries = timeEntries.associateBy { it.id },
        projects = mapOf(),
        editedTimeEntry = null
    )
