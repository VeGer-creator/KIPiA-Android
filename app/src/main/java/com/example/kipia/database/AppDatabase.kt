// app/src/main/java/com/example/kipia/database/AppDatabase.kt
package com.example.kipia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ControlPointEntity::class, // Добавляем КП
        PKUEntity::class,         // Обновляем ПКУ
        TubeEntity::class,        // Обновляем Трубу
        NodeEntity::class,        // Добавляем Узел
        SectionEntity::class,     // Добавляем Отсек
        EquipmentEntity::class    // Обновляем Оборудование
    ],
    version = 4, // Увеличиваем версию базы данных
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun controlPointDao(): ControlPointDao // Добавляем DAO для КП
    abstract fun pkuDao(): PKUDao
    abstract fun tubeDao(): TubeDao
    abstract fun nodeDao(): NodeDao // Добавляем DAO для Узла
    abstract fun sectionDao(): SectionDao // Добавляем DAO для Отсека
    abstract fun equipmentDao(): EquipmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
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