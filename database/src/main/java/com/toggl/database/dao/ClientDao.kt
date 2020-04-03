package com.toggl.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.toggl.database.models.DatabaseClient

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients")
    fun getAll(): List<DatabaseClient>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun getOne(id: Long): DatabaseClient

    @Insert
    fun insertAll(vararg clients: DatabaseClient): List<Long>

    @Insert
    fun insert(client: DatabaseClient): Long

    @Update
    fun update(client: DatabaseClient)

    @Update
    fun updateAll(clients: List<DatabaseClient>)

    @Delete
    fun delete(client: DatabaseClient)
}