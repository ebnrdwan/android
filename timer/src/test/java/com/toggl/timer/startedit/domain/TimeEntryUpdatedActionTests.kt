package com.toggl.timer.startedit.domain

import com.toggl.models.domain.Workspace
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.createTimeEntry
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class TimeEntryUpdatedActionTests : FreeCoroutineSpec() {
    init {
        val repository = mockk<TimeEntryRepository>()
        val workspace = mockk<Workspace>()
        coEvery { workspace.id } returns 1
        val timeEntries = mapOf(
            1L to createTimeEntry(1, "first"),
            2L to createTimeEntry(2, "second"),
            3L to createTimeEntry(2, "third")
        )
        val editableTimeEntry = EditableTimeEntry.empty(workspace.id)
        val initState = StartEditState(timeEntries, mapOf(1L to workspace), editableTimeEntry)
        val updated = timeEntries[2L]!!.copy(description = "second updated")
        val reducer = StartEditReducer(repository, dispatcherProvider)
        coEvery { workspace.id } returns 1

        "The TimeEntryUpdated action" - {
            reducer.testReduce(
                initialState = initState,
                action = StartEditAction.TimeEntryUpdated(2, updated)
            ) { state, effect ->
                "should update updated time entry" {
                    state.timeEntries.shouldContain(2L to updated)
                }
                "shouldn't change any other time entry than the updated one" {
                    state.timeEntries.filterKeys { key -> key != 2L } shouldBe initState.timeEntries.filterKeys { key -> key != 2L }
                }
                "shouldn't return any effect" {
                    effect.shouldBeEmpty()
                }
            }
        }
    }
}