package com.toggl.models.extensions

import com.toggl.models.domain.Workspace
import com.toggl.models.domain.WorkspaceFeature

fun Workspace.isPro() =
    features.contains(WorkspaceFeature.Pro)