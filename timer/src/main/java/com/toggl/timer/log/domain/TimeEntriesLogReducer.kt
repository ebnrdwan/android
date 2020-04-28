package com.toggl.timer.log.domain

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.MutableValue
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.extensions.effect
import com.toggl.architecture.extensions.noEffect
import com.toggl.common.feature.extensions.mutateWithoutEffects
import com.toggl.models.common.SwipeDirection
import com.toggl.models.domain.TimeEntry
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.DeleteTimeEntryEffect
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.common.domain.handleTimeEntryDeletionStateChanges
import com.toggl.timer.exceptions.TimeEntryDoesNotExistException
import com.toggl.timer.extensions.containsExactly
import javax.inject.Inject

class TimeEntriesLogReducer @Inject constructor(
    private val repository: TimeEntryRepository,
    private val dispatcherProvider: DispatcherProvider
) : Reducer<TimeEntriesLogState, TimeEntriesLogAction> {

    override fun reduce(
        state: MutableValue<TimeEntriesLogState>,
        action: TimeEntriesLogAction
    ): List<Effect<TimeEntriesLogAction>> =
        when (action) {
            is TimeEntriesLogAction.ContinueButtonTapped -> {
                val timeEntryToContinue = state().timeEntries[action.id]
                    ?.let(EditableTimeEntry.Companion::fromSingle)
                    ?: throw TimeEntryDoesNotExistException()

                startTimeEntry(timeEntryToContinue, repository)
            }
            is TimeEntriesLogAction.TimeEntryTapped ->
                state.mutateWithoutEffects {
                    val entryToEdit = timeEntries[action.id]
                        ?.run(EditableTimeEntry.Companion::fromSingle)
                        ?: throw TimeEntryDoesNotExistException()

                    copy(editableTimeEntry = entryToEdit)
                }

            is TimeEntriesLogAction.TimeEntryGroupTapped ->
                state.mutateWithoutEffects {
                    val entryToEdit = EditableTimeEntry.fromGroup(state().getAllTimeEntriesWithIds(action.ids))

                    copy(editableTimeEntry = entryToEdit)
                }

            is TimeEntriesLogAction.TimeEntrySwiped -> {
                val swipedEntry = state().timeEntries[action.id]
                    ?: throw TimeEntryDoesNotExistException()

                when (action.direction) {
                    SwipeDirection.Left ->
                        handleDeletingSwipe(state, listOf(swipedEntry.id))
                    SwipeDirection.Right ->
                        startTimeEntry(EditableTimeEntry.fromSingle(swipedEntry), repository)
                }
            }

            is TimeEntriesLogAction.TimeEntryGroupSwiped -> {

                when (action.direction) {
                    SwipeDirection.Left ->
                        handleDeletingSwipe(state, action.ids)
                    SwipeDirection.Right -> {
                        val timeEntryToStart = EditableTimeEntry.fromGroup(state().getAllTimeEntriesWithIds(action.ids))

                        startTimeEntry(timeEntryToStart, repository)
                    }
                }
            }

            is TimeEntriesLogAction.TimeEntryStarted ->
                state.mutateWithoutEffects {
                    copy(
                        timeEntries = handleTimeEntryCreationStateChanges(
                            timeEntries,
                            action.startedTimeEntry,
                            action.stoppedTimeEntry
                        )
                    )
                }

            is TimeEntriesLogAction.TimeEntryDeleted ->
                state.mutateWithoutEffects {
                    copy(
                        timeEntries = handleTimeEntryDeletionStateChanges(
                            timeEntries,
                            action.deletedTimeEntry
                        )
                    )
                }

            is TimeEntriesLogAction.ToggleTimeEntryGroupTapped ->
                state.mutateWithoutEffects {
                    val newUngroupedTimeEntries =
                        if (expandedGroupIds.contains(action.groupId)) expandedGroupIds - action.groupId
                        else expandedGroupIds + action.groupId
                    copy(expandedGroupIds = newUngroupedTimeEntries)
                }
            is TimeEntriesLogAction.CommitDeletion -> {
                val currentState = state()
                val timeEntryIdsToDelete =
                    if (currentState.entriesPendingDeletion.containsExactly(action.ids)) action.ids
                    else listOf()

                if (timeEntryIdsToDelete.none()) {
                    noEffect()
                } else {

                    state.mutate {
                        val updatedTimeEntries = markDeletedTimeEntries(timeEntries, timeEntryIdsToDelete)
                        copy(timeEntries = updatedTimeEntries, entriesPendingDeletion = setOf())
                    }
                    timeEntryIdsToDelete
                        .mapNotNull { currentState.timeEntries[it] }
                        .map(::delete)
                }
            }
            is TimeEntriesLogAction.UndoButtonTapped ->
                state.mutateWithoutEffects { copy(entriesPendingDeletion = emptySet()) }
        }

    private fun startTimeEntry(timeEntry: EditableTimeEntry, repository: TimeEntryRepository) =
        effect(
            StartTimeEntryEffect(repository, timeEntry, dispatcherProvider) {
                TimeEntriesLogAction.TimeEntryStarted(it.startedTimeEntry, it.stoppedTimeEntry)
            }
        )

    private fun delete(timeEntry: TimeEntry) =
        DeleteTimeEntryEffect(repository, timeEntry, dispatcherProvider, TimeEntriesLogAction::TimeEntryDeleted)

    private fun handleDeletingSwipe(
        state: MutableValue<TimeEntriesLogState>,
        entriesToDelete: List<Long>
    ): List<Effect<TimeEntriesLogAction>> {

        val entriesToCommitDeletion = state().entriesPendingDeletion

        state.mutate {
            val updatedEntries = markDeletedTimeEntries(timeEntries, entriesToCommitDeletion)
            copy(timeEntries = updatedEntries, entriesPendingDeletion = entriesToDelete.toSet())
        }

        val deleteEffects = prepareDeleteEffects(state, entriesToCommitDeletion)
        deleteEffects.add(WaitForUndoEffect(entriesToDelete))

        return deleteEffects
    }

    private fun prepareDeleteEffects(
        state: MutableValue<TimeEntriesLogState>,
        idsToDelete: Collection<Long>
    ): MutableList<Effect<TimeEntriesLogAction>> =
        idsToDelete
            .mapNotNull { state().timeEntries[it] }
            .map(::delete)
            .toMutableList()

    private fun markDeletedTimeEntries(
        timeEntries: Map<Long, TimeEntry>,
        deletedTimeEntryIds: Collection<Long>
    ): Map<Long, TimeEntry> =
        timeEntries.map {
            if (deletedTimeEntryIds.contains(it.key)) it.key to it.value.copy(isDeleted = true)
            else it.key to it.value
        }.toMap()

    private fun TimeEntriesLogState.getAllTimeEntriesWithIds(ids: List<Long>): Collection<TimeEntry> {
        return if (this.timeEntries.keys.containsAll(ids)) this.timeEntries.filterKeys { it in ids }.values
        else throw TimeEntryDoesNotExistException()
    }
}
