package com.toggl.timer.start.domain

import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry

fun createInitialState(timeEntries: List<TimeEntry> = listOf()) =
    StartTimeEntryState(
        timeEntries = timeEntries.associateBy { it.id },
        editableTimeEntry = EditableTimeEntry.empty
    )