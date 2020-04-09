package com.toggl.timer.log.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.toggl.common.deepLinks
import com.toggl.models.common.SwipeDirection
import com.toggl.environment.services.time.TimeService
import com.toggl.timer.R
import com.toggl.timer.di.TimerComponentProvider
import com.toggl.timer.log.domain.FlatTimeEntryViewModel
import com.toggl.timer.log.domain.TimeEntriesLogAction
import com.toggl.timer.log.domain.TimeEntriesLogState
import com.toggl.timer.log.domain.TimeEntryGroupViewModel
import com.toggl.timer.log.domain.TimeEntryViewModel
import com.toggl.timer.log.domain.timeEntriesLogSelector
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_time_entries_log.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TimeEntriesLogFragment : Fragment(R.layout.fragment_time_entries_log) {

    @Inject
    lateinit var timeService: TimeService

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val store: TimeEntriesLogStoreViewModel by viewModels { viewModelFactory }

    private val adapter = TimeEntriesLogAdapter(
        { store.dispatch(TimeEntriesLogAction.ContinueButtonTapped(it)) },
        { store.dispatch(TimeEntriesLogAction.ToggleTimeEntryGroupTapped(it)) }
    )

    override fun onAttach(context: Context) {
        (requireActivity().applicationContext as TimerComponentProvider)
            .provideTimerComponent().inject(this)
        super.onAttach(context)
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler_view.adapter = adapter
        val context = requireContext()
        val swipeCallback = createSwipeActionCallback(requireContext())
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recycler_view)

        val todayString = context.getString(R.string.today)
        val yesterdayString = context.getString(R.string.yesterday)

        val curriedTimeEntriesSelector: suspend (TimeEntriesLogState) -> List<TimeEntryViewModel> = {
            timeEntriesLogSelector(
                it.timeEntries,
                it.projects,
                it.clients,
                timeService,
                todayString,
                yesterdayString,
                true,
                it.expandedGroupIds,
                it.entriesPendingDeletion
            )
        }

        lifecycleScope.launch {
            store.state
                .map(curriedTimeEntriesSelector)
                .distinctUntilChanged()
                .onEach { adapter.submitList(it) }
                .launchIn(this)

            store.state
                .map { it.editableTimeEntry != null }
                .distinctUntilChanged()
                .onEach { isEditViewExpanded ->
                    if (isEditViewExpanded) {
                        findNavController().navigate(deepLinks.timeEntriesStartEditDialog)
                    }
                }
                .launchIn(this)
        }
    }

    private fun onLogItemSwiped(adapterPosition: Int, swipeDirection: SwipeDirection) {
        val currentItems: List<TimeEntryViewModel> = adapter.currentList
        if (adapterPosition >= currentItems.size) {
            adapter.notifyDataSetChanged()
            return
        }

        when (val item = currentItems[adapterPosition]) {
            is FlatTimeEntryViewModel -> TimeEntriesLogAction.TimeEntrySwiped(item.id, swipeDirection)
            is TimeEntryGroupViewModel -> TimeEntriesLogAction.TimeEntryGroupSwiped(item.timeEntryIds, swipeDirection)
            else -> null
        }?.let(store::dispatch)

        if (swipeDirection == SwipeDirection.Right) {
            adapter.notifyItemChanged(adapterPosition)
        }
    }

    private fun createSwipeActionCallback(context: Context): SwipeActionCallback {
        val leftSwipeColor = ContextCompat.getColor(context, R.color.stop_time_entry_button_background)
        val rightSwipeColor = ContextCompat.getColor(context, R.color.start_time_entry_button_background)
        val actionTextColor = ContextCompat.getColor(context, R.color.text_on_surface)
        val actionFontSize = resources.getDimension(R.dimen.swipe_action_font_size)
        val actionPadding = resources.getDimension(R.dimen.swipe_action_text_padding)

        val swipeLeftParams = SwipeActionParams(
            text = getString(R.string.delete),
            backgroundColor = leftSwipeColor,
            textColor = actionTextColor,
            fontSize = actionFontSize,
            textPadding = actionPadding
        )
        val swipeRightParams = SwipeActionParams(
            text = getString(R.string.continue_this),
            backgroundColor = rightSwipeColor,
            textColor = actionTextColor,
            fontSize = actionFontSize,
            textPadding = actionPadding
        )

        return SwipeActionCallback(
            swipeLeftParams = swipeLeftParams,
            swipeRightParams = swipeRightParams,
            onSwipeActionListener = ::onLogItemSwiped
        )
    }
}
