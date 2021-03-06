package com.toggl.timer.startedit.domain

import com.toggl.timer.common.CoroutineTest
import com.toggl.timer.common.assertNoEffectsWereReturned
import com.toggl.timer.common.testReduce
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@DisplayName("The AutocompleteSuggestionsUpdated action")
internal class AutocompleteSuggestionTappedActionTests : CoroutineTest() {
    val initialState = createInitialState()
    val reducer = createReducer()

    @Test
    fun `should return no effect`() = runBlockingTest {
        reducer.testReduce(
            initialState,
            action = StartEditAction.AutocompleteSuggestionTapped(mockk()),
            testCase = ::assertNoEffectsWereReturned
        )
    }
}