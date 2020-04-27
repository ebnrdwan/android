package com.toggl.database.util

import com.toggl.database.BaseDatabaseTest
import com.toggl.database.models.DatabaseClient
import com.toggl.database.models.DatabaseProject
import com.toggl.database.models.DatabaseTag
import com.toggl.database.models.DatabaseTask
import com.toggl.database.models.DatabaseTimeEntry
import com.toggl.database.models.DatabaseTimeEntryWithTags
import com.toggl.database.models.DatabaseWorkspace
import com.toggl.models.domain.WorkspaceFeature
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneOffset

fun BaseDatabaseTest.defaultWorkspaceId(): Long {
    val workspaces = database.workspaceDao().getAll()
    if (workspaces.isNotEmpty())
        return workspaces.first().id

    return createProWorkspace("Generated Default Workspace")
}

fun BaseDatabaseTest.prepareTimeEntry(
    description: String = "Generated Time Entry",
    startTime: OffsetDateTime = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
    duration: Duration? = Duration.ofMinutes(30),
    billable: Boolean = true,
    workspaceId: Long = defaultWorkspaceId(),
    projectId: Long? = null,
    taskId: Long? = null,
    isDeleted: Boolean = false
): DatabaseTimeEntry = DatabaseTimeEntry(
    description = description,
    startTime = startTime,
    duration = duration,
    billable = billable,
    workspaceId = workspaceId,
    projectId = projectId,
    taskId = taskId,
    isDeleted = isDeleted
)

fun BaseDatabaseTest.prepareTimeEntryWithTagsIds(
    timeEntry: DatabaseTimeEntry,
    tagIds: List<Long>
): DatabaseTimeEntryWithTags {
    return DatabaseTimeEntryWithTags(timeEntry, tagIds)
}

fun BaseDatabaseTest.prepareProject(
    name: String = "Generated Project",
    color: String = "#C2C2C2",
    active: Boolean = true,
    isPrivate: Boolean = true,
    billable: Boolean = true,
    workspaceId: Long = defaultWorkspaceId(),
    clientId: Long? = null
): DatabaseProject = DatabaseProject(
    name = name,
    color = color,
    active = active,
    isPrivate = isPrivate,
    billable = billable,
    workspaceId = workspaceId,
    clientId = clientId
)

fun BaseDatabaseTest.prepareWorkspace(
    name: String = "Generated Workspace",
    features: List<WorkspaceFeature> = listOf(WorkspaceFeature.Pro)
): DatabaseWorkspace {
    return DatabaseWorkspace(name = name, features = features)
}

fun BaseDatabaseTest.prepareClient(
    name: String = "Generated Client",
    workspaceId: Long = defaultWorkspaceId()
): DatabaseClient = DatabaseClient(
    name = name,
    workspaceId = workspaceId
)

fun BaseDatabaseTest.prepareTag(
    name: String = "Generated Tag",
    workspaceId: Long = defaultWorkspaceId()
): DatabaseTag = DatabaseTag(
    name = name,
    workspaceId = workspaceId
)

fun BaseDatabaseTest.prepareTask(
    name: String = "Default Task",
    active: Boolean = true,
    workspaceId: Long = defaultWorkspaceId(),
    projectId: Long = createSimpleProject("Generated Default Project", workspaceId = workspaceId),
    userId: Long? = null
): DatabaseTask = DatabaseTask(
    name = name,
    active = active,
    projectId = projectId,
    workspaceId = workspaceId,
    userId = userId
)

fun BaseDatabaseTest.createProjects(vararg projects: DatabaseProject) =
    database.projectDao().insertAll(*projects)

fun BaseDatabaseTest.createSimpleProject(
    name: String = "Default Project",
    color: String = "#C2C2C2",
    active: Boolean = true,
    isPrivate: Boolean = true,
    billable: Boolean = true,
    workspaceId: Long = defaultWorkspaceId(),
    clientId: Long? = null
): Long {
    val project = DatabaseProject(
        name = name,
        color = color,
        active = active,
        isPrivate = isPrivate,
        billable = billable,
        clientId = clientId,
        workspaceId = workspaceId
    )
    return createProjects(project).first()
}

fun BaseDatabaseTest.createProWorkspace(workspaceName: String) =
    database.workspaceDao().insert(DatabaseWorkspace(name = workspaceName, features = listOf(WorkspaceFeature.Pro)))
