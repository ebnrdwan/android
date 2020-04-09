package com.toggl.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.toggl.onboarding.ui.LoginViewModel
import com.toggl.timer.log.ui.TimeEntriesLogStoreViewModel
import com.toggl.timer.running.ui.RunningTimeEntryStoreViewModel
import com.toggl.timer.startedit.ui.StartTimeEntryStoreViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(TimeEntriesLogStoreViewModel::class)
    abstract fun bindTimeEntriesLogViewModel(viewModel: TimeEntriesLogStoreViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun bindLoginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(StartTimeEntryStoreViewModel::class)
    abstract fun bindStartTimeEntryViewModel(viewModel: StartTimeEntryStoreViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RunningTimeEntryStoreViewModel::class)
    abstract fun bindRunningTimeEntryStoreViewModel(viewModel: RunningTimeEntryStoreViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: TogglViewModelFactory): ViewModelProvider.Factory
}
