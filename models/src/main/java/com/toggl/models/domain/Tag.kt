package com.toggl.models.domain

data class Tag(
    val id: Long = 0,
    val name: String,
    val workspaceId: Long
)