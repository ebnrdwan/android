package com.toggl.timer.running.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.specs.FreeSpec
import io.mockk.coEvery
import io.mockk.mockk

class DescriptionTextFieldTappedActionTests : FreeSpec({
    val repository = mockk<TimeEntryRepository>()
    val workspace = mockk<Workspace>()
    val reducer = RunningTimeEntryReducer(repository)
    val editableTimeEntry = EditableTimeEntry.fromSingle(createTimeEntry(1, description = "Test"))
    val initialState = createInitialState(editableTimeEntry = editableTimeEntry)

    coEvery { workspace.id } returns 1

    "The DescriptionTextFieldTapped action" - {
        reducer.testReduce(
            initialState = initialState,
            action = RunningTimeEntryAction.DescriptionTextFieldTapped
        ) { state, effect ->
            "should init editableTimeEntry" {
                state.editableTimeEntry.shouldNotBeNull()
            }
            "shouldn't emit any effect effect" {
                effect.shouldBeEmpty()
            }
        }
    }
})