package com.toggl.repository.interfaces

import com.toggl.models.domain.Tag

interface TagRepository {
    suspend fun loadTags(): List<Tag>
}