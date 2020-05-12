package com.toggl.timer.startedit.domain

import com.google.android.gms.common.api.Api
import com.toggl.models.common.AutocompleteSuggestion
import com.toggl.models.domain.Client
import com.toggl.models.domain.Tag
import com.toggl.models.domain.Task
import com.toggl.timer.common.CoroutineTest
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.project.domain.createProject
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@DisplayName("The UpdateAutocompleteSuggestionsEffect")
class UpdateAutocompleteSuggestionsEffectTests : CoroutineTest() {

    @Test
    fun `returns time entries when the description matches the query`() = runBlockingTest {

        val timeEntries = (1L..10L).map { createTimeEntry(it, description = if (it % 2 == 0L) "Even $it" else "Odd $it") }

        val effect = UpdateAutocompleteSuggestionsEffect(
            dispatcherProvider,
            "even",
            0,
            mapOf(),
            mapOf(),
            mapOf(),
            mapOf(),
            timeEntries.associateBy { it.id }
        )

        val result = effect.execute()
        result.shouldNotBeNull()
        result.autocompleteSuggestions.filterIsInstance<AutocompleteSuggestion.TimeEntry>().size shouldBe 5
    }

    @Test
    fun `returns time entries when the project name matches the query`() = runBlockingTest {

        val projects = listOf(createProject(1, name = "project"))
        val timeEntries = (1L..10L).map { createTimeEntry(it, projectId = if (it % 2 == 0L) null else 1L) }

        val effect = UpdateAutocompleteSuggestionsEffect(
            dispatcherProvider,
            "proj",
            0,
            mapOf(),
            mapOf(),
            mapOf(),
            projects.associateBy { it.id },
            timeEntries.associateBy { it.id }
        )

        val result = effect.execute()
        result.shouldNotBeNull()
        result.autocompleteSuggestions.filterIsInstance<AutocompleteSuggestion.TimeEntry>().size shouldBe 5
    }

    @Test
    fun `returns time entries when the client name matches the query`() = runBlockingTest {

        val projects = listOf(createProject(1, name = "project", clientId = 1))
        val clients = listOf(Client(1, "client", 1))
        val timeEntries = (1L..10L).map { createTimeEntry(it, projectId = if (it % 2 == 0L) null else 1L) }

        val effect = UpdateAutocompleteSuggestionsEffect(
            dispatcherProvider,
            "clien",
            0,
            mapOf(),
            mapOf(),
            clients.associateBy { it.id },
            projects.associateBy { it.id },
            timeEntries.associateBy { it.id }
        )

        val result = effect.execute()
        result.shouldNotBeNull()
        result.autocompleteSuggestions.filterIsInstance<AutocompleteSuggestion.TimeEntry>().size shouldBe 5
    }

    @Test
    fun `returns time entries when the task name matches the query`() = runBlockingTest {

        val tasks = listOf(Task(1, "task", true, 1, 1, 1))
        val timeEntries = (1L..10L).map { createTimeEntry(it, taskId = if (it % 2 == 0L) null else 1L) }

        val effect = UpdateAutocompleteSuggestionsEffect(
            dispatcherProvider,
            "tas",
            0,
            mapOf(),
            tasks.associateBy { it.id },
            mapOf(),
            mapOf(),
            timeEntries.associateBy { it.id }
        )

        val result = effect.execute()
        result.shouldNotBeNull()
        result.autocompleteSuggestions.filterIsInstance<AutocompleteSuggestion.TimeEntry>().size shouldBe 5
    }

    @Test
    fun `matches multiple words for time entry descriptions`() = runBlockingTest {

        val timeEntries = listOf(
            createTimeEntry(1, description = "Android App Development"), // Matches
            createTimeEntry(2, description = "Another apt technology to be developed"), // Matches
            createTimeEntry(3, description = "Anthropomorphic apartment devs"), // Matches
            createTimeEntry(4, description = "ANTS APPEAR MORE DEVELOPED HERE"), // Matches
            createTimeEntry(5, description = "iOS App development"),
            createTimeEntry(6, description = "Another technology"),
            createTimeEntry(7, description = "Meetings")
        )

        val effect = UpdateAutocompleteSuggestionsEffect(
            dispatcherProvider,
            "an ap dev",
            0,
            mapOf(),
            mapOf(),
            mapOf(),
            mapOf(),
            timeEntries.associateBy { it.id }
        )

        val expectedEntries = timeEntries.take(4)
        val result = effect.execute()
        result.shouldNotBeNull()

        val timeEntrySuggestions = result.autocompleteSuggestions.filterIsInstance<AutocompleteSuggestion.TimeEntry>()
        timeEntrySuggestions.size shouldBe expectedEntries.size
        timeEntrySuggestions.all { expectedEntries.contains(it.timeEntry) }.shouldBeTrue()
    }

    @Test
    fun `matches multiple words for when the matches happen in different entities`() = runBlockingTest {

        val tasks = listOf(
            Task(1, "ants",true, 1, 1, 1)
        )

        val clients = listOf(
            Client(1, "devs", 1)
        )

        val projects = listOf(
            createProject(1, name = "App"),
            createProject(2, name = "dunno", clientId = 1)
        )
        val tags = listOf(
            Tag(1, "ants", 1),
            Tag(2, "app", 1)
        )

        val timeEntries = listOf(
            createTimeEntry(1, description = "Android App Development"), // Matches by description
            createTimeEntry(2, description = "Another technology to be developed", projectId = 1), // Matches by description and project
            createTimeEntry(3, description = "Anthropomorphic apartment", projectId = 2), // Matches by description and client name
            createTimeEntry(4, description = "", projectId = 2, tags = listOf(1, 2)), // Matches by client and tags
            createTimeEntry(5, description = "iOS App development", taskId = 1), // Matches by description and task
            createTimeEntry(6, description = "Another technology"),
            createTimeEntry(7, description = "Meetings")
        )

        val effect = UpdateAutocompleteSuggestionsEffect(
            dispatcherProvider,
            "an ap dev",
            0,
            tags.associateBy { it.id },
            tasks.associateBy { it.id },
            clients.associateBy { it.id },
            projects.associateBy { it.id },
            timeEntries.associateBy { it.id }
        )

        val expectedEntries = timeEntries.take(5)
        val result = effect.execute()
        result.shouldNotBeNull()

        val timeEntrySuggestions = result.autocompleteSuggestions.filterIsInstance<AutocompleteSuggestion.TimeEntry>()
        timeEntrySuggestions.size shouldBe expectedEntries.size
        timeEntrySuggestions.all { expectedEntries.contains(it.timeEntry) }.shouldBeTrue()
    }
}