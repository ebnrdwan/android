package com.toggl.models.domain

data class Workspace(
    val id: Long = 0,
    val name: String,
    val features: List<WorkspaceFeature>
)