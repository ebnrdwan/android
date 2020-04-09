package com.toggl.timer.di

import com.toggl.architecture.core.Store
import com.toggl.architecture.core.combine
import com.toggl.architecture.core.pullback
import com.toggl.timer.common.domain.TimerAction
import com.toggl.timer.common.domain.TimerReducer
import com.toggl.timer.common.domain.TimerState
import com.toggl.timer.log.domain.TimeEntriesLogAction
import com.toggl.timer.log.domain.TimeEntriesLogReducer
import com.toggl.timer.log.domain.TimeEntriesLogState
import com.toggl.timer.running.domain.RunningTimeEntryAction
import com.toggl.timer.running.domain.RunningTimeEntryReducer
import com.toggl.timer.running.domain.RunningTimeEntryState
import com.toggl.timer.startedit.domain.StartTimeEntryAction
import com.toggl.timer.startedit.domain.StartTimeEntryReducer
import com.toggl.timer.startedit.domain.StartTimeEntryState
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi

@Module(subcomponents = [TimerComponent::class])
class TimerModule {

    @ExperimentalCoroutinesApi
    @Provides
    internal fun timeEntriesLogStore(store: Store<TimerState, TimerAction>): Store<TimeEntriesLogState, TimeEntriesLogAction> =
        store.view(
            mapToLocalState = TimeEntriesLogState.Companion::fromTimerState,
            mapToGlobalAction = TimeEntriesLogAction.Companion::toTimerAction
        )

    @ExperimentalCoroutinesApi
    @Provides
    internal fun startTimeEntryStore(store: Store<TimerState, TimerAction>): Store<StartTimeEntryState, StartTimeEntryAction> =
        store.view(
            mapToLocalState = StartTimeEntryState.Companion::fromTimerState,
            mapToGlobalAction = StartTimeEntryAction.Companion::toTimerAction
        )

    @ExperimentalCoroutinesApi
    @Provides
    internal fun runningTimeEntryStore(store: Store<TimerState, TimerAction>): Store<RunningTimeEntryState, RunningTimeEntryAction> =
        store.view(
            mapToLocalState = RunningTimeEntryState.Companion::fromTimerState,
            mapToGlobalAction = RunningTimeEntryAction.Companion::toTimerAction
        )

    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @Provides
    @Singleton
    internal fun timerReducer(
        timeEntriesLogReducer: TimeEntriesLogReducer,
        startTimeEntryReducer: StartTimeEntryReducer,
        runningTimeEntryReducer: RunningTimeEntryReducer
    ): TimerReducer {

        return combine(
            timeEntriesLogReducer.pullback(
                mapToLocalState = TimeEntriesLogState.Companion::fromTimerState,
                mapToLocalAction = TimeEntriesLogAction.Companion::fromTimerAction,
                mapToGlobalState = TimeEntriesLogState.Companion::toTimerState,
                mapToGlobalAction = TimeEntriesLogAction.Companion::toTimerAction
            ),
            startTimeEntryReducer.pullback(
                mapToLocalState = StartTimeEntryState.Companion::fromTimerState,
                mapToLocalAction = StartTimeEntryAction.Companion::fromTimerAction,
                mapToGlobalState = StartTimeEntryState.Companion::toTimerState,
                mapToGlobalAction = StartTimeEntryAction.Companion::toTimerAction
            ),
            runningTimeEntryReducer.pullback(
                mapToLocalState = RunningTimeEntryState.Companion::fromTimerState,
                mapToLocalAction = RunningTimeEntryAction.Companion::fromTimerAction,
                mapToGlobalState = RunningTimeEntryState.Companion::toTimerState,
                mapToGlobalAction = RunningTimeEntryAction.Companion::toTimerAction
            )
        )
    }
}
