package com.toggl.domain.reducers

import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.HigherOrderReducer
import com.toggl.architecture.core.SettableValue
import com.toggl.architecture.extensions.noEffect
import com.toggl.domain.AppAction
import com.toggl.domain.AppState
import com.toggl.models.extensions.isPro
import com.toggl.timer.common.domain.TimerAction
import com.toggl.timer.common.domain.getRunningTimeEntryWorkspaceId
import com.toggl.timer.start.domain.StartTimeEntryAction

class FeatureAvailabilityReducer(override val innerReducer: AppReducer)
    : HigherOrderReducer<AppState, AppAction> {
    override fun reduce(
        state: SettableValue<AppState>,
        action: AppAction
    ): List<Effect<AppAction>> {

        if (action.isToggleBillableAction()) {
            val workspaceId = state.value.timerLocalState.getRunningTimeEntryWorkspaceId()
            return state.value.workspaces[workspaceId]?.let { workspace ->
                if (workspace.isPro()) innerReducer.reduce(state, action)
                else noEffect()
            } ?: noEffect()
        }

        return innerReducer.reduce(state, action)
    }
}

fun AppAction.isToggleBillableAction() =
    this is AppAction.Timer &&
        this.timer is TimerAction.StartTimeEntry &&
        this.timer.startTimeEntryAction is StartTimeEntryAction.ToggleBillable