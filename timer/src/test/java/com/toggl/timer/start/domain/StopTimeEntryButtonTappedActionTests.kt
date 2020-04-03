package com.toggl.timer.start.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.StopTimeEntryEffect
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeSingleton
import io.kotlintest.matchers.types.shouldBeTypeOf
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.coEvery
import io.mockk.mockk

class StopTimeEntryButtonTappedActionTests : FreeSpec({
    val repository = mockk<TimeEntryRepository>()
    val workspace = mockk<Workspace>()
    coEvery { workspace.id } returns 1
    val editableTimeEntry = EditableTimeEntry.empty(workspace.id)
    val initState = StartTimeEntryState(mapOf(), mapOf(1L to workspace), editableTimeEntry)
    val reducer = StartTimeEntryReducer(repository)

    "The StopTimeEntryButtonTapped action" - {
        reducer.testReduce(
            initialState = initState,
            action = StartTimeEntryAction.StopTimeEntryButtonTapped
        ) { state, effect ->
            "should return StartTimeEntryEffect effect" {
                effect.shouldBeSingleton()
                effect.single().shouldBeTypeOf<StopTimeEntryEffect<StartTimeEntryAction.TimeEntryUpdated>>()
            }
            "shouldn't change the state" {
                state shouldBe initState
            }
        }
    }
})