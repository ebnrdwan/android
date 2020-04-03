package com.toggl.database.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(entity = DatabaseWorkspace::class, parentColumns = ["id"], childColumns = ["workspaceId"]),
        ForeignKey(entity = DatabaseClient::class, parentColumns = ["id"], childColumns = ["clientId"])
    ]
)
data class DatabaseProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val color: String,
    val active: Boolean,
    val isPrivate: Boolean,
    val billable: Boolean?,
    val workspaceId: Long,
    val clientId: Long?
)
