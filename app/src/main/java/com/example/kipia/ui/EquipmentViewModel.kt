// app/src/main/java/com/example/kipia/ui/EquipmentViewModel.kt
package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.DetailedEquipmentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class EquipmentViewModel(private val database: AppDatabase) : ViewModel() {

    private val _equipment = MutableStateFlow<List<DetailedEquipmentEntity>>(emptyList())
    val equipment: StateFlow<List<DetailedEquipmentEntity>> = _equipment

    private val _currentNodeId = MutableStateFlow<Long?>(null)
    private val _currentSectionId = MutableStateFlow<Long?>(null)

    fun loadEquipmentByNodeId(nodeId: Long) {
        _currentNodeId.value = nodeId
        _currentSectionId.value = null

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.detailedEquipmentDao().getEquipmentByNodeId(nodeId)
            }
            _equipment.value = result
        }
    }

    fun loadEquipmentBySectionId(sectionId: Long) {
        _currentSectionId.value = sectionId
        _currentNodeId.value = null

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.detailedEquipmentDao().getEquipmentBySectionId(sectionId)
            }
            _equipment.value = result
        }
    }

    fun addEquipment(equipment: DetailedEquipmentEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.detailedEquipmentDao().insert(equipment)
            }
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }

    fun updateEquipment(equipment: DetailedEquipmentEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.detailedEquipmentDao().update(equipment)
            }
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }

    fun deleteEquipment(equipment: DetailedEquipmentEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.detailedEquipmentDao().delete(equipment)
            }
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }

    suspend fun getEquipmentById(equipmentId: Long): DetailedEquipmentEntity? {
        return withContext(Dispatchers.IO) {
            database.detailedEquipmentDao().getEquipmentById(equipmentId)
        }
    }

    // ДОБАВЛЕНО: Методы для работы с фото
    fun addPhotosToEquipment(equipmentId: Long, photoPaths: List<String>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val equipment = database.detailedEquipmentDao().getEquipmentById(equipmentId)
                equipment?.let {
                    val currentPaths = if (it.photoPaths.isNotEmpty()) {
                        try {
                            Json.decodeFromString<List<String>>(it.photoPaths) // ИСПРАВЛЕНО: правильный синтаксис
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                    val updatedPaths = currentPaths + photoPaths
                    val updatedPathsJson = Json.encodeToString(updatedPaths) // ИСПРАВЛЕНО: правильный синтаксис

                    database.detailedEquipmentDao().updatePhotoPaths(equipmentId, updatedPathsJson)
                }
            }
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }

    fun removePhotoFromEquipment(equipmentId: Long, photoPath: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val equipment = database.detailedEquipmentDao().getEquipmentById(equipmentId)
                equipment?.let {
                    val currentPaths = if (it.photoPaths.isNotEmpty()) {
                        try {
                            Json.decodeFromString<List<String>>(it.photoPaths) // ИСПРАВЛЕНО: правильный синтаксис
                        } catch (e: Exception) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                    val updatedPaths = currentPaths.toMutableList().apply { remove(photoPath) }
                    val updatedPathsJson = Json.encodeToString(updatedPaths) // ИСПРАВЛЕНО: правильный синтаксис

                    database.detailedEquipmentDao().updatePhotoPaths(equipmentId, updatedPathsJson)
                }
            }
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }
}