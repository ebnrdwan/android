package com.toggl.domain.reducers

import com.toggl.architecture.core.Reducer
import com.toggl.domain.AppAction
import com.toggl.domain.AppState
import com.toggl.domain.generators.appAction
import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature
import com.toggl.timer.common.domain.TimerAction
import com.toggl.timer.start.domain.StartTimeEntryAction
import io.kotlintest.properties.Gen
import io.kotlintest.specs.FreeSpec
import io.mockk.spyk
import io.mockk.verify

class FeatureAvailabilityReducerTests : FreeSpec({
    "The FeatureAvailabilityReducer" - {
        "For non premium actions" - {
            val state = AppState()
            val settableValue = state.toSettableValue { }
            val nonPremiumActions = Gen.appAction().constants().filterNot(::actionIsPremium)

            "forwards the action to the child reducer" - {
                nonPremiumActions.forEach { appAction ->
                    val spyReducer = spyk<Reducer<AppState, AppAction>>()
                    val featureAvailabilityReducer = FeatureAvailabilityReducer(spyReducer)

                    featureAvailabilityReducer.reduce(settableValue, appAction)

                    verify { spyReducer.reduce(settableValue, appAction) }
                }
            }
        }

        "For the toggle billable action" - {
            val toggleBillableAction = AppAction.Timer(
                TimerAction.StartTimeEntry(
                    StartTimeEntryAction.ToggleBillable
                )
            )

            "forwards the action normally if the edited TE is in a premium workspace" - {
                val state = AppState().copy(
                    workspaces = mapOf(1L to Workspace(1, "Auto created workspace", listOf(WorkspaceFeature.Pro)))
                )
                val settableValue = state.toSettableValue { }
                val spyReducer = spyk<Reducer<AppState, AppAction>>()
                val featureAvailabilityReducer = FeatureAvailabilityReducer(spyReducer)

                featureAvailabilityReducer.reduce(settableValue, toggleBillableAction)

                verify { spyReducer.reduce(settableValue, toggleBillableAction) }
            }

            "does not forward the action when the edited TE is in a non-premium workspace" - {
                val state = AppState().copy(
                    workspaces = mapOf(1L to Workspace(1, "Auto created workspace", listOf()))
                )
                val settableValue = state.toSettableValue { }
                val spyReducer = spyk<Reducer<AppState, AppAction>>()
                val featureAvailabilityReducer = FeatureAvailabilityReducer(spyReducer)

                featureAvailabilityReducer.reduce(settableValue, toggleBillableAction)

                verify(exactly = 0) { spyReducer.reduce(settableValue, toggleBillableAction) }
            }
        }
    }
})

private fun actionIsPremium(appAction: AppAction) =
    appAction.isToggleBillableAction()