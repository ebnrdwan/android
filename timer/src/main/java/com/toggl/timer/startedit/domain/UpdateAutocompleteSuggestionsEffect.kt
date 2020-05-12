package com.toggl.timer.startedit.domain

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.models.common.AutocompleteSuggestion
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

    private val wordSeparator = " "

    override suspend fun execute(): StartEditAction.AutocompleteSuggestionsUpdated? =
        withContext(dispatcherProvider.computation) {
            val words = query.split(wordSeparator)
            val suggestions = fetchTimeEntrySuggestionsFor(words)
            StartEditAction.AutocompleteSuggestionsUpdated(suggestions)
        }

    private fun fetchTimeEntrySuggestionsFor(words: List<String>): List<AutocompleteSuggestion> {

        fun TimeEntry.projectOrClientNameContains(word: String) : Boolean {
            val project = projectId?.run(projects::get) ?: return false
            if (project.name.contains(word, true))
                return true

            val client = project.clientId?.run(clients::get) ?: return false
            return client.name.contains(word, true)
        }

        fun TimeEntry.tagNamesContain(word: String) : Boolean {
            if (tagIds.isEmpty())
                return false

            return tagIds.mapNotNull(tags::get).any { tag -> tag.name.contains(word, true) }
        }

        fun TimeEntry.taskNameContains(word: String) : Boolean {
            val task = taskId?.run(tasks::get) ?: return false
            return task.name.contains(word, true)
        }

        return words.fold(timeEntries.values) { timeEntries, word ->
            timeEntries.filter { timeEntry ->
                timeEntry.description.contains(word, true) ||
                timeEntry.projectOrClientNameContains(word) ||
                timeEntry.tagNamesContain(word) ||
                timeEntry.taskNameContains(word)
            }
        }.map(AutocompleteSuggestion::TimeEntry)
    }
}
