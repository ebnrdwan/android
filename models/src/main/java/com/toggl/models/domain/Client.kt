package com.toggl.models.domain

data class Client(
    val id: Long,
    val name: String,
    val workspaceId: Long
)