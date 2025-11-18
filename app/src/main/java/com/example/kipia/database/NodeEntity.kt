// app/src/main/java/com/example/kipia/database/NodeEntity.kt
package com.example.kipia.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "node_table",
    foreignKeys = [
        ForeignKey(
            entity = TubeEntity::class,
            parentColumns = ["id"],
            childColumns = ["tubeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["tubeId"])] // ДОБАВИТЬ ЭТУ СТРОКУ
)
data class NodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val tubeId: Long,
    val nodeType: String,
    val orderIndex: Int = 0
)