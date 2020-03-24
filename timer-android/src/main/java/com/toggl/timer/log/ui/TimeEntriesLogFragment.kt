package com.toggl.timer.log.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.toggl.environment.services.time.TimeService
import com.toggl.timer.R
import com.toggl.timer.di.TimerComponentProvider
import com.toggl.timer.log.domain.TimeEntriesLogState
import com.toggl.timer.log.domain.TimeEntryViewModel
import com.toggl.timer.log.domain.TimeEntriesLogAction
import com.toggl.timer.log.domain.timeEntriesLogSelector
import javax.inject.Inject
import kotlinx.android.synthetic.main.time_entries_log_fragment.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TimeEntriesLogFragment : Fragment(R.layout.time_entries_log_fragment) {

    @Inject
    lateinit var timeService: TimeService

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val store: TimeEntriesLogStoreViewModel by viewModels { viewModelFactory }

    private val adapter = TimeEntriesLogAdapter(
        { store.dispatch(TimeEntriesLogAction.ContinueButtonTapped(it)) },
        { TODO("dispatch group expanding action") }
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
        val todayString = context.getString(R.string.today)
        val yesterdayString = context.getString(R.string.yesterday)

        val curriedTimeEntriesSelector: suspend (TimeEntriesLogState) -> List<TimeEntryViewModel> = {
            timeEntriesLogSelector(
                it.timeEntries,
                it.projects,
                timeService,
                todayString,
                yesterdayString,
                true
            )
        }

        lifecycleScope.launch {
            store.state
                .map(curriedTimeEntriesSelector)
                .distinctUntilChanged()
                .onEach { adapter.submitList(it) }
                .launchIn(this)
        }
    }
}