package com.toggl.timer.start.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.specs.FreeSpec
import io.mockk.coEvery
import io.mockk.mockk

class CloseButtonTappedActionTests : FreeSpec({
    val repository = mockk<TimeEntryRepository>()
    val workspace = mockk<Workspace>()
    val editableTimeEntry = EditableTimeEntry.fromSingle(createTimeEntry(1, description = "Test"))
    val state = StartTimeEntryState(mapOf(), mapOf(1L to workspace), editableTimeEntry)
    val reducer = StartTimeEntryReducer(repository)
    coEvery { workspace.id } returns 1

    "The CloseButtonTapped action" - {
        reducer.testReduce(
            initialState = state,
            action = StartTimeEntryAction.CloseButtonTapped
        ) { state, effect ->
            "should nullify editableTimeEntry" {
                state.editableTimeEntry.shouldBeNull()
            }
            "shouldn't emmit any effect" {
                effect.shouldBeEmpty()
            }
        }
    }
})