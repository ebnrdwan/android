package com.toggl.domain.loading

import com.toggl.architecture.DispatcherProvider
import com.toggl.architecture.core.Effect
import com.toggl.repository.interfaces.TagRepository
import kotlinx.coroutines.withContext

class LoadTagsEffect(
    private val repository: TagRepository,
    private val dispatcherProvider: DispatcherProvider
) : Effect<LoadingAction> {
    override suspend fun execute(): LoadingAction? =
        withContext(dispatcherProvider.io) {
            val tags = repository.loadTags()
            LoadingAction.TagsLoaded(tags)
        }
}