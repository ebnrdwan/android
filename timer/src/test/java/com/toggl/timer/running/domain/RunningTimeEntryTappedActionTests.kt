package com.toggl.timer.running.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.coEvery
import io.mockk.mockk
import org.threeten.bp.Duration

class RunningTimeEntryTappedActionTests : FreeSpec({
    val repository = mockk<TimeEntryRepository>()
    val workspace = mockk<Workspace>()
    val reducer = RunningTimeEntryReducer(repository)
    val editableTimeEntry = EditableTimeEntry.fromSingle(createTimeEntry(1, description = "Test"))
    val initialState = createInitialState(
        editableTimeEntry = editableTimeEntry,
        timeEntries = mapOf(
            1L to createTimeEntry(1, description = "Test", duration = Duration.ofHours(1)),
            2L to createTimeEntry(2, description = "Running", duration = null)
        )
    )

    coEvery { workspace.id } returns 1

    "The RunningTimeEntryTapped action" - {
        reducer.testReduce(
            initialState = initialState,
            action = RunningTimeEntryAction.RunningTimeEntryTapped
        ) { state, effect ->
            "should init editableTimeEntry with currently running time entry" {
                state.editableTimeEntry.shouldNotBeNull()
                state.editableTimeEntry!!.description shouldBe "Running"
            }
            "shouldn't emit any effect effect" {
                effect.shouldBeEmpty()
            }
        }
    }
})