package com.toggl.domain.reducers

import com.toggl.architecture.core.Reducer
import com.toggl.domain.AppAction
import com.toggl.domain.AppState
import com.toggl.domain.loading.LoadingAction
import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature
import com.toggl.onboarding.domain.actions.OnboardingAction
import com.toggl.timer.common.domain.TimerAction
import com.toggl.timer.startedit.domain.StartEditAction
import io.kotlintest.specs.FreeSpec
import io.mockk.spyk
import io.mockk.verify

class FeatureAvailabilityReducerTests : FreeSpec() {
    init {

        val allActions = listOf(
            AppAction.Loading(LoadingAction.StartLoading),
            AppAction.Timer(TimerAction.StartTimeEntry(StartEditAction.BillableTapped)),
            AppAction.Onboarding(OnboardingAction.LoginTapped)
        )

        "The FeatureAvailabilityReducer" - {
            "For non premium actions" - {
                val state = AppState()
                val mutableValue = state.toMutableValue { }
                val nonPremiumActions = allActions.filterNot(::actionIsPremium)

                "forwards the action to the child reducer" - {
                    nonPremiumActions.forEach { appAction ->
                        val spyReducer = spyk<Reducer<AppState, AppAction>>()
                        val featureAvailabilityReducer = FeatureAvailabilityReducer(spyReducer)

                        featureAvailabilityReducer.reduce(mutableValue, appAction)

                        verify { spyReducer.reduce(mutableValue, appAction) }
                    }
                }
            }

            "For the toggle billable action" - {
                val toggleBillableAction = AppAction.Timer(
                    TimerAction.StartTimeEntry(
                        StartEditAction.BillableTapped
                    )
                )

                "forwards the action normally if the edited TE is in a premium workspace" - {
                    val state = AppState().copy(
                        workspaces = mapOf(1L to Workspace(1, "Auto created workspace", listOf(WorkspaceFeature.Pro)))
                    )
                    val mutableValue = state.toMutableValue { }
                    val spyReducer = spyk<Reducer<AppState, AppAction>>()
                    val featureAvailabilityReducer = FeatureAvailabilityReducer(spyReducer)

                    featureAvailabilityReducer.reduce(mutableValue, toggleBillableAction)

                    verify { spyReducer.reduce(mutableValue, toggleBillableAction) }
                }

                "does not forward the action when the edited TE is in a non-premium workspace" - {
                    val state = AppState().copy(
                        workspaces = mapOf(1L to Workspace(1, "Auto created workspace", listOf()))
                    )
                    val mutableValue = state.toMutableValue { }
                    val spyReducer = spyk<Reducer<AppState, AppAction>>()
                    val featureAvailabilityReducer = FeatureAvailabilityReducer(spyReducer)

                    featureAvailabilityReducer.reduce(mutableValue, toggleBillableAction)

                    verify(exactly = 0) { spyReducer.reduce(mutableValue, toggleBillableAction) }
                }
            }
        }
    }

    private fun actionIsPremium(appAction: AppAction) =
        appAction.isToggleBillableAction()
}