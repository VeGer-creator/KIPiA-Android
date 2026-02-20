package com.example.kipia.sync

import android.util.Log
import com.example.kipia.database.AppDatabase
import com.example.kipia.sync.DataMerger.plus
import com.example.kipia.sync.EntityConverters.toEntity
import com.example.kipia.sync.EntityConverters.toSyncEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EnhancedDataMerger {

    suspend fun mergeWithDependencies(database: AppDatabase, incoming: SyncEntities): MergeResult {
        return withContext(Dispatchers.IO) {
            var result = MergeResult()

            try {
                // Шаг 1: Валидация зависимостей
                validateDependencies(incoming)

                // Шаг 2: Слияние в правильном порядке с учетом зависимостей
                result = result.plus(mergeControlPointsWithDeps(database, incoming.controlPoints))
                result = result.plus(mergePKUsWithDeps(database, incoming.pkus, incoming.controlPoints))
                result = result.plus(mergeTubesWithDeps(database, incoming.tubes, incoming.controlPoints))
                result = result.plus(mergeSectionsWithDeps(database, incoming.sections, incoming.pkus))
                result = result.plus(mergeNodesWithDeps(database, incoming.nodes, incoming.tubes))
                result = result.plus(mergeEquipmentWithDeps(database, incoming.equipment, incoming.nodes, incoming.sections))
                result = result.plus(mergeDetailedEquipmentWithDeps(database, incoming.detailedEquipment, incoming.nodes, incoming.sections))
                result = result.plus(mergeRemarksWithDeps(database, incoming.remarks, incoming.controlPoints))
                result = result.plus(mergeEventsWithDeps(database, incoming.events, incoming.controlPoints))

            } catch (e: DependencyException) {
                Log.e("EnhancedDataMerger", "Dependency error during merge", e)
                throw e
            } catch (e: Exception) {
                Log.e("EnhancedDataMerger", "Unexpected error during merge", e)
                throw e
            }

            result
        }
    }

    private fun validateDependencies(incoming: SyncEntities) {
        // Проверяем, что все внешние ключи существуют
        incoming.pkus.forEach { pku ->
            if (incoming.controlPoints.none { it.id == pku.controlPointId }) {
                throw DependencyException("PKU ${pku.id} references non-existent ControlPoint ${pku.controlPointId}")
            }
        }

        incoming.tubes.forEach { tube ->
            if (incoming.controlPoints.none { it.id == tube.controlPointId }) {
                throw DependencyException("Tube ${tube.id} references non-existent ControlPoint ${tube.controlPointId}")
            }
        }

        incoming.nodes.forEach { node ->
            if (incoming.tubes.none { it.id == node.tubeId }) {
                throw DependencyException("Node ${node.id} references non-existent Tube ${node.tubeId}")
            }
        }

        incoming.sections.forEach { section ->
            if (incoming.pkus.none { it.id == section.pkuId }) {
                throw DependencyException("Section ${section.id} references non-existent PKU ${section.pkuId}")
            }
        }

        incoming.equipment.forEach { equipment ->
            if (equipment.nodeId != null && incoming.nodes.none { it.id == equipment.nodeId }) {
                throw DependencyException("Equipment ${equipment.id} references non-existent Node ${equipment.nodeId}")
            }
            if (equipment.sectionId != null && incoming.sections.none { it.id == equipment.sectionId }) {
                throw DependencyException("Equipment ${equipment.id} references non-existent Section ${equipment.sectionId}")
            }
        }

        incoming.detailedEquipment.forEach { equipment ->
            if (equipment.nodeId != null && incoming.nodes.none { it.id == equipment.nodeId }) {
                throw DependencyException("DetailedEquipment ${equipment.id} references non-existent Node ${equipment.nodeId}")
            }
            if (equipment.sectionId != null && incoming.sections.none { it.id == equipment.sectionId }) {
                throw DependencyException("DetailedEquipment ${equipment.id} references non-existent Section ${equipment.sectionId}")
            }
        }

        incoming.remarks.forEach { remark ->
            if (incoming.controlPoints.none { it.id == remark.controlPointId }) {
                throw DependencyException("Remark ${remark.id} references non-existent ControlPoint ${remark.controlPointId}")
            }
        }

        incoming.events.forEach { event ->
            if (incoming.controlPoints.none { it.id == event.controlPointId }) {
                throw DependencyException("Event ${event.id} references non-existent ControlPoint ${event.controlPointId}")
            }
        }
    }

    private suspend fun mergeControlPointsWithDeps(
        database: AppDatabase,
        incoming: List<ControlPointSyncEntity>
    ): MergeResult {
        // Базовая реализация слияния
        return DataMerger.mergeControlPoints(database, incoming)
    }

    private suspend fun mergePKUsWithDeps(
        database: AppDatabase,
        incomingPKUs: List<PKUSyncEntity>,
        incomingControlPoints: List<ControlPointSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingPKUs = database.pkuDao().getAllPKUs()
        val existingMap = existingPKUs.associateBy { it.id }

        for (incomingPKU in incomingPKUs) {
            // Проверяем, что ControlPoint существует
            if (incomingControlPoints.any { it.id == incomingPKU.controlPointId }) {
                val existingPKU = existingMap[incomingPKU.id]

                if (existingPKU == null) {
                    database.pkuDao().insert(incomingPKU.toEntity())
                    added++
                } else {
                    val resolvedEntity = ConflictResolver.resolveConflict(
                        existingPKU.toSyncEntity("local"),
                        incomingPKU,
                        System.currentTimeMillis(), // В реальности нужно хранить timestamp
                        incomingPKU.lastModified
                    ).toEntity()

                    database.pkuDao().update(
                        resolvedEntity.id,
                        resolvedEntity.name,
                        resolvedEntity.description
                    )
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping PKU ${incomingPKU.id} - ControlPoint ${incomingPKU.controlPointId} not found")
            }
        }

        return MergeResult(pkusAdded = added, pkusUpdated = updated)
    }

    private suspend fun mergeTubesWithDeps(
        database: AppDatabase,
        incomingTubes: List<TubeSyncEntity>,
        incomingControlPoints: List<ControlPointSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingTubes = database.tubeDao().getAllTubes()
        val existingMap = existingTubes.associateBy { it.id }

        for (incomingTube in incomingTubes) {
            // Проверяем, что ControlPoint существует
            if (incomingControlPoints.any { it.id == incomingTube.controlPointId }) {
                val existingTube = existingMap[incomingTube.id]

                if (existingTube == null) {
                    database.tubeDao().insert(incomingTube.toEntity())
                    added++
                } else {
                    val resolvedEntity = ConflictResolver.resolveConflict(
                        existingTube.toSyncEntity("local"),
                        incomingTube,
                        System.currentTimeMillis(),
                        incomingTube.lastModified
                    ).toEntity()

                    database.tubeDao().update(resolvedEntity.id, resolvedEntity.name)
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping Tube ${incomingTube.id} - ControlPoint ${incomingTube.controlPointId} not found")
            }
        }

        return MergeResult(tubesAdded = added, tubesUpdated = updated)
    }

    private suspend fun mergeSectionsWithDeps(
        database: AppDatabase,
        incomingSections: List<SectionSyncEntity>,
        incomingPKUs: List<PKUSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingSections = database.sectionDao().getAllSections()
        val existingMap = existingSections.associateBy { it.id }

        for (incomingSection in incomingSections) {
            // Проверяем, что PKU существует
            if (incomingPKUs.any { it.id == incomingSection.pkuId }) {
                val existingSection = existingMap[incomingSection.id]

                if (existingSection == null) {
                    database.sectionDao().insert(incomingSection.toEntity())
                    added++
                } else {
                    val resolvedEntity = ConflictResolver.resolveConflict(
                        existingSection.toSyncEntity("local"),
                        incomingSection,
                        System.currentTimeMillis(),
                        incomingSection.lastModified
                    ).toEntity()

                    database.sectionDao().update(resolvedEntity)
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping Section ${incomingSection.id} - PKU ${incomingSection.pkuId} not found")
            }
        }

        return MergeResult(sectionsAdded = added, sectionsUpdated = updated)
    }

    private suspend fun mergeNodesWithDeps(
        database: AppDatabase,
        incomingNodes: List<NodeSyncEntity>,
        incomingTubes: List<TubeSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingNodes = database.nodeDao().getAllNodes()
        val existingMap = existingNodes.associateBy { it.id }

        for (incomingNode in incomingNodes) {
            // Проверяем, что Tube существует
            if (incomingTubes.any { it.id == incomingNode.tubeId }) {
                val existingNode = existingMap[incomingNode.id]

                if (existingNode == null) {
                    database.nodeDao().insert(incomingNode.toEntity())
                    added++
                } else {
                    val resolvedEntity = ConflictResolver.resolveConflict(
                        existingNode.toSyncEntity("local"),
                        incomingNode,
                        System.currentTimeMillis(),
                        incomingNode.lastModified
                    ).toEntity()

                    database.nodeDao().update(resolvedEntity.id, resolvedEntity.name)
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping Node ${incomingNode.id} - Tube ${incomingNode.tubeId} not found")
            }
        }

        return MergeResult(nodesAdded = added, nodesUpdated = updated)
    }

    private suspend fun mergeEquipmentWithDeps(
        database: AppDatabase,
        incomingEquipment: List<EquipmentSyncEntity>,
        incomingNodes: List<NodeSyncEntity>,
        incomingSections: List<SectionSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingEquipment = database.equipmentDao().getAllEquipment()
        val existingMap = existingEquipment.associateBy { it.id }

        for (incomingEq in incomingEquipment) {
            // Проверяем зависимости
            val nodeExists = incomingEq.nodeId == null || incomingNodes.any { it.id == incomingEq.nodeId }
            val sectionExists = incomingEq.sectionId == null || incomingSections.any { it.id == incomingEq.sectionId }

            if (nodeExists && sectionExists) {
                val existingEq = existingMap[incomingEq.id]

                if (existingEq == null) {
                    database.equipmentDao().insert(incomingEq.toEntity())
                    added++
                } else {
                    val resolvedEntity = ConflictResolver.resolveConflict(
                        existingEq.toSyncEntity("local"),
                        incomingEq,
                        System.currentTimeMillis(),
                        incomingEq.lastModified
                    ).toEntity()

                    database.equipmentDao().update(resolvedEntity)
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping Equipment ${incomingEq.id} - dependencies not found")
            }
        }

        return MergeResult(equipmentAdded = added, equipmentUpdated = updated)
    }

    private suspend fun mergeDetailedEquipmentWithDeps(
        database: AppDatabase,
        incomingEquipment: List<DetailedEquipmentSyncEntity>,
        incomingNodes: List<NodeSyncEntity>,
        incomingSections: List<SectionSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingEquipment = database.detailedEquipmentDao().getAllDetailedEquipment()
        val existingMap = existingEquipment.associateBy { it.id }

        for (incomingEq in incomingEquipment) {
            // Проверяем зависимости
            val nodeExists = incomingEq.nodeId == null || incomingNodes.any { it.id == incomingEq.nodeId }
            val sectionExists = incomingEq.sectionId == null || incomingSections.any { it.id == incomingEq.sectionId }

            if (nodeExists && sectionExists) {
                val existingEq = existingMap[incomingEq.id]

                if (existingEq == null) {
                    database.detailedEquipmentDao().insert(incomingEq.toEntity())
                    added++
                } else {
                    val resolvedEntity = ConflictResolver.resolveConflict(
                        existingEq.toSyncEntity("local"),
                        incomingEq,
                        System.currentTimeMillis(),
                        incomingEq.lastModified
                    ).toEntity()

                    database.detailedEquipmentDao().update(resolvedEntity)
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping DetailedEquipment ${incomingEq.id} - dependencies not found")
            }
        }

        return MergeResult(detailedEquipmentAdded = added, detailedEquipmentUpdated = updated)
    }

    private suspend fun mergeRemarksWithDeps(
        database: AppDatabase,
        incomingRemarks: List<RemarkSyncEntity>,
        incomingControlPoints: List<ControlPointSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingRemarks = database.remarkDao().getAllRemarks()
        val existingMap = existingRemarks.associateBy { it.id }

        for (incomingRemark in incomingRemarks) {
            // Проверяем, что ControlPoint существует
            if (incomingControlPoints.any { it.id == incomingRemark.controlPointId }) {
                val existingRemark = existingMap[incomingRemark.id]

                if (existingRemark == null) {
                    database.remarkDao().insert(incomingRemark.toEntity())
                    added++
                } else {
                    val resolvedEntity = when {
                        existingRemark is com.example.kipia.database.RemarkEntity &&
                                incomingRemark is RemarkSyncEntity -> {
                            val localSync = existingRemark.toSyncEntity("local")
                            ConflictResolver.resolveCriticalDataConflict(localSync, incomingRemark) as RemarkSyncEntity
                        }
                        else -> incomingRemark
                    }.toEntity()

                    database.remarkDao().update(resolvedEntity)
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping Remark ${incomingRemark.id} - ControlPoint ${incomingRemark.controlPointId} not found")
            }
        }

        return MergeResult(remarksAdded = added, remarksUpdated = updated)
    }

    private suspend fun mergeEventsWithDeps(
        database: AppDatabase,
        incomingEvents: List<EventSyncEntity>,
        incomingControlPoints: List<ControlPointSyncEntity>
    ): MergeResult {
        var added = 0
        var updated = 0

        val existingEvents = database.eventDao().getAllEvents()
        val existingMap = existingEvents.associateBy { it.id }

        for (incomingEvent in incomingEvents) {
            // Проверяем, что ControlPoint существует
            if (incomingControlPoints.any { it.id == incomingEvent.controlPointId }) {
                val existingEvent = existingMap[incomingEvent.id]

                if (existingEvent == null) {
                    // Используем конвертер
                    database.eventDao().insert(incomingEvent.toEntity())
                    added++
                } else {
                    val localSync = existingEvent.toSyncEntity("local")
                    val resolvedSync = ConflictResolver.resolveCriticalDataConflict(
                        localSync,
                        incomingEvent
                    ) as EventSyncEntity

                    // Используем конвертер
                    database.eventDao().update(resolvedSync.toEntity())
                    updated++
                }
            } else {
                Log.w("EnhancedDataMerger", "Skipping Event ${incomingEvent.id} - ControlPoint ${incomingEvent.controlPointId} not found")
            }
        }

        return MergeResult(eventsAdded = added, eventsUpdated = updated)
    }
}

class DependencyException(message: String) : Exception(message)