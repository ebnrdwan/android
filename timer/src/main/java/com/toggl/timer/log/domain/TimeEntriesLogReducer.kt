package com.toggl.timer.log.domain

import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.SettableValue
import com.toggl.architecture.extensions.effect
import com.toggl.architecture.extensions.noEffect
import com.toggl.models.common.SwipeDirection
import com.toggl.models.domain.TimeEntry
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.DeleteTimeEntryEffect
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.common.domain.handleTimeEntryDeletionStateChanges
import com.toggl.timer.extensions.containsExactly
import java.lang.IllegalStateException
import javax.inject.Inject

class TimeEntriesLogReducer @Inject constructor(private val repository: TimeEntryRepository)
    : Reducer<TimeEntriesLogState, TimeEntriesLogAction> {

    override fun reduce(
        state: SettableValue<TimeEntriesLogState>,
        action: TimeEntriesLogAction
    ): List<Effect<TimeEntriesLogAction>> =
            when (action) {
                is TimeEntriesLogAction.ContinueButtonTapped -> {
                    val timeEntryToContinue = state.value.timeEntries[action.id]
                        ?.let(EditableTimeEntry.Companion::fromSingle)
                        ?: throw IllegalStateException()

                    startTimeEntry(timeEntryToContinue, repository)
                }
                is TimeEntriesLogAction.TimeEntryTapped -> {
                    val entryToEdit = state.value.timeEntries[action.id]
                        ?.run(EditableTimeEntry.Companion::fromSingle)
                        ?: throw IllegalStateException()

                    state.value = state.value.copy(editableTimeEntry = entryToEdit)
                    noEffect()
                }

                is TimeEntriesLogAction.TimeEntryGroupTapped -> {
                    val entryToEdit = state.value.timeEntries[action.ids.first()]
                        ?.run { EditableTimeEntry.fromGroup(action.ids, this) }
                        ?: throw IllegalStateException()

                    state.value = state.value.copy(editableTimeEntry = entryToEdit)
                    noEffect()
                }

                is TimeEntriesLogAction.TimeEntrySwiped -> {
                    val swipedEntry = state.value.timeEntries[action.id]
                        ?: throw IllegalStateException()

                    when (action.direction) {
                        SwipeDirection.Left -> effect(delete(swipedEntry, repository))
                        SwipeDirection.Right ->
                            startTimeEntry(EditableTimeEntry.fromSingle(swipedEntry), repository)
                    }
                }

                is TimeEntriesLogAction.TimeEntryGroupSwiped -> {

                    when (action.direction) {
                        SwipeDirection.Left -> action.ids
                            .map { state.value.timeEntries[it] ?: throw IllegalStateException() }
                            .map { delete(it, repository) }
                        SwipeDirection.Right -> {
                            val timeEntryToStart = state.value.timeEntries[action.ids.first()]
                                ?.let { EditableTimeEntry.fromGroup(action.ids, it) }
                                ?: throw IllegalStateException()
                            startTimeEntry(timeEntryToStart, repository)
                        }
                    }
                }

                is TimeEntriesLogAction.TimeEntryStarted -> {
                    state.value = state.value.copy(
                        timeEntries = handleTimeEntryCreationStateChanges(
                            state.value.timeEntries,
                            action.startedTimeEntry,
                            action.stoppedTimeEntry
                        )
                    )
                    noEffect()
                }

                is TimeEntriesLogAction.TimeEntryDeleted -> {
                    state.value = state.value.copy(
                        timeEntries = handleTimeEntryDeletionStateChanges(
                            state.value.timeEntries,
                            action.deletedTimeEntry
                        )
                    )
                    noEffect()
                }
                is TimeEntriesLogAction.ToggleTimeEntryGroupTapped -> {
                    val newUngroupedTimeEntries =
                        if (state.value.expandedGroupIds.contains(action.groupId)) state.value.expandedGroupIds - action.groupId
                        else state.value.expandedGroupIds + action.groupId
                    state.value = state.value.copy(expandedGroupIds = newUngroupedTimeEntries)
                    noEffect()
                }
                is TimeEntriesLogAction.CommitDeletion -> {
                    val timeEntryIdsToDelete =
                        if(state.value.entriesPendingDeletion.containsExactly(action.ids)) action.ids
                        else listOf()

                    if (timeEntryIdsToDelete.none()){
                        noEffect()
                    } else {
                        val timeEntriesToDelete = timeEntryIdsToDelete.mapNotNull { state.value.timeEntries[it] }
                        state.value = state.value.copy(entriesPendingDeletion = setOf())
                        timeEntriesToDelete.map { delete(it, repository) }
                    }
                }
            }

    private fun startTimeEntry(timeEntry: EditableTimeEntry, repository: TimeEntryRepository) =
        effect(
            StartTimeEntryEffect(repository, timeEntry) {
                TimeEntriesLogAction.TimeEntryStarted(it.startedTimeEntry, it.stoppedTimeEntry)
            }
        )

    private fun delete(timeEntry: TimeEntry, repository: TimeEntryRepository) =
        DeleteTimeEntryEffect(repository, timeEntry, TimeEntriesLogAction::TimeEntryDeleted)
}
