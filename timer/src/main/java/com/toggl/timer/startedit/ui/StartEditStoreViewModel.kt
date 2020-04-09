package com.toggl.timer.startedit.ui

import androidx.lifecycle.ViewModel
import com.toggl.architecture.core.Store
import com.toggl.timer.startedit.domain.StartEditAction
import com.toggl.timer.startedit.domain.StartEditState
import javax.inject.Inject

class StartEditStoreViewModel @Inject constructor(
    store: Store<StartEditState, StartEditAction>
) : ViewModel(), Store<StartEditState, StartEditAction> by store
