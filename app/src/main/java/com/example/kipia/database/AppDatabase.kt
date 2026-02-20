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
    version = 13, // НЕ МЕНЯЙТЕ эту версию без необходимости
    exportSchema = false // Отключаем экспорт схемы чтобы избежать проверок
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun controlPointDao(): ControlPointDao
    abstract fun pkuDao(): PKUDao
    abstract fun tubeDao(): TubeDao
    abstract fun nodeDao(): NodeDao
    abstract fun sectionDao(): SectionDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun detailedEquipmentDao(): DetailedEquipmentDao
    abstract fun remarkDao(): RemarkDao
    abstract fun eventDao(): EventDao

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
                    .fallbackToDestructiveMigration() // Всегда используем деструктивную миграцию
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Метод для принудительного пересоздания базы (для разработки)
        fun recreateInstance(context: Context): AppDatabase {
            INSTANCE = null
            return getInstance(context)
        }
    }
}