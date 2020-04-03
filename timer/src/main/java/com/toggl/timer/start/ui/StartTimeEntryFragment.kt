package com.toggl.timer.start.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.toggl.timer.R
import com.toggl.timer.di.TimerComponentProvider
import com.toggl.timer.extensions.runningTimeEntryOrNull
import com.toggl.timer.start.domain.StartTimeEntryAction
import kotlinx.android.synthetic.main.fragment_start_time_entry.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class StartTimeEntryFragment : Fragment(R.layout.fragment_start_time_entry) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val store: StartTimeEntryStoreViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        (requireActivity().applicationContext as TimerComponentProvider)
            .provideTimerComponent().inject(this)
        super.onAttach(context)
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        time_entry_description.doOnTextChanged { text, _, _, _ ->
            val action = StartTimeEntryAction.DescriptionEntered(text.toString())
            store.dispatch(action)
        }

        close_action.setOnClickListener {
            store.dispatch(StartTimeEntryAction.CloseButtonTapped)
        }

        lifecycleScope.launch {

            store.state
                .map { it.editableTimeEntry?.description }
                .distinctUntilChanged()
                .onEach {
                    if (time_entry_description?.text.toString() != it) {
                        time_entry_description.setText(it)
                    }
                }
                .launchIn(this)

            store.state
                .map { it.timeEntries.runningTimeEntryOrNull() != null }
                .distinctUntilChanged()
                .onEach { setEditedTimeEntryState(it) }
                .launchIn(this)

            store.state
                .mapNotNull { it.editableTimeEntry }
                .onEach {
                    time_entry_description.requestFocus()
                    extended_options.isInvisible = true
                }
                .launchIn(this)
        }
    }

    private fun setEditedTimeEntryState(timeEntryIsRunning: Boolean) {
        done_action.isVisible = !timeEntryIsRunning
        done_action.setOnClickListener {
            store.dispatch(StartTimeEntryAction.DoneButtonTapped)
        }
    }
}
