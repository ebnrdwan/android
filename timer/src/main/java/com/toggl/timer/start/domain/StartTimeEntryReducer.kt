package com.toggl.timer.start.domain

import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.SettableValue
import com.toggl.architecture.extensions.effect
import com.toggl.architecture.extensions.noEffect
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.extensions.replaceTimeEntryWithId
import javax.inject.Inject

class StartTimeEntryReducer @Inject constructor(
    private val repository: TimeEntryRepository
) : Reducer<StartTimeEntryState, StartTimeEntryAction> {

    override fun reduce(
        state: SettableValue<StartTimeEntryState>,
        action: StartTimeEntryAction
    ): List<Effect<StartTimeEntryAction>> =
        when (action) {
            StartTimeEntryAction.StopTimeEntryButtonTapped ->
                effect(StopTimeEntryEffect(repository))
            StartTimeEntryAction.StartTimeEntryButtonTapped -> {
                val editableTimeEntry = state.value.editableTimeEntry
                val workspace = state.value.workspaces.values.single()
                state.value = state.value.copy(editableTimeEntry = EditableTimeEntry.empty(workspace.id))
                startTimeEntry(editableTimeEntry, repository)
            }
            is StartTimeEntryAction.TimeEntryDescriptionChanged -> {
                state.value = StartTimeEntryState.editableTimeEntry.modify(state.value) {
                    it.copy(description = action.description)
                }
                noEffect()
            }
            is StartTimeEntryAction.TimeEntryUpdated -> {
                val newTimeEntries = state.value.timeEntries
                    .replaceTimeEntryWithId(action.id, action.timeEntry)
                state.value = state.value.copy(timeEntries = newTimeEntries)
                noEffect()
            }
            is StartTimeEntryAction.TimeEntryStarted -> {
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
                StartTimeEntryAction.TimeEntryStarted(it.startedTimeEntry, it.stoppedTimeEntry)
            }
        )
}
