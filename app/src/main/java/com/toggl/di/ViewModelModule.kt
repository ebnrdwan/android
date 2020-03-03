package com.toggl.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.toggl.TogglViewModelFactory
import com.toggl.onboarding.ui.LoginViewModel
import com.toggl.timer.ui.StartTimeEntryStoreViewModel
import com.toggl.timer.ui.TimeEntriesLogStoreViewModel
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
    abstract fun bindViewModelFactory(factory: TogglViewModelFactory): ViewModelProvider.Factory
}