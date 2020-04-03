package com.toggl.timer.running.ui

import androidx.lifecycle.ViewModel
import com.toggl.architecture.core.Store
import com.toggl.timer.running.domain.RunningTimeEntryAction
import com.toggl.timer.running.domain.RunningTimeEntryState
import javax.inject.Inject

class RunningTimeEntryStoreViewModel @Inject constructor(
    store: Store<RunningTimeEntryState, RunningTimeEntryAction>
) : ViewModel(), Store<RunningTimeEntryState, RunningTimeEntryAction> by store