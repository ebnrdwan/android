package com.toggl.timer.common.domain

import com.toggl.architecture.core.Effect
import com.toggl.models.domain.TimeEntry
import com.toggl.repository.interfaces.TimeEntryRepository

class StopTimeEntryEffect<Action>(
    private val repository: TimeEntryRepository,
    private val mapFn: (TimeEntry) -> Action
) : Effect<Action> {
    override suspend fun execute(): Action? =
        repository.stopRunningTimeEntry()
            ?.run(mapFn)
}
