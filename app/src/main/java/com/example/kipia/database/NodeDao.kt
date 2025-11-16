// app/src/main/java/com/example/kipia/database/NodeDao.kt
package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete

@Dao
interface NodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(node: NodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<NodeEntity>)

    @Query("SELECT * FROM node_table WHERE tubeId = :tubeId")
    suspend fun getNodesByTubeId(tubeId: Long): List<NodeEntity>

    @Query("SELECT * FROM node_table WHERE id = :id")
    suspend fun getNodeById(id: Long): NodeEntity?

    @Delete
    suspend fun delete(node: NodeEntity)

    @Query("DELETE FROM node_table WHERE id = :id")
    suspend fun deleteById(id: Long)
}