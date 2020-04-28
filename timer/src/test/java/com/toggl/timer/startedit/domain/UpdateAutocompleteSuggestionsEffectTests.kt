package com.toggl.timer.startedit.domain

import com.toggl.timer.common.CoroutineTest
import io.kotlintest.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
@DisplayName("The UpdateAutocompleteSuggestionsEffect")
class UpdateAutocompleteSuggestionsEffectTests : CoroutineTest() {

    @Test
    fun `returns nothing`() = runBlockingTest {

        val effect = UpdateAutocompleteSuggestionsEffect(
            dispatcherProvider,
            "something @and stuff",
            12,
            mapOf(),
            mapOf(),
            mapOf(),
            mapOf(),
            mapOf()
        )

        val result = effect.execute()
        result shouldBe StartEditAction.AutocompleteSuggestionsUpdated(emptyList())
    }
}