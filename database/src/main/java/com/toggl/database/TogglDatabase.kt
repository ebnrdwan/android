package com.toggl.database

import com.toggl.database.dao.ClientDao
import com.toggl.database.dao.ProjectDao
import com.toggl.database.dao.TagDao
import com.toggl.database.dao.TimeEntryDao
import com.toggl.database.dao.WorkspaceDao

interface TogglDatabase {
    fun timeEntryDao(): TimeEntryDao
    fun projectDao(): ProjectDao
    fun workspaceDao(): WorkspaceDao
    fun clientDao(): ClientDao
    fun tagDao(): TagDao
}
