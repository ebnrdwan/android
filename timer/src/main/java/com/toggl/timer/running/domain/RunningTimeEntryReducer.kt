package com.toggl.timer.running.domain

import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.SettableValue
import com.toggl.architecture.extensions.effect
import com.toggl.architecture.extensions.noEffect
import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.StopTimeEntryEffect
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.extensions.replaceTimeEntryWithId
import com.toggl.timer.extensions.runningTimeEntryOrNull
import javax.inject.Inject

class RunningTimeEntryReducer @Inject constructor(
    private val repository: TimeEntryRepository
) : Reducer<RunningTimeEntryState, RunningTimeEntryAction> {

    override fun reduce(
        state: SettableValue<RunningTimeEntryState>,
        action: RunningTimeEntryAction
    ): List<Effect<RunningTimeEntryAction>> =
        when (action) {
            RunningTimeEntryAction.StopButtonTapped -> stopTimeEntry(repository)
            RunningTimeEntryAction.DescriptionTextFieldTapped -> {
                state.value = state.value.copy(editableTimeEntry = EditableTimeEntry.empty(
                    Workspace(1, "Auto created workspace", listOf(WorkspaceFeature.Pro)).id)
                )
                noEffect()
            }
            RunningTimeEntryAction.RunningTimeEntryTapped -> {
                state.value.timeEntries.runningTimeEntryOrNull()?.let { runningTimeEntry ->
                    state.value = state.value.copy(editableTimeEntry = EditableTimeEntry.fromSingle(runningTimeEntry))
                }
                noEffect()
            }
            is RunningTimeEntryAction.TimeEntryUpdated -> {
                val newTimeEntries = state.value.timeEntries
                    .replaceTimeEntryWithId(action.id, action.timeEntry)
                state.value = state.value.copy(timeEntries = newTimeEntries)
                noEffect()
            }
            is RunningTimeEntryAction.TimeEntryStarted -> {
                state.value = state.value.copy(
                    timeEntries = handleTimeEntryCreationStateChanges(
                        state.value.timeEntries,
                        action.startedTimeEntry,
                        action.stoppedTimeEntry
                    )
                )
                noEffect()
            }
        }

    private fun startTimeEntry(editableTimeEntry: EditableTimeEntry, repository: TimeEntryRepository) =
        effect(
            StartTimeEntryEffect(repository, editableTimeEntry) {
                RunningTimeEntryAction.TimeEntryStarted(it.startedTimeEntry, it.stoppedTimeEntry)
            }
        )

    private fun stopTimeEntry(repository: TimeEntryRepository) =
        effect(
            StopTimeEntryEffect(repository) { stoppedTimeEntry ->
                    RunningTimeEntryAction.TimeEntryUpdated(
                        stoppedTimeEntry.id,
                        stoppedTimeEntry
                    )
            }
        )
}