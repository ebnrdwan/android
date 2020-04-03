package com.toggl.timer.common

import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.SettableValue
import com.toggl.models.domain.TimeEntry
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

fun createTimeEntry(
    id: Long,
    description: String = "",
    startTime: OffsetDateTime = OffsetDateTime.now(),
    duration: Duration? = null,
    billable: Boolean = false,
    projectId: Long? = null
) =
    TimeEntry(
        id,
        description,
        startTime,
        duration,
        billable,
        1,
        projectId,
        null,
        false,
        emptyList()
    )

fun <T> T.toSettableValue(setFunction: (T) -> Unit) =
    SettableValue({ this }, setFunction)

suspend fun <State, Action> Reducer<State, Action>.testReduce(
    initialState: State,
    action: Action,
    testCase: suspend (State, List<Effect<Action>>) -> Unit
) {
    var state = initialState
    val settableValue = state.toSettableValue { state = it }
    val effect = reduce(settableValue, action)
    testCase(state, effect)
}