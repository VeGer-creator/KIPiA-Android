package com.example.kipia.sync

import android.util.Log

object ConflictResolver {
    private const val TAG = "ConflictResolver"

    /**
     * Правила разрешения конфликтов для разных типов сущностей
     */
    fun <T> resolveConflict(
        local: T,
        remote: T,
        localTimestamp: Long,
        remoteTimestamp: Long
    ): T {
        return when {
            // Если удаленные данные новее - используем их
            remoteTimestamp > localTimestamp -> {
                Log.d(TAG, "Using remote data (newer timestamp)")
                remote
            }
            // Если локальные данные новее - используем их
            localTimestamp > remoteTimestamp -> {
                Log.d(TAG, "Using local data (newer timestamp)")
                local
            }
            // Если временные метки равны - используем более полные данные
            else -> resolveByDataCompleteness(local, remote)
        }
    }

    private fun <T> resolveByDataCompleteness(local: T, remote: T): T {
        val localCompleteness = calculateDataCompleteness(local)
        val remoteCompleteness = calculateDataCompleteness(remote)

        return if (remoteCompleteness > localCompleteness) {
            Log.d(TAG, "Using remote data (more complete)")
            remote
        } else {
            Log.d(TAG, "Using local data (more complete)")
            local
        }
    }

    private fun <T> calculateDataCompleteness(entity: T): Int {
        var completeness = 0

        when (entity) {
            is ControlPointSyncEntity -> {
                if (entity.name.isNotBlank()) completeness += 30
                if (entity.description.isNotBlank()) completeness += 20
            }
            is EquipmentSyncEntity -> {
                if (entity.name.isNotBlank()) completeness += 20
                if (entity.model.isNotBlank()) completeness += 15
                if (entity.serialNumber.isNotBlank()) completeness += 15
                if (entity.photoPaths.isNotBlank()) completeness += 10
            }
            is RemarkSyncEntity -> {
                if (entity.title.isNotBlank()) completeness += 25
                if (entity.description.isNotBlank()) completeness += 15
                if (entity.photos.isNotBlank()) completeness += 10
            }
            // Добавьте правила для других типов сущностей
        }

        return completeness
    }

    /**
     * Специальные правила для критических данных
     */
    fun resolveCriticalDataConflict(local: Any, remote: Any): Any {
        return when {
            // Для замечаний с высоким приоритетом всегда предпочитаем удаленные данные
            local is RemarkSyncEntity && remote is RemarkSyncEntity -> {
                if (remote.priority == "Высокий" && local.priority != "Высокий") {
                    Log.d(TAG, "Using remote remark data (high priority)")
                    remote
                } else if (local.priority == "Высокий" && remote.priority != "Высокий") {
                    Log.d(TAG, "Using local remark data (high priority)")
                    local
                } else {
                    resolveConflict(local, remote, local.lastModified, remote.lastModified)
                }
            }
            // Для завершенных событий предпочитаем данные, которые были завершены
            local is EventSyncEntity && remote is EventSyncEntity -> {
                if (remote.isCompleted && !local.isCompleted) {
                    Log.d(TAG, "Using remote event data (completed)")
                    remote
                } else if (local.isCompleted && !remote.isCompleted) {
                    Log.d(TAG, "Using local event data (completed)")
                    local
                } else {
                    resolveConflict(local, remote, local.lastModified, remote.lastModified)
                }
            }
            else -> resolveConflict(local, remote, 0, 0)
        }
    }
}