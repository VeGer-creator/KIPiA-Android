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
            // Обновляем список после добавления
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }

    fun updateEquipment(equipment: DetailedEquipmentEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.detailedEquipmentDao().update(equipment)
            }
            // Обновляем список после изменения
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }

    fun deleteEquipment(equipment: DetailedEquipmentEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.detailedEquipmentDao().delete(equipment)
            }
            // Обновляем список после удаления
            _currentNodeId.value?.let { loadEquipmentByNodeId(it) }
            _currentSectionId.value?.let { loadEquipmentBySectionId(it) }
        }
    }

    suspend fun getEquipmentById(equipmentId: Long): DetailedEquipmentEntity? {
        return withContext(Dispatchers.IO) {
            database.detailedEquipmentDao().getEquipmentById(equipmentId)
        }
    }

}