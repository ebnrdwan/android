package com.toggl.timer.startedit.domain

import com.toggl.architecture.extensions.noEffect
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.FreeCoroutineSpec
import com.toggl.timer.common.toSettableValue
import io.kotlintest.properties.Gen
import io.kotlintest.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ToggleBillableActionTests : FreeCoroutineSpec() {
    init {
        val repository = mockk<TimeEntryRepository>()
        val reducer = StartTimeEntryReducer(repository, dispatcherProvider)

        "The ToggleBillable action" - {
            "should invert the values of the editable time entry" - {
                Gen.bool().constants().forEach { originalBillableValue ->
                    var state = StartTimeEntryState.editableTimeEntry
                        .modify(createInitialState()) { it.copy(billable = originalBillableValue) }
                    val settableValue = state.toSettableValue { state = it }

                    val effects = reducer.reduce(settableValue, StartTimeEntryAction.ToggleBillable)
                    effects shouldBe noEffect()
                    state.editableTimeEntry!!.billable shouldBe !originalBillableValue
                }
            }
        }
    }
}