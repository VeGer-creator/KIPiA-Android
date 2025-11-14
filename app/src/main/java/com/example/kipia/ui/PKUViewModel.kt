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

    init {
        loadPKUs()
    }

    private fun loadPKUs() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.pkuDao().getAllPKUs()
            }
            _pkus.value = result
        }
    }

    fun addPKU(name: String, description: String = "") {
        viewModelScope.launch {
            val pku = PKUEntity(name = name, description = description)
            withContext(Dispatchers.IO) {
                database.pkuDao().insert(pku)
            }
            loadPKUs() // обновляем список
        }
    }

    fun deletePKU(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.pkuDao().deleteById(id)
            }
            loadPKUs()
        }
    }
}