// app/src/main/java/com/example/kipia/database/AppDatabase.kt
package com.example.kipia.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [
        ControlPointEntity::class,
        PKUEntity::class,
        TubeEntity::class,
        NodeEntity::class,
        SectionEntity::class,
        EquipmentEntity::class,
        DetailedEquipmentEntity::class,
        RemarkEntity::class,
        EventEntity::class
    ],
    version = 9, // Увеличиваем версию
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun controlPointDao(): ControlPointDao
    abstract fun pkuDao(): PKUDao
    abstract fun tubeDao(): TubeDao
    abstract fun nodeDao(): NodeDao
    abstract fun sectionDao(): SectionDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun detailedEquipmentDao(): DetailedEquipmentDao // ДОБАВИТЬ
    abstract fun remarkDao(): RemarkDao // ДОБАВИТЬ
    abstract fun eventDao(): EventDao // ДОБАВИТЬ

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kipia_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}