package com.toggl.timer.common.domain

import com.toggl.architecture.core.Effect
import com.toggl.repository.timeentry.StartTimeEntryResult
import com.toggl.repository.timeentry.TimeEntryRepository

class StartTimeEntryEffect<Action>(
    private val repository: TimeEntryRepository,
    private val description: String,
    private val mapFn: (StartTimeEntryResult) -> Action
) : Effect<Action> {
    override suspend fun execute(): Action? =
        repository
            .startTimeEntry(description)
            .run(mapFn)
}
