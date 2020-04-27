package com.toggl.timer.startedit.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.CoroutineTest
import com.toggl.timer.common.assertNoEffectsWereReturned
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.types.shouldBeNull
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@DisplayName("The CloseButtonTapped action")
class CloseButtonTappedActionTests : CoroutineTest() {
    private val repository = mockk<TimeEntryRepository>()
    private val workspace = mockk<Workspace> { every { id } returns 1 }
    private val editableTimeEntry = EditableTimeEntry.fromSingle(createTimeEntry(1, description = "Test"))
    private val state = createInitialState(workspaces = listOf(workspace), editableTimeEntry = editableTimeEntry)
    private val reducer = StartEditReducer(repository, dispatcherProvider)

    @Test
    fun `should set the editableTimeEntry to null`() = runBlockingTest {
        reducer.testReduce(
            initialState = state,
            action = StartEditAction.CloseButtonTapped
        ) { state, _ -> state.editableTimeEntry.shouldBeNull() }
    }

    @Test
    fun `shouldn't return any effect`() = runBlockingTest {
        reducer.testReduce(
            initialState = state,
            action = StartEditAction.CloseButtonTapped,
            testCase = ::assertNoEffectsWereReturned
        )
    }
}