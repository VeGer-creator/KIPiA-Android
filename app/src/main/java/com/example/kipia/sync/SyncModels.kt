package com.example.kipia.sync

import android.util.Log
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
data class SyncPackage(
    val deviceId: String,
    val timestamp: Long,
    val entities: SyncEntities,
    val fileAttachments: List<FileAttachment> = emptyList(),
    @Contextual
    val fileMetadata: List<@Contextual FileMetadata> = emptyList() // Новое поле для метаданных файлов
)

@Serializable
data class SyncEntities(
    val controlPoints: List<ControlPointSyncEntity> = emptyList(),
    val pkus: List<PKUSyncEntity> = emptyList(),
    val tubes: List<TubeSyncEntity> = emptyList(),
    val nodes: List<NodeSyncEntity> = emptyList(),
    val sections: List<SectionSyncEntity> = emptyList(),
    val equipment: List<EquipmentSyncEntity> = emptyList(),
    val detailedEquipment: List<DetailedEquipmentSyncEntity> = emptyList(),
    val remarks: List<RemarkSyncEntity> = emptyList(),
    val events: List<EventSyncEntity> = emptyList()
)

@Serializable
data class FileAttachment(
    val entityType: String,
    val entityId: Long,
    val fieldName: String,
    val fileName: String,
    val fileData: String, // base64 encoded
    val fileType: String = "photo", // Новое поле: photo/document/unknown
    val relativePath: String? = null // Новое поле: относительный путь
) {
    // Вспомогательное свойство для получения строкового ID
    val stringId: String get() = entityId.toString()

    // Вспомогательное свойство для ключа
    val entityKey: String get() = "${entityType}_${entityId}_${fieldName}"
}

@Serializable
data class FileMetadata(
    val fileName: String,
    val fileSize: Long,
    val totalChunks: Int,
    val entityType: String,
    val entityId: String,
    val fileType: String,
    val originalPath: String? = null,
    val checksum: String = "",
    val lastModified: Long = System.currentTimeMillis()
)

// Sync Entity классы для всех сущностей
@Serializable
data class ControlPointSyncEntity(
    val id: Long,
    val name: String,
    val description: String,
    val lastModified: Long,
    val deviceId: String
)

@Serializable
data class PKUSyncEntity(
    val id: Long,
    val name: String,
    val description: String,
    val isCompleted: Boolean,
    val controlPointId: Long,
    val lastModified: Long,
    val deviceId: String
)

@Serializable
data class TubeSyncEntity(
    val id: Long,
    val name: String,
    val controlPointId: Long,
    val lastModified: Long,
    val deviceId: String
)

@Serializable
data class NodeSyncEntity(
    val id: Long,
    val name: String,
    val tubeId: Long,
    val nodeType: String,
    val orderIndex: Int,
    val lastModified: Long,
    val deviceId: String
)

@Serializable
data class SectionSyncEntity(
    val id: Long,
    val name: String,
    val pkuId: Long,
    val lastModified: Long,
    val deviceId: String
)

@Serializable
data class EquipmentSyncEntity(
    val id: Long,
    val name: String,
    val nodeId: Long?,
    val sectionId: Long?,
    val model: String,
    val manufacturer: String,
    val serialNumber: String,
    val nominal: String,
    val verificationYear: String,
    val photoPaths: String,
    val lastModified: Long,
    val deviceId: String
) {
    // Вспомогательное свойство для получения списка путей
    val photoList: List<String>
        get() = if (photoPaths.isNotBlank()) {
            photoPaths.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
    // Убрана проверка photos.isNotBlank() - поля нет
}

@Serializable
data class DetailedEquipmentSyncEntity(
    val id: Long,
    val equipmentType: String,
    val name: String,
    val nodeId: Long?,
    val sectionId: Long?,
    val model: String,
    val manufacturer: String,
    val serialNumber: String,
    val productionYear: String,
    val verificationYear: String,
    val nominal: String,
    val pressureLimit: String,
    val softwareVersion: String,
    val mo: String,
    val mz: String,
    val mto: String,
    val mtz: String,
    val muo: String,
    val muz: String,
    val outputContacts: String,
    val photoPaths: String,
    val lastModified: Long,
    val deviceId: String,
    val photos: String = "" // Новое поле для совместимости
) {
    // Вспомогательное свойство для получения списка путей
    val photoList: List<String>
        get() = if (photoPaths.isNotBlank()) {
            photoPaths.split(",").filter { it.isNotBlank() }
        } else if (photos.isNotBlank()) {
            photos.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
}

@Serializable
data class RemarkSyncEntity(
    val id: Long,
    val controlPointId: Long,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val status: String,
    val createdDate: String,
    val deadline: String,
    val completedDate: String,
    val photos: String,
    val isArchived: Boolean,
    val lastModified: Long,
    val deviceId: String
) {
    // Вспомогательное свойство для получения списка путей
    val photoList: List<String>
        get() = if (photos.isNotBlank()) {
            photos.split(",").filter { it.isNotBlank() }
        } else {
            emptyList()
        }
}

@Serializable
data class EventSyncEntity(
    val id: Long,
    val controlPointId: Long,
    val title: String,
    val description: String,
    val type: String,
    val date: String,
    val time: String,
    val isCompleted: Boolean,
    val participants: String,
    val lastModified: Long,
    val deviceId: String
)

// Конвертеры Entity <-> SyncEntity
object EntityConverters {

    // ControlPoint конвертеры
    fun ControlPointSyncEntity.toEntity() = com.example.kipia.database.ControlPointEntity(
        id = id,
        name = name,
        description = description
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация ControlPoint: $id - $name")
    }

    fun com.example.kipia.database.ControlPointEntity.toSyncEntity(deviceId: String) = ControlPointSyncEntity(
        id = id,
        name = name,
        description = description,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // PKU конвертеры
    fun PKUSyncEntity.toEntity() = com.example.kipia.database.PKUEntity(
        id = id,
        name = name,
        description = description,
        isCompleted = isCompleted,
        controlPointId = controlPointId
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация PKU: $id - $name")
    }

    fun com.example.kipia.database.PKUEntity.toSyncEntity(deviceId: String) = PKUSyncEntity(
        id = id,
        name = name,
        description = description,
        isCompleted = isCompleted,
        controlPointId = controlPointId,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // Tube конвертеры
    fun TubeSyncEntity.toEntity() = com.example.kipia.database.TubeEntity(
        id = id,
        name = name,
        controlPointId = controlPointId
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация Трубы: $id - $name")
    }

    fun com.example.kipia.database.TubeEntity.toSyncEntity(deviceId: String) = TubeSyncEntity(
        id = id,
        name = name,
        controlPointId = controlPointId,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // Node конвертеры
    fun NodeSyncEntity.toEntity() = com.example.kipia.database.NodeEntity(
        id = id,
        name = name,
        tubeId = tubeId,
        nodeType = nodeType,
        orderIndex = orderIndex
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация Узла: $id - $name")
    }

    fun com.example.kipia.database.NodeEntity.toSyncEntity(deviceId: String) = NodeSyncEntity(
        id = id,
        name = name,
        tubeId = tubeId,
        nodeType = nodeType,
        orderIndex = orderIndex,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // Section конвертеры
    fun SectionSyncEntity.toEntity() = com.example.kipia.database.SectionEntity(
        id = id,
        name = name,
        pkuId = pkuId
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация PKU секции: $id - $name")
    }

    fun com.example.kipia.database.SectionEntity.toSyncEntity(deviceId: String) = SectionSyncEntity(
        id = id,
        name = name,
        pkuId = pkuId,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // Equipment конвертеры
    fun EquipmentSyncEntity.toEntity() = com.example.kipia.database.EquipmentEntity(
        id = id,
        name = name,
        nodeId = nodeId,
        sectionId = sectionId,
        model = model,
        manufacturer = manufacturer,
        serialNumber = serialNumber,
        nominal = nominal,
        verificationYear = verificationYear,
        photoPaths = photoPaths
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация оборудования: $id - $name, фото: ${photoList.size}")
    }

    fun com.example.kipia.database.EquipmentEntity.toSyncEntity(deviceId: String) = EquipmentSyncEntity(
        id = id,
        name = name,
        nodeId = nodeId,
        sectionId = sectionId,
        model = model,
        manufacturer = manufacturer,
        serialNumber = serialNumber,
        nominal = nominal,
        verificationYear = verificationYear,
        photoPaths = photoPaths ?: "",
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // DetailedEquipment конвертеры
    fun DetailedEquipmentSyncEntity.toEntity() = com.example.kipia.database.DetailedEquipmentEntity(
        id = id,
        equipmentType = equipmentType,
        name = name,
        nodeId = nodeId,
        sectionId = sectionId,
        model = model,
        manufacturer = manufacturer,
        serialNumber = serialNumber,
        productionYear = productionYear,
        verificationYear = verificationYear,
        nominal = nominal,
        pressureLimit = pressureLimit,
        softwareVersion = softwareVersion,
        mo = mo,
        mz = mz,
        mto = mto,
        mtz = mtz,
        muo = muo,
        muz = muz,
        outputContacts = outputContacts,
        photoPaths = photoPaths,
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация детализации оборудования: $id - $name, фото: ${photoList.size}")
    }

    fun com.example.kipia.database.DetailedEquipmentEntity.toSyncEntity(deviceId: String) = DetailedEquipmentSyncEntity(
        id = id,
        equipmentType = equipmentType,
        name = name,
        nodeId = nodeId,
        sectionId = sectionId,
        model = model,
        manufacturer = manufacturer,
        serialNumber = serialNumber,
        productionYear = productionYear,
        verificationYear = verificationYear,
        nominal = nominal,
        pressureLimit = pressureLimit,
        softwareVersion = softwareVersion,
        mo = mo,
        mz = mz,
        mto = mto,
        mtz = mtz,
        muo = muo,
        muz = muz,
        outputContacts = outputContacts,
        photoPaths = photoPaths,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId,
        //photos = photos ?: ""
    )

    // Remark конвертеры
    fun RemarkSyncEntity.toEntity() = com.example.kipia.database.RemarkEntity(
        id = id,
        controlPointId = controlPointId,
        title = title,
        description = description,
        category = category,
        priority = priority,
        status = status,
        createdDate = createdDate,
        deadline = deadline,
        completedDate = completedDate,
        photos = photos,
        isArchived = isArchived
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация Замечаний: $id - $title, фото: ${photoList.size}")
    }

    fun com.example.kipia.database.RemarkEntity.toSyncEntity(deviceId: String) = RemarkSyncEntity(
        id = id,
        controlPointId = controlPointId,
        title = title,
        description = description,
        category = category,
        priority = priority,
        status = status,
        createdDate = createdDate,
        deadline = deadline,
        completedDate = completedDate,
        photos = photos ?: "",
        isArchived = isArchived,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // SyncModels.kt - добавьте это в object EntityConverters (после Remark конвертеров):

    // Event конвертеры
    fun EventSyncEntity.toEntity() = com.example.kipia.database.EventEntity(
        id = id,
        controlPointId = controlPointId,
        title = title,
        description = description,
        type = type,
        date = date,
        time = time,
        isCompleted = isCompleted,
        participants = participants
    ).also {
        Log.d("EntityConverters", "🔄 Конвертация Event: $id - $title")
    }

    fun com.example.kipia.database.EventEntity.toSyncEntity(deviceId: String) = EventSyncEntity(
        id = id,
        controlPointId = controlPointId,
        title = title,
        description = description,
        type = type,
        date = date,
        time = time,
        isCompleted = isCompleted,
        participants = participants,
        lastModified = System.currentTimeMillis(),
        deviceId = deviceId
    )

    // Вспомогательные методы для работы с фото
    fun updateEntityWithPhoto(entity: Any, photoPath: String): Any {
        return when (entity) {
            is EquipmentSyncEntity -> entity.copy(
                photoPaths = addPhotoToList(entity.photoPaths, photoPath)
            )
            is DetailedEquipmentSyncEntity -> entity.copy(
                photoPaths = addPhotoToList(entity.photoPaths, photoPath),
            )
            is RemarkSyncEntity -> entity.copy(photos = addPhotoToList(entity.photos, photoPath))
            else -> entity
        }
    }

    private fun addPhotoToList(existingPhotos: String, newPhoto: String): String {
        return if (existingPhotos.isBlank()) {
            newPhoto
        } else {
            "$existingPhotos,$newPhoto"
        }
    }

    fun extractPhotoPaths(entity: Any): List<String> {
        return when (entity) {
            is EquipmentSyncEntity -> entity.photoList
            is DetailedEquipmentSyncEntity -> entity.photoList
            is RemarkSyncEntity -> entity.photoList
            else -> emptyList()
        }
    }
}