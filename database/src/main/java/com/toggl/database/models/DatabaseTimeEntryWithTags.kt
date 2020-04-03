package com.toggl.database.models

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class DatabaseTimeEntryWithTags(
    @Embedded
    val timeEntry: DatabaseTimeEntry,

    @Relation(
        parentColumn = "id",
        entity = DatabaseTag::class,
        entityColumn = "id",
        associateBy = Junction(
            value = DatabaseTimeEntryTag::class,
            parentColumn = "timeEntryId",
            entityColumn = "tagId"
        ),
        projection = ["id"]
    )
    var tags: List<Long>
)