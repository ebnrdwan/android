package com.toggl.domain.reducers

import com.toggl.architecture.DispatcherProvider
import com.toggl.domain.extensions.createClient
import com.toggl.domain.extensions.createProject
import com.toggl.domain.extensions.createTag
import com.toggl.domain.extensions.createTimeEntry
import com.toggl.domain.extensions.toSettableValue
import com.toggl.domain.loading.LoadClientsEffect
import com.toggl.domain.loading.LoadProjectsEffect
import com.toggl.domain.loading.LoadTimeEntriesEffect
import com.toggl.domain.loading.LoadWorkspacesEffect
import com.toggl.domain.loading.LoadingAction
import com.toggl.domain.loading.LoadingReducer
import com.toggl.domain.loading.LoadingState
import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature
import com.toggl.repository.interfaces.ClientRepository
import com.toggl.repository.interfaces.ProjectRepository
import com.toggl.repository.interfaces.TagRepository
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.repository.interfaces.WorkspaceRepository
import io.kotlintest.matchers.collections.shouldContainInOrder
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.mockk.mockk

class LoadingReducerTests : FreeSpec({
    val projectRepository = mockk<ProjectRepository>()
    val clientRepository = mockk<ClientRepository>()
    val timeEntryRepository = mockk<TimeEntryRepository>()
    val workspaceRepository = mockk<WorkspaceRepository>()
    val tagRepository = mockk<TagRepository>()
    val dispatcherProvider = mockk<DispatcherProvider>()
    val reducer = LoadingReducer(projectRepository, clientRepository, timeEntryRepository, workspaceRepository, tagRepository, dispatcherProvider)
    val emptyState = LoadingState(listOf(), listOf(), listOf(), listOf(), listOf())

    "The LoadingReducer" - {
        "when receiving a Start Loading action" - {

            "does not update the state" {
                var initialState = emptyState
                val settableValue = initialState.toSettableValue { initialState = it }
                reducer.reduce(settableValue, LoadingAction.StartLoading)

                initialState shouldBe emptyState
            }

            "returns a list of effects that load entities" {
                var initialState = emptyState
                val settableValue = initialState.toSettableValue { initialState = it }
                val effects = reducer.reduce(settableValue, LoadingAction.StartLoading)

                effects.map { it.javaClass.kotlin } shouldContainInOrder listOf(
                    LoadWorkspacesEffect::class,
                    LoadProjectsEffect::class,
                    LoadClientsEffect::class,
                    LoadTimeEntriesEffect::class
                )
            }
        }

        "when receiving a Time Entries Loaded action" - {

            "updates the state to add the loaded time entries" - {
                val entries = listOf(createTimeEntry(1), createTimeEntry(2), createTimeEntry(3))
                var initialState = emptyState
                val settableValue = initialState.toSettableValue { initialState = it }
                reducer.reduce(settableValue, LoadingAction.TimeEntriesLoaded(entries))

                initialState shouldBe emptyState.copy(timeEntries = entries)
            }
        }

        "when receiving a Workspaces Loaded action" - {

            "updates the state to add the loaded workspaces" - {
                val workspaces = listOf(
                    Workspace(1, "1", listOf()),
                    Workspace(2, "2", listOf(WorkspaceFeature.Pro)),
                    Workspace(3, "3", listOf())
                )
                var initialState = emptyState
                val settableValue = initialState.toSettableValue { initialState = it }
                reducer.reduce(settableValue, LoadingAction.WorkspacesLoaded(workspaces))

                initialState shouldBe emptyState.copy(workspaces = workspaces)
            }
        }

        "when receiving a Projects Loaded action" - {

            "updates the state to add the loaded projects" - {
                val projects = (1L..10L).map { createProject(it) }
                var initialState = emptyState
                val settableValue = initialState.toSettableValue { initialState = it }
                reducer.reduce(settableValue, LoadingAction.ProjectsLoaded(projects))

                initialState shouldBe emptyState.copy(projects = projects)
            }
        }

        "when receiving a Clients Loaded action" - {

            "updates the state to add the loaded clients" - {
                val clients = (1L..10L).map { createClient(it) }
                var initialState = emptyState
                val settableValue = initialState.toSettableValue { initialState = it }
                reducer.reduce(settableValue, LoadingAction.ClientsLoaded(clients))

                initialState shouldBe emptyState.copy(clients = clients)
            }
        }

        "when receiving a Tags Loaded action" - {

            "updates the state to add the loaded tags" - {
                val tags = (1L..10L).map { createTag(it) }
                var initialState = emptyState
                val settableValue = initialState.toSettableValue { initialState = it }
                reducer.reduce(settableValue, LoadingAction.TagsLoaded(tags))

                initialState shouldBe emptyState.copy(tags = tags)
            }
        }
    }
})