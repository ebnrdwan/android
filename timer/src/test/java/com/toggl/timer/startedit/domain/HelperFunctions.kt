package com.toggl.timer.startedit.domain

import arrow.core.mapOf
import com.toggl.models.common.AutocompleteSuggestion
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace
import com.toggl.timer.common.domain.EditableTimeEntry

fun createInitialState(
    workspaces: List<Workspace> = listOf(),
    timeEntries: List<TimeEntry> = listOf(),
    editableTimeEntry: EditableTimeEntry = EditableTimeEntry.empty(1),
    autoCompleteSuggestions: List<AutocompleteSuggestion> = listOf()
) =
    StartEditState(
        tags = mapOf(),
        tasks = mapOf(),
        clients = mapOf(),
        projects = mapOf(),
        workspaces = workspaces.associateBy { it.id },
        timeEntries = timeEntries.associateBy { it.id },
        editableTimeEntry = editableTimeEntry,
        autocompleteSuggestions = autoCompleteSuggestions,
        dateTimePickMode = DateTimePickMode.None
    )