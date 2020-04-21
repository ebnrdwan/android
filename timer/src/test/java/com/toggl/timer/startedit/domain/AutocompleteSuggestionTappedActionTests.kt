package com.toggl.timer.startedit.domain

import com.toggl.repository.interfaces.TimeEntryRepository
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
    val repository = mockk<TimeEntryRepository>()
    val initialState = createInitialState()
    val reducer = StartEditReducer(repository, dispatcherProvider)

    @Test
    fun `should return no effect`() = runBlockingTest {
        reducer.testReduce(
            initialState,
            action = StartEditAction.AutocompleteSuggestionTapped(mockk()),
            testCase = ::assertNoEffectsWereReturned
        )
    }
}