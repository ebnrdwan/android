package com.toggl.timer.start.domain

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

class DescriptionEnteredActionTests : FreeSpec({
    val repository = mockk<TimeEntryRepository>()
    val workspace = mockk<Workspace>()
    val editableTimeEntry = EditableTimeEntry.fromSingle(createTimeEntry(1, description = ""))
    val state = StartTimeEntryState(mapOf(), mapOf(1L to workspace), editableTimeEntry)
    val reducer = StartTimeEntryReducer(repository)
    coEvery { workspace.id } returns 1

    "The TimeEntryDescriptionChanged action" - {
        reducer.testReduce(
            initialState = state,
            action = StartTimeEntryAction.DescriptionEntered("new description")
        ) { state, effect ->
            "should change EditableTimeEntry's description" {
                state.editableTimeEntry.shouldNotBeNull()
                state.editableTimeEntry!!.description shouldBe "new description"
                effect.shouldBeEmpty()
            }
            "shouldn't return any effect" {
                effect.shouldBeEmpty()
            }
        }
    }
})
