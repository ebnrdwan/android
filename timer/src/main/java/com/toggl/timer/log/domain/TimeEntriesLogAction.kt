package com.toggl.timer.log.domain

import arrow.optics.optics
import com.toggl.models.common.SwipeDirection
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.common.domain.TimerAction

@optics
sealed class TimeEntriesLogAction {
    data class ContinueButtonTapped(val id: Long) : TimeEntriesLogAction()
    data class TimeEntryTapped(val id: Long) : TimeEntriesLogAction()
    data class TimeEntrySwiped(val id: Long, val direction: SwipeDirection) : TimeEntriesLogAction()
    data class TimeEntryGroupTapped(val ids: List<Long>) : TimeEntriesLogAction()
    data class TimeEntryGroupSwiped(val ids: List<Long>, val direction: SwipeDirection) : TimeEntriesLogAction()
    data class TimeEntryDeleted(val deletedTimeEntry: TimeEntry) : TimeEntriesLogAction()
    data class TimeEntryStarted(val startedTimeEntry: TimeEntry, val stoppedTimeEntry: TimeEntry?) :
        TimeEntriesLogAction()
    data class ToggleTimeEntryGroupTapped(val groupId: Long) : TimeEntriesLogAction()
    object UndoButtonPressed : TimeEntriesLogAction()

    companion object {
        fun fromTimerAction(timerAction: TimerAction): TimeEntriesLogAction? =
            if (timerAction !is TimerAction.TimeEntriesLog) null
            else timerAction.timeEntriesLogAction

        fun toTimerAction(timeEntriesLogAction: TimeEntriesLogAction): TimerAction =
            TimerAction.TimeEntriesLog(
                timeEntriesLogAction
            )
    }
}

fun TimeEntriesLogAction.formatForDebug() =
    when (this) {
        is TimeEntriesLogAction.ContinueButtonTapped -> "Continue time entry button tapped for id $id"
        is TimeEntriesLogAction.TimeEntryTapped -> "Tapped time entry with id $id"
        is TimeEntriesLogAction.TimeEntrySwiped -> "Time entry with id $id swiped to the $direction "
        is TimeEntriesLogAction.TimeEntryGroupTapped -> "Tapped group containing time entries $ids"
        is TimeEntriesLogAction.TimeEntryGroupSwiped -> "Group containing time entries $ids swiped to the $direction"
        is TimeEntriesLogAction.TimeEntryStarted -> "Time entry started $startedTimeEntry"
        is TimeEntriesLogAction.TimeEntryDeleted -> "Deleted time entry with id ${deletedTimeEntry.id}"
        is TimeEntriesLogAction.ToggleTimeEntryGroupTapped -> "Time entry group with id $groupId toggled"
        is TimeEntriesLogAction.UndoButtonPressed -> "Undo button pressed"
    }
