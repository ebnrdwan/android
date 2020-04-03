package com.toggl.database

import android.content.Context
import androidx.room.Room
import com.toggl.database.dao.ClientDao
import com.toggl.database.dao.ProjectDao
import com.toggl.database.dao.TagDao
import com.toggl.database.dao.TimeEntryDao
import com.toggl.database.dao.WorkspaceDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
    includes = [
        RoomDatabaseModule::class,
        DatabaseDaoModule::class
    ]
)
class DatabaseModule

@Module
class RoomDatabaseModule {
    @Provides
    @Singleton
    fun appDatabase(applicationContext: Context): TogglDatabase =
        Room.databaseBuilder(
            applicationContext,
            TogglRoomDatabase::class.java, "toggl.db"
        ).build()
}

@Module
class DatabaseDaoModule {
    @Provides
    @Singleton
    fun timeEntryDao(appDatabase: TogglDatabase): TimeEntryDao = appDatabase.timeEntryDao()

    @Provides
    @Singleton
    fun workspaceDao(appDatabase: TogglDatabase): WorkspaceDao = appDatabase.workspaceDao()

    @Provides
    @Singleton
    fun projectDao(appDatabase: TogglDatabase): ProjectDao = appDatabase.projectDao()

    @Provides
    @Singleton
    fun clientDao(appDatabase: TogglDatabase): ClientDao = appDatabase.clientDao()

    @Provides
    @Singleton
    fun tagsDao(appDatabase: TogglDatabase): TagDao = appDatabase.tagDao()
}
