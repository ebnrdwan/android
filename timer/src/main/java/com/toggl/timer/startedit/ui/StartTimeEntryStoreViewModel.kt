package com.toggl.timer.startedit.ui

import androidx.lifecycle.ViewModel
import com.toggl.architecture.core.Store
import com.toggl.timer.startedit.domain.StartTimeEntryAction
import com.toggl.timer.startedit.domain.StartTimeEntryState
import javax.inject.Inject

class StartTimeEntryStoreViewModel @Inject constructor(
    store: Store<StartTimeEntryState, StartTimeEntryAction>
) : ViewModel(), Store<StartTimeEntryState, StartTimeEntryAction> by store
