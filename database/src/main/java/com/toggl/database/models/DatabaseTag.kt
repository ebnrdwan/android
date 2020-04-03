package com.toggl.database.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(entity = DatabaseWorkspace::class, parentColumns = ["id"], childColumns = ["workspaceId"])
    ]
)
data class DatabaseTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val workspaceId: Long
)