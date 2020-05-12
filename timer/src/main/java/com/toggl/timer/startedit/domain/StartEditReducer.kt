package com.toggl.timer.startedit.domain

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.MutableValue
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.extensions.effect
import com.toggl.architecture.extensions.noEffect
import com.toggl.common.Constants.TimeEntry.maxDurationInHours
import com.toggl.common.feature.extensions.mutateWithoutEffects
import com.toggl.common.feature.extensions.returnEffect
import com.toggl.environment.services.time.TimeService
import com.toggl.models.common.AutocompleteSuggestion
import com.toggl.models.domain.TimeEntry
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.domain.SaveTimeEntryEffect
import com.toggl.timer.common.domain.StartTimeEntryEffect
import com.toggl.timer.common.domain.extensions.isNew
import com.toggl.timer.common.domain.extensions.isRunning
import com.toggl.timer.common.domain.extensions.isRunningOrNew
import com.toggl.timer.common.domain.extensions.isStopped
import com.toggl.timer.common.domain.handleTimeEntryCreationStateChanges
import com.toggl.timer.exceptions.EditableTimeEntryDoesNotHaveADurationSetException
import com.toggl.timer.exceptions.EditableTimeEntryDoesNotHaveAStartTimeSetException
import com.toggl.timer.exceptions.EditableTimeEntryShouldNotBeNullException
import com.toggl.timer.extensions.absoluteDurationBetween
import com.toggl.timer.extensions.replaceTimeEntryWithId
import com.toggl.timer.startedit.domain.TemporalInconsistency.DurationTooLong
import com.toggl.timer.startedit.domain.TemporalInconsistency.StartTimeAfterCurrentTime
import com.toggl.timer.startedit.domain.TemporalInconsistency.StartTimeAfterStopTime
import com.toggl.timer.startedit.domain.TemporalInconsistency.StopTimeBeforeStartTime
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

class StartEditReducer @Inject constructor(
    private val repository: TimeEntryRepository,
    private val timeService: TimeService,
    private val dispatcherProvider: DispatcherProvider
) : Reducer<StartEditState, StartEditAction> {

    override fun reduce(
        state: MutableValue<StartEditState>,
        action: StartEditAction
    ): List<Effect<StartEditAction>> {
        return when (action) {
            StartEditAction.CloseButtonTapped, StartEditAction.DialogDismissed ->
                state.mutateWithoutEffects { copy(editableTimeEntry = null) }
            is StartEditAction.DescriptionEntered ->
                state.mutate {
                    editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(description = action.description)
                    }
                } returnEffect updateAutocompleteSuggestions(action, state())
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
            StartEditAction.AddProjectChipTapped,
            StartEditAction.ProjectButtonTapped ->
                state.mutateWithoutEffects {
                    editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(description = if (it.description.isEmpty()) "@" else it.description + " @")
                    }
                }
            StartEditAction.AddTagChipTapped,
            StartEditAction.TagButtonTapped ->
                state.mutateWithoutEffects {
                    editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(description = if (it.description.isEmpty()) "#" else it.description + " #")
                    }
                }
            is StartEditAction.PickerTapped ->
                state.mutateWithoutEffects {
                    copy(dateTimePickMode = action.pickerMode, temporalInconsistency = TemporalInconsistency.None)
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
            is StartEditAction.AutocompleteSuggestionTapped -> {
                state.mutateWithoutEffects {
                    when (action.autocompleteSuggestion) {
                        is AutocompleteSuggestion.TimeEntry -> modifyWithTimeEntrySuggestion(action.autocompleteSuggestion)
                        else -> this
                    }
                }
            }
            StartEditAction.DateTimePickingCancelled -> {
                state.mutateWithoutEffects { copy(dateTimePickMode = DateTimePickMode.None) }
            }
            is StartEditAction.DateTimePicked -> {
                val mode = state().dateTimePickMode
                if (mode == DateTimePickMode.None) {
                    noEffect()
                } else {
                    val editableTimeEntry = state().editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                    val temporalInconsistency = detectTemporalInconsistencies(editableTimeEntry, mode, action.dateTime)
                    if (temporalInconsistency == TemporalInconsistency.None) {
                        state.mutateWithoutEffects {
                            when (mode) {
                                DateTimePickMode.StartTime, DateTimePickMode.StartDate ->
                                    handleStartTimeEdition(editableTimeEntry, action)
                                DateTimePickMode.EndTime, DateTimePickMode.EndDate ->
                                    handleEndTimeEdition(editableTimeEntry, action)
                                else -> this
                            }
                        }
                    } else {
                        state.mutate {
                            copy(dateTimePickMode = DateTimePickMode.None, temporalInconsistency = temporalInconsistency)
                        }
                        effect(ReopenPickerEffect(mode))
                    }
                }
            }
            is StartEditAction.DurationInputted -> {
                val maxDuration = Duration.ofHours(maxDurationInHours)
                if (action.duration < Duration.ZERO || action.duration > maxDuration)
                    return noEffect()

                state.mutateWithoutEffects {
                    val editableTimeEntry = state().editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()

                    StartEditState.editableTimeEntry.modify(this) {
                        if (editableTimeEntry.isRunningOrNew()) it.copy(startTime = timeService.now() - action.duration)
                        else it.copy(duration = action.duration)
                    }
                }
            }
            is StartEditAction.WheelChangedStartTime -> {
                val editableTimeEntry = state().editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                val maxDuration = Duration.ofHours(maxDurationInHours)
                val now = timeService.now()
                val endTime = editableTimeEntry.endTime(now)
                val newDuration = action.startTime.absoluteDurationBetween(endTime)
                if (action.startTime > endTime || newDuration > maxDuration)
                    return noEffect()

                state.mutateWithoutEffects {
                    StartEditState.editableTimeEntry.modify(this) {
                        if (editableTimeEntry.isRunningOrNew())
                            it.copy(startTime = action.startTime)
                        else
                            it.copy(startTime = action.startTime, duration = newDuration)
                    }
                }
            }
            is StartEditAction.WheelChangedEndTime -> {
                val maxDuration = Duration.ofHours(maxDurationInHours)
                if (action.duration < Duration.ZERO || action.duration > maxDuration)
                    return noEffect()

                state.mutateWithoutEffects {
                    val editableTimeEntry = state().editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                    if (editableTimeEntry.isNew()) throw EditableTimeEntryDoesNotHaveAStartTimeSetException()
                    if (editableTimeEntry.isRunning()) throw EditableTimeEntryDoesNotHaveADurationSetException()

                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(duration = action.duration)
                    }
                }
            }
            is StartEditAction.WheelOffsettedStartTime -> {
                val editableTimeEntry = state().editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                if (editableTimeEntry.isRunningOrNew())
                    return noEffect()

                state.mutateWithoutEffects {
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(startTime = action.startTime)
                    }
                }
            }
            StartEditAction.StopButtonTapped -> {
                val editableTimeEntry = state().editableTimeEntry ?: throw EditableTimeEntryShouldNotBeNullException()
                if (editableTimeEntry.isStopped())
                    return noEffect()

                val startTime = editableTimeEntry.startTime
                val now = timeService.now()

                if (startTime == null)
                    return state.mutateWithoutEffects {
                        StartEditState.editableTimeEntry.modify(this) {
                            it.copy(
                                startTime = now,
                                duration = Duration.ZERO
                            )
                        }
                    }

                val maxDuration = Duration.ofHours(maxDurationInHours)
                val durationUntilNow = startTime.absoluteDurationBetween(now)
                if (startTime > now || durationUntilNow > maxDuration)
                    return noEffect()

                state.mutateWithoutEffects {
                    StartEditState.editableTimeEntry.modify(this) {
                        it.copy(duration = durationUntilNow)
                    }
                }
            }
        }
    }

    private fun StartEditState.modifyWithTimeEntrySuggestion(autocompleteSuggestion: AutocompleteSuggestion.TimeEntry): StartEditState =
        StartEditState.editableTimeEntry.modify(this) {
            val timeEntrySuggestion = autocompleteSuggestion.timeEntry
            it.copy(
                workspaceId = timeEntrySuggestion.workspaceId,
                description = timeEntrySuggestion.description,
                projectId = timeEntrySuggestion.projectId,
                tagIds = timeEntrySuggestion.tagIds,
                billable = timeEntrySuggestion.billable
            )
        }

    private fun StartEditState.handleEndTimeEdition(
        editableTimeEntry: EditableTimeEntry,
        action: StartEditAction.DateTimePicked
    ): StartEditState {
        val startTime = editableTimeEntry.startTime ?: throw EditableTimeEntryDoesNotHaveAStartTimeSetException()
        return copy(
            dateTimePickMode = DateTimePickMode.None,
            temporalInconsistency = TemporalInconsistency.None,
            editableTimeEntry = editableTimeEntry.copy(
                duration = startTime.absoluteDurationBetween(action.dateTime)
            )
        )
    }

    private fun StartEditState.handleStartTimeEdition(
        editableTimeEntry: EditableTimeEntry,
        action: StartEditAction.DateTimePicked
    ): StartEditState {
        val originalDuration = editableTimeEntry.duration
        return if (originalDuration == null) {
            copy(
                dateTimePickMode = DateTimePickMode.None,
                temporalInconsistency = TemporalInconsistency.None,
                editableTimeEntry = editableTimeEntry.copy(
                    startTime = action.dateTime
                )
            )
        } else {
            val originalStartTime = editableTimeEntry.startTime ?: throw EditableTimeEntryDoesNotHaveAStartTimeSetException()
            copy(
                dateTimePickMode = DateTimePickMode.None,
                temporalInconsistency = TemporalInconsistency.None,
                editableTimeEntry = editableTimeEntry.copy(
                    startTime = action.dateTime,
                    duration = action.dateTime.absoluteDurationBetween(originalStartTime + originalDuration)
                )
            )
        }
    }

    private fun detectTemporalInconsistencies(
        editableTimeEntry: EditableTimeEntry,
        mode: DateTimePickMode,
        newDateTime: OffsetDateTime
    ): TemporalInconsistency {
        val maxDuration = Duration.ofHours(999)
        val now = timeService.now()
        val startTime = editableTimeEntry.startTime
        val duration = editableTimeEntry.duration
        val endTime = (startTime ?: now) + (duration ?: Duration.ZERO)

        return when {
            startTime == null -> detectTemporalInconsistenciesOnEmptyTimeEntry(mode, newDateTime, now, maxDuration)
            duration == null -> detectTemporalInconsistenciesOnRunningTimeEntry(mode, newDateTime, now, maxDuration)
            else -> detectTemporalInconsistenciesOnStoppedTimeEntry(mode, startTime, endTime, newDateTime, maxDuration)
        }
    }

    private fun detectTemporalInconsistenciesOnStoppedTimeEntry(
        mode: DateTimePickMode,
        startTime: OffsetDateTime,
        endTime: OffsetDateTime,
        newDateTime: OffsetDateTime,
        maxDuration: Duration
    ): TemporalInconsistency = when {
        mode.targetsStart() && newDateTime > endTime -> StartTimeAfterStopTime
        mode.targetsStart() && endTime.absoluteDurationBetween(newDateTime) > maxDuration -> DurationTooLong
        mode.targetsEnd() && newDateTime < startTime -> StopTimeBeforeStartTime
        mode.targetsEnd() && newDateTime.absoluteDurationBetween(startTime) > maxDuration -> DurationTooLong
        else -> TemporalInconsistency.None
    }

    private fun detectTemporalInconsistenciesOnRunningTimeEntry(
        mode: DateTimePickMode,
        newDateTime: OffsetDateTime,
        now: OffsetDateTime,
        maxDuration: Duration
    ): TemporalInconsistency = when {
        mode.targetsStart() && now.absoluteDurationBetween(newDateTime) > maxDuration -> DurationTooLong
        mode.targetsStart() && newDateTime > now -> StartTimeAfterCurrentTime
        mode.targetsEnd() -> throw EditableTimeEntryDoesNotHaveADurationSetException()
        else -> TemporalInconsistency.None
    }

    private fun detectTemporalInconsistenciesOnEmptyTimeEntry(
        mode: DateTimePickMode,
        newDateTime: OffsetDateTime,
        now: OffsetDateTime,
        maxDuration: Duration
    ): TemporalInconsistency = when {
        mode.targetsStart() && now.absoluteDurationBetween(newDateTime) > maxDuration -> DurationTooLong
        mode.targetsEnd() -> throw EditableTimeEntryDoesNotHaveAStartTimeSetException()
        else -> TemporalInconsistency.None
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

    private fun updateAutocompleteSuggestions(
        action: StartEditAction.DescriptionEntered,
        state: StartEditState
    ): List<Effect<StartEditAction>> =
        effect(
            UpdateAutocompleteSuggestionsEffect(
                dispatcherProvider,
                action.description,
                action.cursorPosition,
                state.tags,
                state.tasks,
                state.clients,
                state.projects,
                state.timeEntries
            )
        )

    private fun EditableTimeEntry.shouldStart() = this.ids.isEmpty()
    private fun EditableTimeEntry.endTime(now: OffsetDateTime): OffsetDateTime {
        if (startTime == null)
            return now

        val relativeDuration = duration ?: startTime.absoluteDurationBetween(now)
        return startTime + relativeDuration
    }

    private fun DateTimePickMode.targetsStart() = this == DateTimePickMode.StartDate || this == DateTimePickMode.StartTime
    private fun DateTimePickMode.targetsEnd() = this == DateTimePickMode.EndDate || this == DateTimePickMode.EndTime
}
