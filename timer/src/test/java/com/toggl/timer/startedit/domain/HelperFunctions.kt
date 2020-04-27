package com.toggl.timer.startedit.domain

import arrow.core.mapOf
import com.toggl.models.common.AutocompleteSuggestion
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry

fun createInitialState(
    timeEntries: List<TimeEntry> = listOf(),
    autoCompleteSuggestions: List<AutocompleteSuggestion> = listOf()
) =
    StartEditState(
        timeEntries = timeEntries.associateBy { it.id },
        workspaces = mapOf(),
        editableTimeEntry = EditableTimeEntry.empty(1),
        autocompleteSuggestions = autoCompleteSuggestions,
        dateTimePickMode = DateTimePickMode.None
    )