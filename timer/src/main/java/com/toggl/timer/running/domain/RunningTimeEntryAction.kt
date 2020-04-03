package com.toggl.timer.running.domain

import arrow.optics.optics
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.domain.TimerAction

@optics
sealed class RunningTimeEntryAction {
    object StopButtonTapped : RunningTimeEntryAction()
    object DescriptionTextFieldTapped : RunningTimeEntryAction()
    object RunningTimeEntryTapped : RunningTimeEntryAction()
    data class TimeEntryUpdated(val id: Long, val timeEntry: TimeEntry) : RunningTimeEntryAction()
    data class TimeEntryStarted(val startedTimeEntry: TimeEntry, val stoppedTimeEntry: TimeEntry?) : RunningTimeEntryAction()

    companion object {
        fun fromTimerAction(timerAction: TimerAction): RunningTimeEntryAction? =
            if (timerAction !is TimerAction.RunningTimeEntry) null
            else timerAction.runningTimeEntryAction

        fun toTimerAction(timeEntriesLogAction: RunningTimeEntryAction): TimerAction =
            TimerAction.RunningTimeEntry(
                timeEntriesLogAction
            )
    }
}

fun RunningTimeEntryAction.formatForDebug() =
    when (this) {
        RunningTimeEntryAction.StopButtonTapped -> "Stop time entry button tapped"
        RunningTimeEntryAction.DescriptionTextFieldTapped -> "Description field tapped"
        RunningTimeEntryAction.RunningTimeEntryTapped -> "Running time entry tapped"
        is RunningTimeEntryAction.TimeEntryUpdated -> "Time entry with id $id updated"
        is RunningTimeEntryAction.TimeEntryStarted -> "Time entry started with id $startedTimeEntry.id"
    }