import com.toggl.architecture.DispatcherProvider
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.timer.common.testReduce
import com.toggl.timer.startedit.domain.DateTimePickMode
import com.toggl.timer.startedit.domain.StartEditAction
import com.toggl.timer.startedit.domain.StartEditReducer
import com.toggl.timer.startedit.domain.createInitialState
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@kotlinx.coroutines.ExperimentalCoroutinesApi
@DisplayName("The PickerTapped action")
internal class PickerTappedActionTests {
    val testDispatcher = TestCoroutineDispatcher()
    val dispatcherProvider = DispatcherProvider(testDispatcher, testDispatcher, Dispatchers.Main)
    val repository = mockk<TimeEntryRepository>()
    val initialState = createInitialState()
    val reducer = StartEditReducer(repository, dispatcherProvider)

    @ParameterizedTest
    @MethodSource("allPickerModes")
    fun `sets the DateTimePickMode in state and returns no effect`(dateTimePickMode: DateTimePickMode) = runBlockingTest {
        reducer.testReduce(
            initialState = initialState,
            action = StartEditAction.PickerTapped(dateTimePickMode)
        ) { state, effects ->
            state.dateTimePickMode shouldBe dateTimePickMode
            effects.shouldBeEmpty()
        }
    }

    companion object {
        @JvmStatic
        fun allPickerModes() = DateTimePickMode.values().toList()
    }
}