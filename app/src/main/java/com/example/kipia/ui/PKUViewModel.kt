package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.PKUEntity
import com.example.kipia.database.SectionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PKUViewModel(private val database: AppDatabase) : ViewModel() {

    private val _pkus = MutableStateFlow<List<PKUEntity>>(emptyList())
    val pkus: StateFlow<List<PKUEntity>> = _pkus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadPKUsByControlPointId(controlPointId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = withContext(Dispatchers.IO) {
                    database.pkuDao().getPKUsByControlPointId(controlPointId)
                }
                _pkus.value = result
            } catch (e: Exception) {
                _errorMessage.value = "Не удалось загрузить ПКУ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Метод addPKU - добавление ПКУ с автоматическим созданием стандартных отсеков
    fun addPKU(name: String, description: String = "", controlPointId: Long) {
        viewModelScope.launch {
            // Сначала создаем ПКУ
            val pku = PKUEntity(name = name, description = description, controlPointId = controlPointId)
            withContext(Dispatchers.IO) {
                database.pkuDao().insert(pku)
            }

            // Находим созданный ПКУ по имени и controlPointId
            val pkus = withContext(Dispatchers.IO) {
                database.pkuDao().getPKUsByControlPointId(controlPointId)
            }
            val newPKU = pkus.find { it.name == name && it.controlPointId == controlPointId }

            // Автоматически создаем стандартные отсеки
            newPKU?.let { pku ->
                val defaultSections = listOf(
                    SectionEntity(name = "Инженерный отсек", pkuId = pku.id),
                    SectionEntity(name = "Трансформаторный отсек", pkuId = pku.id)
                )

                withContext(Dispatchers.IO) {
                    defaultSections.forEach { section ->
                        database.sectionDao().insert(section)
                    }
                }
            }

            loadPKUsByControlPointId(controlPointId)
        }
    }

    fun deletePKU(id: Long, controlPointId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.pkuDao().deleteById(id)
            }
            loadPKUsByControlPointId(controlPointId)
        }
    }

    fun updatePKU(id: Long, newName: String, newDescription: String = "") {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.pkuDao().update(id, newName, newDescription)
            }
            // Обновляем список ПКУ для текущего КП
            val current = withContext(Dispatchers.IO) {
                database.pkuDao().getPKUById(id)
            }
            current?.let { pku ->
                loadPKUsByControlPointId(pku.controlPointId)
            }
        }
    }
}