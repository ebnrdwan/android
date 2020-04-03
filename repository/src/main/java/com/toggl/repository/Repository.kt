package com.toggl.repository

import com.toggl.database.dao.ClientDao
import com.toggl.database.dao.ProjectDao
import com.toggl.database.dao.TagDao
import com.toggl.database.dao.TimeEntryDao
import com.toggl.database.dao.WorkspaceDao
import com.toggl.database.models.DatabaseClient
import com.toggl.database.models.DatabaseProject
import com.toggl.database.models.DatabaseTag
import com.toggl.database.models.DatabaseTimeEntry
import com.toggl.database.models.DatabaseTimeEntryWithTags
import com.toggl.database.models.DatabaseWorkspace
import com.toggl.environment.services.time.TimeService
import com.toggl.models.domain.Project
import com.toggl.models.domain.Tag
import com.toggl.models.domain.TimeEntry
import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature
import com.toggl.repository.interfaces.ClientRepository
import com.toggl.repository.extensions.toDatabaseModel
import com.toggl.repository.extensions.toModel
import com.toggl.repository.interfaces.ProjectRepository
import com.toggl.repository.interfaces.StartTimeEntryResult
import com.toggl.repository.interfaces.TagRepository
import com.toggl.repository.interfaces.TimeEntryRepository
import com.toggl.repository.interfaces.WorkspaceRepository
import org.threeten.bp.Duration

class Repository(
    private val projectDao: ProjectDao,
    private val timeEntryDao: TimeEntryDao,
    private val workspaceDao: WorkspaceDao,
    private val clientDao: ClientDao,
    private val tagDao: TagDao,
    private val timeService: TimeService
) : ProjectRepository, TimeEntryRepository, WorkspaceRepository, ClientRepository, TagRepository {

    override suspend fun loadProjects(): List<Project> =
        projectDao.getAll().map(DatabaseProject::toModel)

    override suspend fun loadTimeEntries(): List<TimeEntry> =
        timeEntryDao.getAllTimeEntriesWithTags().map(DatabaseTimeEntryWithTags::toModel)

    override suspend fun loadTags(): List<Tag> =
        tagDao.getAll().map(DatabaseTag::toModel)

    override suspend fun loadWorkspaces(): List<Workspace> =
        workspaceDao.getAll().let {
            if (it.any())
                return@let it.map(DatabaseWorkspace::toModel)

            // Automatically create a default workspace
            val workspace = DatabaseWorkspace(
                name = "Auto created workspace",
                features = listOf(WorkspaceFeature.Pro)
            )
            val workspaceId = workspaceDao.insert(workspace)

            return@let listOf(workspace.copy(id = workspaceId))
                .map(DatabaseWorkspace::toModel)
        }

    override suspend fun loadClients() = clientDao.getAll().map(DatabaseClient::toModel)

    override suspend fun startTimeEntry(
        workspaceId: Long,
        description: String
    ): StartTimeEntryResult {
        val stoppedTimeEntry = stopRunningTimeEntry()
        val id = timeEntryDao.insertTimeEntry(
            DatabaseTimeEntry(
                description = description,
                startTime = timeService.now(),
                duration = null,
                billable = false,
                workspaceId = workspaceId,
                projectId = null,
                taskId = null,
                isDeleted = false
            )
        )
        return StartTimeEntryResult(
            timeEntryDao.getOneTimeEntry(id).let(DatabaseTimeEntry::toModel),
            stoppedTimeEntry
        )
    }

    override suspend fun stopRunningTimeEntry(): TimeEntry? {
        val now = timeService.now()
        return timeEntryDao
            .getAllRunningTimeEntries()
            .map { it.copy(duration = Duration.between(it.startTime, now)) }
            .also(timeEntryDao::updateAllTimeEntries)
            .map(DatabaseTimeEntry::toModel)
            .firstOrNull()
    }

    override suspend fun editTimeEntry(timeEntry: TimeEntry): TimeEntry {
        return timeEntryDao.updateTimeEntryWithTags(timeEntry.toDatabaseModel()).run { timeEntry }
    }

    override suspend fun deleteTimeEntry(timeEntry: TimeEntry): TimeEntry {
        val timeEntryWithTags = timeEntry.copy(isDeleted = true).toDatabaseModel()
        return timeEntryWithTags
            .apply(timeEntryDao::updateTimeEntryWithTags)
            .let(DatabaseTimeEntryWithTags::toModel)
    }
}
