package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.ControlPointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ControlPointViewModel(private val database: AppDatabase) : ViewModel() {

    private val _controlPoints = MutableStateFlow<List<ControlPointEntity>>(emptyList())
    val controlPoints: StateFlow<List<ControlPointEntity>> = _controlPoints

    init {
        loadControlPoints()
    }

    private fun loadControlPoints() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.controlPointDao().getAllControlPoints()
            }
            _controlPoints.value = result
        }
    }

    fun addControlPoint(name: String, description: String = "") {
        viewModelScope.launch {
            val cp = ControlPointEntity(name = name, description = description)
            withContext(Dispatchers.IO) {
                database.controlPointDao().insert(cp)
            }
            loadControlPoints() // обновляем список
        }
    }

    fun deleteControlPoint(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.controlPointDao().deleteById(id)
            }
            loadControlPoints()
        }
    }

    fun updateControlPoint(id: Long, newName: String, newDescription: String = "") {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.controlPointDao().update(id, newName, newDescription)
            }
            loadControlPoints()
        }
    }


}