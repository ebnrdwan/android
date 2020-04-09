package com.toggl.timer.startedit.domain

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.SettableValue
import com.toggl.architecture.extensions.effect
import com.toggl.architecture.extensions.noEffect
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.StopTimeEntryEffect
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.extensions.replaceTimeEntryWithId
import javax.inject.Inject

class StartTimeEntryReducer @Inject constructor(
    private val repository: TimeEntryRepository,
    private val dispatcherProvider: DispatcherProvider
) : Reducer<StartTimeEntryState, StartTimeEntryAction> {

    override fun reduce(
        state: SettableValue<StartTimeEntryState>,
        action: StartTimeEntryAction
    ): List<Effect<StartTimeEntryAction>> =
        when (action) {
            StartTimeEntryAction.StopTimeEntryButtonTapped -> stopTimeEntry(repository)
            StartTimeEntryAction.CloseButtonTapped, StartTimeEntryAction.DialogDismissed -> {
                state.value = state.value.copy(editableTimeEntry = null)
                noEffect()
            }
            is StartTimeEntryAction.DescriptionEntered -> {
                state.value = StartTimeEntryState.editableTimeEntry.modify(state.value) {
                    it.copy(description = action.description)
                }
                noEffect()
            }
            is StartTimeEntryAction.TimeEntryUpdated -> {
                val newTimeEntries = state.value.timeEntries
                    .replaceTimeEntryWithId(action.id, action.timeEntry)
                state.value = state.value.copy(
                    timeEntries = newTimeEntries,
                    editableTimeEntry = null
                )
                noEffect()
            }
            is StartTimeEntryAction.TimeEntryStarted -> {
                state.value = state.value.copy(
                    timeEntries = handleTimeEntryCreationStateChanges(
                        state.value.timeEntries,
                        action.startedTimeEntry,
                        action.stoppedTimeEntry
                    ),
                    editableTimeEntry = null
                )
                noEffect()
            }
            StartTimeEntryAction.ToggleBillable -> {
                state.value = StartTimeEntryState.editableTimeEntry.modify(state.value) {
                    it.copy(billable = !it.billable)
                }

                noEffect()
            }
            StartTimeEntryAction.DoneButtonTapped -> {
                val timeEntry = state.value.editableTimeEntry
                state.value = state.value.copy(editableTimeEntry = null)
                startTimeEntry(timeEntry!!, repository)
            }
        }

    private fun startTimeEntry(editableTimeEntry: EditableTimeEntry, repository: TimeEntryRepository) =
        effect(
            StartTimeEntryEffect(repository, editableTimeEntry, dispatcherProvider) {
                StartTimeEntryAction.TimeEntryStarted(it.startedTimeEntry, it.stoppedTimeEntry)
            }
        )

    private fun stopTimeEntry(repository: TimeEntryRepository) =
        effect(
            StopTimeEntryEffect(repository, dispatcherProvider) {
                StartTimeEntryAction.TimeEntryUpdated(
                    it.id,
                    it
                )
            }
        )
}
