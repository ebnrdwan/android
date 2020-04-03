package com.toggl.repository.di

import com.toggl.database.dao.ClientDao
import com.toggl.database.dao.ProjectDao
import com.toggl.database.dao.TagDao
import com.toggl.database.dao.TimeEntryDao
import com.toggl.database.dao.WorkspaceDao
import com.toggl.environment.services.time.TimeService
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.repository.Repository
import com.toggl.repository.interfaces.ClientRepository
import com.toggl.repository.interfaces.ProjectRepository
import com.toggl.repository.interfaces.TagRepository
import com.toggl.repository.interfaces.WorkspaceRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {
    @Provides
    @Singleton
    fun repository(
        projectDao: ProjectDao,
        timeEntryDao: TimeEntryDao,
        workspaceDao: WorkspaceDao,
        clientDao: ClientDao,
        tagDao: TagDao,
        timeService: TimeService
    ) = Repository(projectDao, timeEntryDao, workspaceDao, clientDao, tagDao, timeService)

    @Provides
    @Singleton
    fun timeEntryRepository(repository: Repository): TimeEntryRepository =
        repository

    @Provides
    @Singleton
    fun workspaceRepository(repository: Repository): WorkspaceRepository =
        repository

    @Provides
    @Singleton
    fun projectRepository(repository: Repository): ProjectRepository =
        repository

    @Provides
    @Singleton
    fun clientRepository(repository: Repository): ClientRepository =
        repository

    @Provides
    @Singleton
    fun tagRepository(repository: Repository): TagRepository =
        repository
}
