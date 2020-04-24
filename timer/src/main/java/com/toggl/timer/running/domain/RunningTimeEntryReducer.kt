package com.toggl.timer.running.domain

import com.toggl.common.feature.extensions.mutateWithoutEffects
import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.MutableValue
import com.toggl.architecture.extensions.effect
import com.toggl.environment.services.time.TimeService
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.StopTimeEntryEffect
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.extensions.replaceTimeEntryWithId
import com.toggl.timer.extensions.runningTimeEntryOrNull
import javax.inject.Inject

class RunningTimeEntryReducer @Inject constructor(
    private val repository: TimeEntryRepository,
    private val dispatcherProvider: DispatcherProvider
) : Reducer<RunningTimeEntryState, RunningTimeEntryAction> {

    @Inject
    lateinit var timeService: TimeService

    override fun reduce(
        state: MutableValue<RunningTimeEntryState>,
        action: RunningTimeEntryAction
    ): List<Effect<RunningTimeEntryAction>> =
        when (action) {
            RunningTimeEntryAction.StartButtonTapped -> startTimeEntry(EditableTimeEntry.empty(1), repository)
            RunningTimeEntryAction.StopButtonTapped -> stopTimeEntry(repository)
            RunningTimeEntryAction.CardTapped ->
                state.mutateWithoutEffects {
                    val entryToOpen = timeEntries.runningTimeEntryOrNull()
                        ?.run(EditableTimeEntry.Companion::fromSingle)
                        ?: EditableTimeEntry.withStartTime(defaultWorkspaceId(), timeService.now())

                    copy(editableTimeEntry = entryToOpen)
                }
            is RunningTimeEntryAction.TimeEntryUpdated ->
                state.mutateWithoutEffects {
                    val newTimeEntries = timeEntries.replaceTimeEntryWithId(action.id, action.timeEntry)
                    copy(timeEntries = newTimeEntries)
                }
            is RunningTimeEntryAction.TimeEntryStarted ->
                state.mutateWithoutEffects {
                    copy(
                        timeEntries = handleTimeEntryCreationStateChanges(
                            timeEntries,
                            action.startedTimeEntry,
                            action.stoppedTimeEntry
                        )
                    )
                }
        }

    private fun startTimeEntry(editableTimeEntry: EditableTimeEntry, repository: TimeEntryRepository) =
        effect(
            StartTimeEntryEffect(repository, editableTimeEntry, dispatcherProvider) {
                RunningTimeEntryAction.TimeEntryStarted(it.startedTimeEntry, it.stoppedTimeEntry)
            }
        )

    private fun stopTimeEntry(repository: TimeEntryRepository) =
        effect(
            StopTimeEntryEffect(repository, dispatcherProvider) { stoppedTimeEntry ->
                    RunningTimeEntryAction.TimeEntryUpdated(
                        stoppedTimeEntry.id,
                        stoppedTimeEntry
                    )
            }
        )

    private fun RunningTimeEntryState.defaultWorkspaceId() = 1L
}