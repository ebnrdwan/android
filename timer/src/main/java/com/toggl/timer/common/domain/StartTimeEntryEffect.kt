package com.toggl.timer.common.domain

import com.toggl.architecture.core.Effect
import com.toggl.repository.interfaces.StartTimeEntryResult
import com.toggl.repository.interfaces.TimeEntryRepository

class StartTimeEntryEffect<Action>(
    private val repository: TimeEntryRepository,
    private val timeEntry: EditableTimeEntry,
    private val mapFn: (StartTimeEntryResult) -> Action
) : Effect<Action> {
    override suspend fun execute(): Action? =
        repository
            .startTimeEntry(timeEntry.workspaceId, timeEntry.description)
            .run(mapFn)
}
