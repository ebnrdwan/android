package com.toggl.timer.startedit.domain

import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.CoroutineTest
import com.toggl.timer.common.assertNoEffectsWereReturned
import com.toggl.timer.common.testReduce
import com.toggl.timer.common.testReduceState
import io.kotlintest.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DateTimePickingCancelledActionTests : CoroutineTest() {
    val repository = mockk<TimeEntryRepository>()
    val initialState = createInitialState()
    val reducer = StartEditReducer(repository, dispatcherProvider)

    @Test
    fun `should return no effect`() = runBlockingTest {
        reducer.testReduce(
            initialState,
            action = StartEditAction.DateTimePickingCancelled,
            testCase = ::assertNoEffectsWereReturned
        )
    }

    @Test
    fun `should change the pickMode to None`() = runBlockingTest {
        reducer.testReduceState(
            initialState.copy(dateTimePickMode = DateTimePickMode.StartTime),
            action = StartEditAction.DateTimePickingCancelled
        ) { state ->
            state.dateTimePickMode shouldBe DateTimePickMode.None
        }
    }
}