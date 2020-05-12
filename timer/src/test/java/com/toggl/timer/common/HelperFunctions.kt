package com.toggl.timer.common

import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.MutableValue
import com.toggl.architecture.core.Reducer
import com.toggl.models.domain.TimeEntry
import io.kotlintest.matchers.collections.shouldBeEmpty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

fun createTimeEntry(
    id: Long,
    description: String = "",
    startTime: OffsetDateTime = OffsetDateTime.now(),
    duration: Duration? = Duration.ofMinutes(2),
    billable: Boolean = false,
    projectId: Long? = null,
    taskId: Long? = null,
    tags: List<Long> = emptyList()
) =
    TimeEntry(
        id,
        description,
        startTime,
        duration,
        billable,
        1,
        projectId,
        taskId,
        false,
        tags
    )

fun <T> T.toMutableValue(setFunction: (T) -> Unit) =
    MutableValue({ this }, setFunction)

suspend fun <State, Action> Reducer<State, Action>.testReduce(
    initialState: State,
    action: Action,
    testCase: suspend (State, List<Effect<Action>>) -> Unit
) {
    var state = initialState
    val mutableValue = state.toMutableValue { state = it }
    val effect = reduce(mutableValue, action)
    testCase(state, effect)
}

@ExperimentalCoroutinesApi
fun <State, Action, EX : Exception> Reducer<State, Action>.testReduceException(
    initialState: State,
    action: Action,
    exception: Class<EX>
) {
    Assertions.assertThrows(exception) {
        runBlockingTest {
            testReduce(initialState, action) { _, _ -> }
        }
    }
}

suspend fun <State, Action> Reducer<State, Action>.testReduceState(
    initialState: State,
    action: Action,
    testCase: suspend (State) -> Unit
) = testReduce(initialState, action) { state, _ -> testCase(state) }

suspend fun <State, Action> Reducer<State, Action>.testReduceEffects(
    initialState: State,
    action: Action,
    testCase: suspend (List<Effect<Action>>) -> Unit
) = testReduce(initialState, action) { _, effects -> testCase(effects) }

suspend fun <State, Action> Reducer<State, Action>.testReduceNoEffects(
    initialState: State,
    action: Action
) = testReduce(initialState, action, ::assertNoEffectsWereReturned)

@Suppress("UNUSED_PARAMETER")
suspend fun <State, Action> assertNoEffectsWereReturned(state: State, effect: List<Effect<Action>>) {
    effect.shouldBeEmpty()
}