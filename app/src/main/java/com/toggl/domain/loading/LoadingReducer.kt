package com.toggl.domain.loading

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.architecture.core.Reducer
import com.toggl.architecture.core.SettableValue
import com.toggl.architecture.extensions.effects
import com.toggl.architecture.extensions.noEffect
import com.toggl.repository.interfaces.ClientRepository
import com.toggl.repository.interfaces.ProjectRepository
import com.toggl.repository.interfaces.TagRepository
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.repository.interfaces.WorkspaceRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoadingReducer @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val clientRepository: ClientRepository,
    private val timeEntryRepository: TimeEntryRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val tagsRepository: TagRepository,
    private val dispatcherProvider: DispatcherProvider
) : Reducer<LoadingState, LoadingAction> {

    override fun reduce(
        state: SettableValue<LoadingState>,
        action: LoadingAction
    ): List<Effect<LoadingAction>> =
        when (action) {
            LoadingAction.StartLoading -> effects(
                LoadWorkspacesEffect(workspaceRepository, dispatcherProvider),
                LoadProjectsEffect(projectRepository, dispatcherProvider),
                LoadClientsEffect(clientRepository, dispatcherProvider),
                LoadTagsEffect(tagsRepository, dispatcherProvider),
                LoadTimeEntriesEffect(timeEntryRepository, dispatcherProvider)
            )
            is LoadingAction.TimeEntriesLoaded -> {
                state.value = state.value.copy(timeEntries = action.timeEntries)
                noEffect()
            }
            is LoadingAction.WorkspacesLoaded -> {
                state.value = state.value.copy(workspaces = action.workspaces)
                noEffect()
            }
            is LoadingAction.ProjectsLoaded -> {
                state.value = state.value.copy(projects = action.projects)
                noEffect()
            }
            is LoadingAction.ClientsLoaded -> {
                state.value = state.value.copy(clients = action.clients)
                noEffect()
            }
            is LoadingAction.TagsLoaded -> {
                state.value = state.value.copy(tags = action.tags)
                noEffect()
            }
        }
}