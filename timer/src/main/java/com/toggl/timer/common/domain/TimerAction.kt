package com.toggl.timer.common.domain

import arrow.optics.optics
import com.toggl.timer.log.domain.TimeEntriesLogAction
import com.toggl.timer.log.domain.formatForDebug
import com.toggl.timer.running.domain.RunningTimeEntryAction
import com.toggl.timer.running.domain.formatForDebug
import com.toggl.timer.startedit.domain.StartEditAction
import com.toggl.timer.startedit.domain.formatForDebug

@optics
sealed class TimerAction {
    class StartTimeEntry(val startEditAction: StartEditAction) : TimerAction()
    class TimeEntriesLog(val timeEntriesLogAction: TimeEntriesLogAction) : TimerAction()
    class RunningTimeEntry(val runningTimeEntryAction: RunningTimeEntryAction) : TimerAction()

    companion object
}

fun TimerAction.formatForDebug(): String =
    when (this) {
        is TimerAction.StartTimeEntry -> startEditAction.formatForDebug()
        is TimerAction.TimeEntriesLog -> timeEntriesLogAction.formatForDebug()
        is TimerAction.RunningTimeEntry -> runningTimeEntryAction.formatForDebug()
    }
