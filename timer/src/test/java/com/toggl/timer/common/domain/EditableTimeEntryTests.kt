package com.toggl.timer.common.domain

import com.toggl.timer.common.createTimeEntry
import io.kotlintest.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime

@DisplayName("EditableTimeEntry")
internal class EditableTimeEntryTests {

    @Test
    fun `for groups, should sum durations of TEs in the represented group and have no start time`() {
        val timeEntries = listOf(
            createTimeEntry(1, duration = Duration.ofMinutes(3)),
            createTimeEntry(2, duration = Duration.ofMinutes(5)),
            createTimeEntry(3)
        )

        val editable = EditableTimeEntry.fromGroup(
            listOf(1, 2),
            timeEntries[0],
            timeEntries.associateBy { it.id }
        )

        editable.duration shouldBe Duration.ofMinutes(8)
        editable.startTime shouldBe null
    }

    @Test
    fun `for single TEs, should copy over the duration and start time`() {
        val now = OffsetDateTime.now()
        val timeEntry = createTimeEntry(1, duration = Duration.ofMinutes(3), startTime = now)

        val editable = EditableTimeEntry.fromSingle(timeEntry)

        editable.duration shouldBe Duration.ofMinutes(3)
        editable.startTime shouldBe now
    }
}