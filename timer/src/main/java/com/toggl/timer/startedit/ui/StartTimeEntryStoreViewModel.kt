package com.toggl.timer.startedit.ui

import androidx.lifecycle.ViewModel
import com.toggl.architecture.core.Store
import com.toggl.timer.startedit.domain.StartTimeEntryAction
import com.toggl.timer.startedit.domain.StartEditState
import javax.inject.Inject

class StartTimeEntryStoreViewModel @Inject constructor(
    store: Store<StartEditState, StartTimeEntryAction>
) : ViewModel(), Store<StartEditState, StartTimeEntryAction> by store
