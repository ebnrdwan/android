package com.toggl.timer.startedit.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.CoroutineTest
import com.toggl.timer.common.assertNoEffectsWereReturned
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@DisplayName("The TimeEntryUpdated action")
class TimeEntryUpdatedActionTests : CoroutineTest() {

    private val repository = mockk<TimeEntryRepository>()
    private val workspace = mockk<Workspace> { every { id } returns 1 }
    private val timeEntries = listOf(
        createTimeEntry(1, "first"),
        createTimeEntry(2, "second"),
        createTimeEntry(3, "third")
    )
    private val editableTimeEntry = EditableTimeEntry.empty(workspace.id)
    private val initState = createInitialState(
        timeEntries = timeEntries,
        workspaces = listOf(workspace),
        editableTimeEntry = editableTimeEntry
    )
    private val updated = timeEntries[1].copy(description = "second updated")
    private val reducer = StartEditReducer(repository, dispatcherProvider)

    @Test
    fun `should update updated time entry`() = runBlockingTest {
        reducer.testReduce(
            initialState = initState,
            action = StartEditAction.TimeEntryUpdated(2, updated)
        ) { state, _ -> state.timeEntries.shouldContain(2L to updated) }
    }

    @Test
    fun `shouldn't change any other time entry than the updated one`() = runBlockingTest {
        val expectedTimeEntries = initState.timeEntries.filterKeys { key -> key != 2L }
        reducer.testReduce(
            initialState = initState,
            action = StartEditAction.TimeEntryUpdated(2, updated)
        ) { state, _ -> state.timeEntries.filterKeys { key -> key != 2L } shouldBe expectedTimeEntries }
    }

    @Test
    fun `shouldn't return any effect`() = runBlockingTest {
        reducer.testReduce(
            initialState = initState,
            action = StartEditAction.TimeEntryUpdated(2, updated),
            testCase = ::assertNoEffectsWereReturned
        )
    }
}