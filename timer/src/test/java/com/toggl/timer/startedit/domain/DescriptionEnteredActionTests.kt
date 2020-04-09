package com.toggl.timer.startedit.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class DescriptionEnteredActionTests : FreeCoroutineSpec() {
    init {
        val repository = mockk<TimeEntryRepository>()
        val workspace = mockk<Workspace>()
        val editableTimeEntry = EditableTimeEntry.fromSingle(createTimeEntry(1, description = ""))
        val state = StartEditState(mapOf(), mapOf(1L to workspace), editableTimeEntry)
        val reducer = StartEditReducer(repository, dispatcherProvider)
        coEvery { workspace.id } returns 1

        "The TimeEntryDescriptionChanged action" - {
            reducer.testReduce(
                initialState = state,
                action = StartEditAction.DescriptionEntered("new description")
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
    }
}