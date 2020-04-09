package com.toggl.timer.di

import com.toggl.timer.log.ui.TimeEntriesLogFragment
import com.toggl.timer.running.ui.RunningTimeEntryFragment
import com.toggl.timer.startedit.ui.StartEditDialogFragment
import dagger.Subcomponent

@Subcomponent
interface TimerComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): TimerComponent
    }

    fun inject(fragment: TimeEntriesLogFragment)
    fun inject(fragment: StartEditDialogFragment)
    fun inject(fragment: RunningTimeEntryFragment)
}

interface TimerComponentProvider {
    fun provideTimerComponent(): TimerComponent
}
