package com.toggl.timer.startedit.domain

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.MutableValue
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.extensions.effect
import com.toggl.common.feature.extensions.mutateWithoutEffects
import com.toggl.models.domain.TimeEntry
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.SaveTimeEntryEffect
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.exceptions.EditableTimeEntryShouldNotBeNullException
import com.toggl.timer.extensions.replaceTimeEntryWithId
import javax.inject.Inject

class StartEditReducer @Inject constructor(
    private val repository: TimeEntryRepository,
    private val dispatcherProvider: DispatcherProvider
) : Reducer<StartEditState, StartEditAction> {

    override fun reduce(
        state: MutableValue<StartEditState>,
        action: StartEditAction
    ): List<Effect<StartEditAction>> =
        when (action) {
            StartEditAction.CloseButtonTapped, StartEditAction.DialogDismissed ->
                state.mutateWithoutEffects { copy(editableTimeEntry = null) }
            is StartEditAction.DescriptionEntered ->
                state.mutateWithoutEffects {
                    editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(description = action.description)
                    }
                }
            is StartEditAction.TimeEntryUpdated ->
                state.mutateWithoutEffects {
                    copy(
                        timeEntries = timeEntries.replaceTimeEntryWithId(action.id, action.timeEntry),
                        editableTimeEntry = null
                    )
                }
            is StartEditAction.TimeEntryStarted ->
                state.mutateWithoutEffects {
                    copy(
                        timeEntries = handleTimeEntryCreationStateChanges(
                            timeEntries,
                            action.startedTimeEntry,
                            action.stoppedTimeEntry
                        ),
                        editableTimeEntry = null
                    )
                }
            StartEditAction.BillableTapped ->
                state.mutateWithoutEffects {
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(billable = !it.billable)
                    }
                }
            StartEditAction.ProjectButtonTapped ->
                state.mutateWithoutEffects {
                    editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(description = if (it.description.isEmpty()) "@" else it.description + " @")
                    }
                }
            StartEditAction.DoneButtonTapped -> {
                val editableTimeEntry = state().editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                state.mutate { copy(editableTimeEntry = null) }
                if (editableTimeEntry.shouldStart()) {
                    startTimeEntry(editableTimeEntry)
                } else {
                    val timeEntriesToEdit = editableTimeEntry.ids.mapNotNull { state().timeEntries[it] }
                    timeEntriesToEdit.map {
                        saveTimeEntry(
                            it.copy(
                                description = editableTimeEntry.description,
                                billable = editableTimeEntry.billable
                            )
                        )
                    }
                }
            }
            is StartEditAction.AutocompleteSuggestionsUpdated ->
                state.mutateWithoutEffects { copy(autocompleteSuggestions = action.autocompleteSuggestions) }
        }

    private fun startTimeEntry(editableTimeEntry: EditableTimeEntry) =
        effect(
            StartTimeEntryEffect(repository, editableTimeEntry, dispatcherProvider) {
                StartEditAction.TimeEntryStarted(it.startedTimeEntry, it.stoppedTimeEntry)
            }
        )

    private fun saveTimeEntry(timeEntry: TimeEntry) =
        SaveTimeEntryEffect(repository, dispatcherProvider, timeEntry) { updatedTimeEntry ->
            StartEditAction.TimeEntryUpdated(updatedTimeEntry.id, updatedTimeEntry)
        }

    private fun EditableTimeEntry.shouldStart() = this.ids.isEmpty()
}
