package com.toggl.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.toggl.database.models.DatabaseWorkspace

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspaces")
    fun getAll(): List<DatabaseWorkspace>

    @Query("SELECT * FROM workspaces WHERE id = :id")
    fun getOne(id: Long): DatabaseWorkspace

    @Insert
    fun insertAll(vararg databaseWorkspaces: DatabaseWorkspace): List<Long>

    @Insert
    fun insert(databaseWorkspace: DatabaseWorkspace): Long

    @Update
    fun update(databaseWorkspace: DatabaseWorkspace)

    @Update
    fun updateAll(databaseWorkspaces: List<DatabaseWorkspace>)

    @Delete
    fun delete(databaseWorkspace: DatabaseWorkspace)
}