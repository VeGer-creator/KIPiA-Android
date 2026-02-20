package com.example.kipia.sync

import com.example.kipia.database.AppDatabase
import com.example.kipia.sync.EntityConverters.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MergeResult(
    val controlPointsAdded: Int = 0,
    val controlPointsUpdated: Int = 0,
    val pkusAdded: Int = 0,
    val pkusUpdated: Int = 0,
    val tubesAdded: Int = 0,
    val tubesUpdated: Int = 0,
    val nodesAdded: Int = 0,
    val nodesUpdated: Int = 0,
    val sectionsAdded: Int = 0,
    val sectionsUpdated: Int = 0,
    val equipmentAdded: Int = 0,
    val equipmentUpdated: Int = 0,
    val detailedEquipmentAdded: Int = 0,
    val detailedEquipmentUpdated: Int = 0,
    val remarksAdded: Int = 0,
    val remarksUpdated: Int = 0,
    val eventsAdded: Int = 0,
    val eventsUpdated: Int = 0
) {
    fun plus(other: MergeResult): MergeResult {
        return MergeResult(
            controlPointsAdded = this.controlPointsAdded + other.controlPointsAdded,
            controlPointsUpdated = this.controlPointsUpdated + other.controlPointsUpdated,
            pkusAdded = this.pkusAdded + other.pkusAdded,
            pkusUpdated = this.pkusUpdated + other.pkusUpdated,
            tubesAdded = this.tubesAdded + other.tubesAdded,
            tubesUpdated = this.tubesUpdated + other.tubesUpdated,
            nodesAdded = this.nodesAdded + other.nodesAdded,
            nodesUpdated = this.nodesUpdated + other.nodesUpdated,
            sectionsAdded = this.sectionsAdded + other.sectionsAdded,
            sectionsUpdated = this.sectionsUpdated + other.sectionsUpdated,
            equipmentAdded = this.equipmentAdded + other.equipmentAdded,
            equipmentUpdated = this.equipmentUpdated + other.equipmentUpdated,
            detailedEquipmentAdded = this.detailedEquipmentAdded + other.detailedEquipmentAdded,
            detailedEquipmentUpdated = this.detailedEquipmentUpdated + other.detailedEquipmentUpdated,
            remarksAdded = this.remarksAdded + other.remarksAdded,
            remarksUpdated = this.remarksUpdated + other.remarksUpdated,
            eventsAdded = this.eventsAdded + other.eventsAdded,
            eventsUpdated = this.eventsUpdated + other.eventsUpdated
        )
    }
}

object DataMerger {

    suspend fun mergeData(database: AppDatabase, incoming: SyncEntities): MergeResult {
        return withContext(Dispatchers.IO) {
            var result = MergeResult()

            try {
                // Слияние ControlPoints (родительская сущность)
                result += mergeControlPoints(database, incoming.controlPoints)

                // Слияние PKUs
                result += mergePKUs(database, incoming.pkus)

                // Слияние Tubes
                result += mergeTubes(database, incoming.tubes)

                // Слияние Sections
                result += mergeSections(database, incoming.sections)

                // Слияние Nodes
                result += mergeNodes(database, incoming.nodes)

                // Слияние Equipment
                result += mergeEquipment(database, incoming.equipment)

                // Слияние DetailedEquipment
                result += mergeDetailedEquipment(database, incoming.detailedEquipment)

                // Слияние Remarks
                result += mergeRemarks(database, incoming.remarks)

                // Слияние Events
                result += mergeEvents(database, incoming.events)

            } catch (e: Exception) {
                throw e
            }

            result
        }
    }

    suspend fun mergeControlPoints(
        database: AppDatabase,
        incoming: List<ControlPointSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.controlPointDao().getAllControlPoints()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                // Новая сущность - добавляем
                database.controlPointDao().insert(incomingEntity.toEntity())
                added++
            } else {
                // Конфликт - применяем правила слияния
                if (shouldUpdate(existingEntity, incomingEntity)) {
                    database.controlPointDao().update(
                        incomingEntity.id,
                        incomingEntity.name,
                        incomingEntity.description
                    )
                    updated++
                }
            }
        }

        return MergeResult(controlPointsAdded = added, controlPointsUpdated = updated)
    }

    internal suspend fun mergePKUs(
        database: AppDatabase,
        incoming: List<PKUSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.pkuDao().getAllPKUs()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.pkuDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.pkuDao().update(
                    incomingEntity.id,
                    incomingEntity.name,
                    incomingEntity.description
                )
                updated++
            }
        }

        return MergeResult(pkusAdded = added, pkusUpdated = updated)
    }

    suspend fun mergeTubes(
        database: AppDatabase,
        incoming: List<TubeSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.tubeDao().getAllTubes()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.tubeDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.tubeDao().update(incomingEntity.id, incomingEntity.name)
                updated++
            }
        }

        return MergeResult(tubesAdded = added, tubesUpdated = updated)
    }

    internal suspend fun mergeNodes(
        database: AppDatabase,
        incoming: List<NodeSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.nodeDao().getAllNodes()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.nodeDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.nodeDao().update(incomingEntity.id, incomingEntity.name)
                updated++
            }
        }

        return MergeResult(nodesAdded = added, nodesUpdated = updated)
    }

    internal suspend fun mergeSections(
        database: AppDatabase,
        incoming: List<SectionSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.sectionDao().getAllSections()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.sectionDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.sectionDao().update(incomingEntity.toEntity())
                updated++
            }
        }

        return MergeResult(sectionsAdded = added, sectionsUpdated = updated)
    }

    internal suspend fun mergeEquipment(
        database: AppDatabase,
        incoming: List<EquipmentSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.equipmentDao().getAllEquipment()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.equipmentDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.equipmentDao().update(incomingEntity.toEntity())
                updated++
            }
        }

        return MergeResult(equipmentAdded = added, equipmentUpdated = updated)
    }

    internal suspend fun mergeDetailedEquipment(
        database: AppDatabase,
        incoming: List<DetailedEquipmentSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.detailedEquipmentDao().getAllDetailedEquipment()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.detailedEquipmentDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.detailedEquipmentDao().update(incomingEntity.toEntity())
                updated++
            }
        }

        return MergeResult(detailedEquipmentAdded = added, detailedEquipmentUpdated = updated)
    }

    internal suspend fun mergeRemarks(
        database: AppDatabase,
        incoming: List<RemarkSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.remarkDao().getAllRemarks()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.remarkDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.remarkDao().update(incomingEntity.toEntity())
                updated++
            }
        }

        return MergeResult(remarksAdded = added, remarksUpdated = updated)
    }

    internal suspend fun mergeEvents(
        database: AppDatabase,
        incoming: List<EventSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existing = database.eventDao().getAllEvents()
        val existingMap = existing.associateBy { it.id }

        for (incomingEntity in incoming) {
            val existingEntity = existingMap[incomingEntity.id]

            if (existingEntity == null) {
                database.eventDao().insert(incomingEntity.toEntity())
                added++
            } else if (shouldUpdate(existingEntity, incomingEntity)) {
                database.eventDao().update(incomingEntity.toEntity())
                updated++
            }
        }

        return MergeResult(eventsAdded = added, eventsUpdated = updated)
    }

    // Правила слияния: обновляем если входящие данные новее
    internal fun shouldUpdate(existing: Any, incoming: Any): Boolean {
        return when {
            existing is com.example.kipia.database.ControlPointEntity &&
                    incoming is ControlPointSyncEntity -> {
                // Простая логика - всегда обновляем для демонстрации
                true
            }
            // Добавьте другие типы сущностей по необходимости
            else -> true
        }
    }

    internal operator fun MergeResult.plus(other: MergeResult): MergeResult {
        return MergeResult(
            controlPointsAdded = this.controlPointsAdded + other.controlPointsAdded,
            controlPointsUpdated = this.controlPointsUpdated + other.controlPointsUpdated,
            pkusAdded = this.pkusAdded + other.pkusAdded,
            pkusUpdated = this.pkusUpdated + other.pkusUpdated,
            tubesAdded = this.tubesAdded + other.tubesAdded,
            tubesUpdated = this.tubesUpdated + other.tubesUpdated,
            nodesAdded = this.nodesAdded + other.nodesAdded,
            nodesUpdated = this.nodesUpdated + other.nodesUpdated,
            sectionsAdded = this.sectionsAdded + other.sectionsAdded,
            sectionsUpdated = this.sectionsUpdated + other.sectionsUpdated,
            equipmentAdded = this.equipmentAdded + other.equipmentAdded,
            equipmentUpdated = this.equipmentUpdated + other.equipmentUpdated,
            detailedEquipmentAdded = this.detailedEquipmentAdded + other.detailedEquipmentAdded,
            detailedEquipmentUpdated = this.detailedEquipmentUpdated + other.detailedEquipmentUpdated,
            remarksAdded = this.remarksAdded + other.remarksAdded,
            remarksUpdated = this.remarksUpdated + other.remarksUpdated,
            eventsAdded = this.eventsAdded + other.eventsAdded,
            eventsUpdated = this.eventsUpdated + other.eventsUpdated
        )
    }
}