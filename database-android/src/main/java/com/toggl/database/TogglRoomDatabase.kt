package com.toggl.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.toggl.database.models.DatabaseClient
import com.toggl.database.models.DatabaseProject
import com.toggl.database.models.DatabaseTag
import com.toggl.database.models.DatabaseTimeEntry
import com.toggl.database.models.DatabaseTimeEntryTag
import com.toggl.database.models.DatabaseWorkspace

@Database(
    entities = [
        DatabaseTimeEntry::class,
        DatabaseProject::class,
        DatabaseWorkspace::class,
        DatabaseClient::class,
        DatabaseTag::class,
        DatabaseTimeEntryTag::class
    ],
    version = 1
)
@TypeConverters(TogglTypeConverters::class)
abstract class TogglRoomDatabase : RoomDatabase(), TogglDatabase
