package com.toggl.database.models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "time_entries_tags",
    primaryKeys = ["timeEntryId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = DatabaseTimeEntry::class, parentColumns = ["id"], childColumns = ["timeEntryId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = DatabaseTag::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class DatabaseTimeEntryTag(
    val timeEntryId: Long,
    val tagId: Long
)