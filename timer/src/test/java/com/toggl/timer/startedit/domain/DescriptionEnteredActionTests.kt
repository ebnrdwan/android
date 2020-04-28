package com.toggl.timer.startedit.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.CoroutineTest
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@DisplayName("The TimeEntryDescriptionChanged action")
internal class DescriptionEnteredActionTests : CoroutineTest() {
    private val repository = mockk<TimeEntryRepository>()
    private val workspace = mockk<Workspace> { every { id } returns 1 }
    private val editableTimeEntry = EditableTimeEntry.fromSingle(createTimeEntry(1, description = ""))
    private val state = createInitialState(workspaces = listOf(workspace), editableTimeEntry = editableTimeEntry)
    private val reducer = StartEditReducer(repository, dispatcherProvider)

    @Test
    fun `should change EditableTimeEntry's description`() = runBlockingTest {
        reducer.testReduce(
            initialState = state,
            action = StartEditAction.DescriptionEntered("new description", 5)
        ) { state, _ -> state.editableTimeEntry!!.description shouldBe "new description" }
    }

    @Test
    fun `returns the effect that updates the autocomplete suggestions`() = runBlockingTest {
        reducer.testReduce(
            initialState = state,
            action = StartEditAction.DescriptionEntered("new description", 5)
        ) { _, effects -> effects.single()::class shouldBe UpdateAutocompleteSuggestionsEffect::class }
    }
}