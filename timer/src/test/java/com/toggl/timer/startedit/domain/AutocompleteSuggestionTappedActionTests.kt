package com.toggl.timer.startedit.domain

import com.toggl.models.common.AutocompleteSuggestion
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.CoroutineTest
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduceNoEffects
import com.toggl.timer.common.testReduceState
import io.kotlintest.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

@ExperimentalCoroutinesApi
@DisplayName("The AutocompleteSuggestionTapped action")
internal class AutocompleteSuggestionTappedActionTests : CoroutineTest() {
    val initialState = createInitialState()
    val reducer = createReducer()

    @Nested
    @DisplayName("When a TimeEntry suggestion is tapped")
    inner class TimeEntrySuggestions : TheoryHolder {

        @ParameterizedTest
        @MethodSource("timeEntries")
        fun `The editableTimeEntry details should be updated`(timeEntrySuggestion: TimeEntry) = runBlockingTest {
            val initialEditableTimeEntry = EditableTimeEntry.empty(1)
            val suggestion = AutocompleteSuggestion.TimeEntry(timeEntrySuggestion)

            reducer.testReduceState(
                initialState,
                StartEditAction.AutocompleteSuggestionTapped(suggestion)
            ) {
                it shouldBe initialState.copy(
                    editableTimeEntry = initialEditableTimeEntry.copy(
                        description = timeEntrySuggestion.description,
                        billable = timeEntrySuggestion.billable,
                        projectId = timeEntrySuggestion.projectId,
                        tagIds = timeEntrySuggestion.tagIds,
                        workspaceId = timeEntrySuggestion.workspaceId,
                        taskId = timeEntrySuggestion.taskId
                    )
                )
            }
        }

        @ParameterizedTest
        @MethodSource("timeEntries")
        fun `should return no effect`(timeEntrySuggestion: TimeEntry) = runBlockingTest {
            val suggestion = AutocompleteSuggestion.TimeEntry(timeEntrySuggestion)

            reducer.testReduceNoEffects(
                initialState,
                action = StartEditAction.AutocompleteSuggestionTapped(suggestion)
            )
        }
    }

    interface TheoryHolder {
        companion object {
            @JvmStatic
            fun timeEntries(): Stream<TimeEntry> = Stream.of(
                createTimeEntry(10),
                createTimeEntry(20, "Expected Description"),
                createTimeEntry(30, billable = true),
                createTimeEntry(40, billable = false),
                createTimeEntry(50, projectId = 10),
                createTimeEntry(60, workspaceId = 10),
                createTimeEntry(80, taskId = 10),
                createTimeEntry(70, tags = listOf(10, 20)),
                createTimeEntry(
                    80,
                    "Expected Description",
                    billable = true,
                    projectId = 20,
                    workspaceId = 20,
                    tags = listOf(30),
                    taskId = 20
                )
            )
        }
    }
}