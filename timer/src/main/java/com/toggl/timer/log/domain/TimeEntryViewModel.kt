package com.toggl.timer.log.domain

import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

sealed class TimeEntryViewModel

data class FlatTimeEntryViewModel(
    val id: Long,
    val description: String,
    val startTime: OffsetDateTime,
    val duration: Duration,
    val project: ProjectViewModel?,
    val client: ClientViewModel?,
    val billable: Boolean
) : TimeEntryViewModel()

data class ProjectViewModel(
    val id: Long,
    val name: String,
    val color: String
)

data class ClientViewModel(
    val id: Long,
    val name: String
)

data class DayHeaderViewModel(
    val dayTitle: String,
    val totalDuration: Duration
) : TimeEntryViewModel()

data class TimeEntryGroupViewModel(
    val groupId: Long,
    val timeEntryIds: List<Long>,
    val isExpanded: Boolean,
    val description: String,
    val duration: Duration,
    val project: ProjectViewModel?,
    val client: ClientViewModel?,
    val billable: Boolean
) : TimeEntryViewModel()