package com.toggl.timer.startedit.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.StopTimeEntryEffect
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeSingleton
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class StopTimeEntryButtonTappedActionTests : FreeCoroutineSpec() {
    init {
        val repository = mockk<TimeEntryRepository>()
        val workspace = mockk<Workspace>()
        coEvery { workspace.id } returns 1
        val editableTimeEntry = EditableTimeEntry.empty(workspace.id)
        val initState = StartEditState(mapOf(), mapOf(1L to workspace), editableTimeEntry)
        val reducer = StartEditReducer(repository, dispatcherProvider)

        "The StopTimeEntryButtonTapped action" - {
            reducer.testReduce(
                initialState = initState,
                action = StartEditAction.StopTimeEntryButtonTapped
            ) { state, effect ->
                "should return StartTimeEntryEffect effect" {
                    effect.shouldBeSingleton()
                    effect.single()
                        .shouldBeTypeOf<StopTimeEntryEffect<StartEditAction.TimeEntryUpdated>>()
                }
                "shouldn't change the state" {
                    state shouldBe initState
                }
            }
        }
    }
}