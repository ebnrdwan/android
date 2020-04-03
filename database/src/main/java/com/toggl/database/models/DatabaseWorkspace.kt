package com.toggl.database.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.toggl.models.domain.WorkspaceFeature

@Entity(tableName = "workspaces")
data class DatabaseWorkspace(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val features: List<WorkspaceFeature>
)