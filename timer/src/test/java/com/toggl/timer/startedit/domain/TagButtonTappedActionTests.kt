package com.toggl.timer.startedit.domain

import com.toggl.architecture.DispatcherProvider
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.assertNoEffectsWereReturned
import com.toggl.timer.common.domain.EditableTimeEntry
import com.toggl.timer.common.testReduce
import com.toggl.timer.common.toMutableValue
import com.toggl.timer.exceptions.EditableTimeEntryShouldNotBeNullException
import io.kotlintest.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@kotlinx.coroutines.ExperimentalCoroutinesApi
@DisplayName("The TagButtonTapped action")
internal class TagButtonTappedActionTests {
    val testDispatcher = TestCoroutineDispatcher()
    val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, Dispatchers.Main)
    val repository = mockk<TimeEntryRepository>()
    val initialState = createInitialState()
    val reducer = StartEditReducer(repository, dispatcherProvider)

    @Test
    fun `should throw if editableTimeEntry is null`() {
        assertThrows<EditableTimeEntryShouldNotBeNullException> {
            var state = initialState.copy(editableTimeEntry = null)
            val mutableValue = state.toMutableValue { state = it }
            reducer.reduce(mutableValue, StartEditAction.TagButtonTapped)
        }
    }

    @Test
    fun `should set the description to # if it was empty and return no effects`() = runBlockingTest {
        reducer.testReduce(initialState, StartEditAction.TagButtonTapped) { state, effects ->
            state.editableTimeEntry!!.description shouldBe "#"
            assertNoEffectsWereReturned(state, effects)
        }
    }

    @Test
    fun `should append # to description and return no effects`() = runBlockingTest {
        val editableWithDescription = EditableTimeEntry(listOf(), 1, "asdf", false, null)

        reducer.testReduce(initialState.copy(editableTimeEntry = editableWithDescription), StartEditAction.TagButtonTapped) { state, effects ->
            state.editableTimeEntry!!.description shouldBe editableWithDescription.description + " #"
            assertNoEffectsWereReturned(state, effects)
        }
    }
}