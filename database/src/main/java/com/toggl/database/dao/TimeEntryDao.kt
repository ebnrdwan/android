package com.toggl.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.toggl.database.models.DatabaseTimeEntry
import com.toggl.database.models.DatabaseTimeEntryTag
import com.toggl.database.models.DatabaseTimeEntryWithTags

@Dao
interface TimeEntryDao {
    // TimeEntries only

    @Query("SELECT * FROM time_entries WHERE NOT isDeleted AND duration is null")
    fun getAllRunningTimeEntries(): List<DatabaseTimeEntry>

    @Query("SELECT * FROM time_entries WHERE NOT isDeleted AND id = :id")
    fun getOneTimeEntry(id: Long): DatabaseTimeEntry

    @Insert
    fun insertAllTimeEntries(vararg databaseTimeEntries: DatabaseTimeEntry): List<Long>

    @Insert
    fun insertTimeEntry(databaseTimeEntries: DatabaseTimeEntry): Long

    @Update
    fun updateTimeEntry(databaseTimeEntry: DatabaseTimeEntry)

    @Update
    fun updateAllTimeEntries(databaseTimeEntries: List<DatabaseTimeEntry>)

    @Delete
    fun deleteTimeEntry(databaseTimeEntry: DatabaseTimeEntry)

    // TimeEntries & Tags

    @Transaction
    @Query("SELECT * FROM time_entries WHERE NOT isDeleted")
    fun getAllTimeEntriesWithTags(): List<DatabaseTimeEntryWithTags>

    @Transaction
    @Update
    fun updateTimeEntryWithTags(databaseTimeEntryWithTags: DatabaseTimeEntryWithTags) {
        insertAllTimeEntryTagsPairs(databaseTimeEntryWithTags.tags.map {
            DatabaseTimeEntryTag(databaseTimeEntryWithTags.timeEntry.id, it)
        })
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAllTimeEntryTagsPairs(databaseTimeEntryTags: List<DatabaseTimeEntryTag>)
}
