package com.toggl.timer.running.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.toggl.models.domain.TimeEntry
import com.toggl.timer.R
import com.toggl.timer.di.TimerComponentProvider
import com.toggl.timer.extensions.runningTimeEntryOrNull
import com.toggl.timer.running.domain.RunningTimeEntryAction
import kotlinx.android.synthetic.main.running_time_entry_blank_layout.*
import kotlinx.android.synthetic.main.fragment_running_time_entry.*
import kotlinx.android.synthetic.main.running_time_entry_layout.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class RunningTimeEntryFragment : Fragment(R.layout.fragment_running_time_entry) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val store: RunningTimeEntryStoreViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        (requireActivity().applicationContext as TimerComponentProvider)
            .provideTimerComponent().inject(this)
        super.onAttach(context)
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        time_entry_description.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                store.dispatch(RunningTimeEntryAction.DescriptionTextFieldTapped)
            }
        }

        running_time_entry_description.setOnClickListener {
            store.dispatch(RunningTimeEntryAction.RunningTimeEntryTapped)
        }

        lifecycleScope.launch {

            val runningTimeEntryFlow = store.state
                .map { it.timeEntries.runningTimeEntryOrNull() }
                .distinctUntilChanged()

            runningTimeEntryFlow
                .filterNotNull()
                .onEach { updateRunningTimeEntryCard(it) }
                .launchIn(this)

            runningTimeEntryFlow
                .map { it != null }
                .distinctUntilChanged()
                .onEach { setEditedTimeEntryState(it) }
                .launchIn(this)
        }
    }

    private fun updateRunningTimeEntryCard(timeEntry: TimeEntry) {
        running_time_entry_description.text = timeEntry.description
    }

    private fun setEditedTimeEntryState(timeEntryIsRunning: Boolean) {
        empty_running_time_entry_layout.isVisible = !timeEntryIsRunning
        running_time_entry_layout.isVisible = timeEntryIsRunning

        with(start_time_entry_button) {
            if (timeEntryIsRunning) {
                val color = ContextCompat.getColor(
                    requireContext(),
                    R.color.stop_time_entry_button_background
                )
                backgroundTintList = ColorStateList.valueOf(color)
                setImageResource(R.drawable.ic_stop)
                setOnClickListener {
                    store.dispatch(RunningTimeEntryAction.StopButtonTapped)
                }
            } else {
                val color = ContextCompat.getColor(
                    requireContext(),
                    R.color.start_time_entry_button_background
                )
                backgroundTintList = ColorStateList.valueOf(color)
                setImageResource(R.drawable.ic_play_big)
            }
        }
    }
}