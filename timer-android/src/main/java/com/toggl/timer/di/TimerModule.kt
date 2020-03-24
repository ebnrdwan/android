package com.toggl.timer.di

import com.toggl.architecture.core.Store
import com.toggl.architecture.core.combine
import com.toggl.architecture.core.pullback
import com.toggl.timer.common.domain.TimerAction
import com.toggl.timer.common.domain.TimerReducer
import com.toggl.timer.common.domain.TimerState
import com.toggl.timer.log.domain.TimeEntriesLogState
import com.toggl.timer.log.domain.TimeEntriesLogAction
import com.toggl.timer.log.domain.TimeEntriesLogReducer
import com.toggl.timer.start.domain.StartTimeEntryAction
import com.toggl.timer.start.domain.StartTimeEntryReducer
import com.toggl.timer.start.domain.StartTimeEntryState
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
    @InternalCoroutinesApi
    @Provides
    @Singleton
    internal fun timerReducer(
        timeEntriesLogReducer: TimeEntriesLogReducer,
        startTimeEntryReducer: StartTimeEntryReducer
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
            )
        )
    }
}
