package com.toggl.timer.common

import com.toggl.architecture.core.SettableValue
import com.toggl.models.domain.TimeEntry
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

fun createTimeEntry(
    id: Long,
    description: String = "",
    startTime: OffsetDateTime = OffsetDateTime.now(),
    duration: Duration? = null,
    billable: Boolean = false,
    projectId: Long? = null
) =
    TimeEntry(
        id,
        description,
        startTime,
        duration,
        billable,
        1,
        projectId,
        null,
        false
    )

fun <T> T.toSettableValue(setFunction: (T) -> Unit) =
    SettableValue({ this }, setFunction)