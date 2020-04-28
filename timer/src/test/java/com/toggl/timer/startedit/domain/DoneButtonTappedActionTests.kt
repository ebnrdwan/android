package com.toggl.timer.startedit.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.StartTimeEntryResult
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.toMutableValue
import com.toggl.timer.exceptions.EditableTimeEntryShouldNotBeNullException
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class DoneButtonTappedActionTests : FreeCoroutineSpec() {
    private val repository = mockk<TimeEntryRepository>()
    private val startTimeEntryResult = mockk<StartTimeEntryResult>()
    private val timeEntry = createTimeEntry(1, "old description")
    private val timeEntry2 = createTimeEntry(2, "old description")
    private val workspace = mockk<Workspace> { every { id } returns 1 }
    private val editableTimeEntry =
        EditableTimeEntry.fromSingle(createTimeEntry(1, description = "Test"))
    private val reducer = StartEditReducer(repository, dispatcherProvider)
    private val state = createInitialState(
        workspaces = listOf(workspace),
        timeEntries = listOf(timeEntry, timeEntry2),
        editableTimeEntry = editableTimeEntry
    )

    init {
        coEvery { workspace.id } returns 1
        every { startTimeEntryResult.startedTimeEntry } returns mockk()
        every { startTimeEntryResult.stoppedTimeEntry } returns mockk()

        "The DoneButtonTapped action" - {
            "should throw when there's no editable entry" {
                var initialState = state.copy(editableTimeEntry = null)
                val mutableValue = initialState.toMutableValue { initialState = it }

                shouldThrow<EditableTimeEntryShouldNotBeNullException> {
                    reducer.reduce(mutableValue, StartEditAction.DoneButtonTapped)
                }
            }

            "should start the TE if the editable has no ids" {
                var initialState = state.copy(editableTimeEntry = editableTimeEntry.copy(ids = listOf()))
                val mutableValue = initialState.toMutableValue { initialState = it }

                val result = reducer.reduce(mutableValue, StartEditAction.DoneButtonTapped)
                result[0].execute()

                result.size shouldBe 1
                coVerify {
                    repository.startTimeEntry(1, "Test")
                }
            }

            "should update the TE if the editable has one id" {
                var initialState = state.copy()
                val mutableValue = initialState.toMutableValue { initialState = it }

                val result = reducer.reduce(mutableValue, StartEditAction.DoneButtonTapped)
                result[0].execute()

                result.size shouldBe 1
                coVerify {
                    repository.editTimeEntry(timeEntry.copy(description = "Test"))
                }
            }

            "should update each TE in a group if the editable has several ids" {
                var initialState = state.copy(editableTimeEntry = editableTimeEntry.copy(ids = listOf(1L, 2L)))
                val mutableValue = initialState.toMutableValue { initialState = it }

                val result = reducer.reduce(mutableValue, StartEditAction.DoneButtonTapped)
                result.forEach { it.execute() }

                result.size shouldBe 2
                coVerify {
                    repository.editTimeEntry(timeEntry.copy(description = "Test"))
                    repository.editTimeEntry(timeEntry2.copy(description = "Test"))
                }
            }

            "should update no TEs if they're not in state" {
                var initialState = state.copy(editableTimeEntry = editableTimeEntry.copy(ids = listOf(3L, 4L)))
                val mutableValue = initialState.toMutableValue { initialState = it }

                val result = reducer.reduce(mutableValue, StartEditAction.DoneButtonTapped)
                result.forEach { it.execute() }

                result.size shouldBe 0
                coVerify(exactly = 0) {
                    repository.editTimeEntry(any())
                }
            }
        }
    }

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        clearMocks(repository)
        coEvery { repository.startTimeEntry(any(), any()) } returns startTimeEntryResult
        coEvery { repository.editTimeEntry(any()) } returns timeEntry
    }
}