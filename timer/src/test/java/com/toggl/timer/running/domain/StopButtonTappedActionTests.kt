package com.toggl.timer.running.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.createTimeEntry
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
class StopButtonTappedActionTests : FreeCoroutineSpec() {
    init {
        val repository = mockk<TimeEntryRepository>()
        val workspace = mockk<Workspace>()
        val reducer = RunningTimeEntryReducer(repository, dispatcherProvider, mockk())
        val editableTimeEntry =
            EditableTimeEntry.fromSingle(createTimeEntry(1, description = "Test"))
        val initialState = createInitialState(editableTimeEntry = editableTimeEntry)

        coEvery { workspace.id } returns 1

        "The StopButtonTapped action" - {
            reducer.testReduce(
                initialState = initialState,
                action = RunningTimeEntryAction.StopButtonTapped
            ) { state, effect ->
                "shouldn't change the state" {
                    state shouldBe initialState
                }
                "should emit StopTimeEntry effect" {
                    effect.shouldBeSingleton()
                    effect.single()
                        .shouldBeTypeOf<StopTimeEntryEffect<RunningTimeEntryAction.TimeEntryUpdated>>()
                }
            }
        }
    }
}