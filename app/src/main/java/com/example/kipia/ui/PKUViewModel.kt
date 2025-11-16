package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.PKUEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PKUViewModel(private val database: AppDatabase) : ViewModel() {

    private val _pkus = MutableStateFlow<List<PKUEntity>>(emptyList())
    val pkus: StateFlow<List<PKUEntity>> = _pkus

    // Метод loadPKUs остаётся, но теперь он загружает все ПКУ
    fun loadPKUs() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.pkuDao().getAllPKUs()
            }
            _pkus.value = result
        }
    }

    // Метод loadPKUsByControlPointId - загрузка ПКУ по ID КП
    fun loadPKUsByControlPointId(controlPointId: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.pkuDao().getPKUsByControlPointId(controlPointId)
            }
            _pkus.value = result
        }
    }

    // Метод addPKU - добавление ПКУ, теперь требует controlPointId
    fun addPKU(name: String, description: String = "", controlPointId: Long) {
        viewModelScope.launch {
            val pku = PKUEntity(name = name, description = description, controlPointId = controlPointId)
            withContext(Dispatchers.IO) {
                database.pkuDao().insert(pku)
            }
            // После добавления можно обновить список, например, через loadPKUs()
            // или loadPKUsByControlPointId(controlPointId), если известен controlPointId
            loadPKUsByControlPointId(controlPointId) // Пример: обновляем список для конкретного КП
        }
    }

    fun deletePKU(id: Long, controlPointId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.pkuDao().deleteById(id)
            }
            loadPKUsByControlPointId(controlPointId) // обновляем список
        }
    }
}