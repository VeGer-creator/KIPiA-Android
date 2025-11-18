// app/src/main/java/com/example/kipia/database/NodeDao.kt
package com.example.kipia.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update

@Dao
interface NodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(node: NodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nodes: List<NodeEntity>)

    @Query("SELECT * FROM node_table WHERE tubeId = :tubeId ORDER BY orderIndex ASC")
    suspend fun getNodesByTubeId(tubeId: Long): List<NodeEntity>

    @Query("SELECT * FROM node_table WHERE id = :id")
    suspend fun getNodeById(id: Long): NodeEntity?

    @Delete
    suspend fun delete(node: NodeEntity)

    @Query("DELETE FROM node_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE node_table SET name = :name WHERE id = :id")
    suspend fun update(id: Long, name: String)

    @Query("UPDATE node_table SET orderIndex = :orderIndex WHERE id = :id")
    suspend fun updateOrder(id: Long, orderIndex: Int)

    @Update
    suspend fun updateNode(node: NodeEntity)
}