package com.toggl.timer.startedit.domain

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.models.domain.Client
import com.toggl.models.domain.Project
import com.toggl.models.domain.Tag
import com.toggl.models.domain.Task
import com.toggl.models.domain.TimeEntry
import kotlinx.coroutines.withContext

class UpdateAutocompleteSuggestionsEffect(
    private val dispatcherProvider: DispatcherProvider,
    private val query: String,
    private val cursorPosition: Int,
    private val tags: Map<Long, Tag>,
    private val tasks: Map<Long, Task>,
    private val clients: Map<Long, Client>,
    private val projects: Map<Long, Project>,
    private val timeEntries: Map<Long, TimeEntry>
) : Effect<StartEditAction.AutocompleteSuggestionsUpdated> {
    override suspend fun execute(): StartEditAction.AutocompleteSuggestionsUpdated? =
        withContext(dispatcherProvider.computation) {
            StartEditAction.AutocompleteSuggestionsUpdated(emptyList())
        }
}
