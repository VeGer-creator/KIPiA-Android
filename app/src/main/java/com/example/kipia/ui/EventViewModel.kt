// app/src/main/java/com/example/kipia/ui/EventViewModel.kt
package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.EventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EventViewModel(private val database: AppDatabase) : ViewModel() {

    private val _events = MutableStateFlow<List<EventEntity>>(emptyList())
    val events: StateFlow<List<EventEntity>> = _events

    private var _currentControlPointId: Long = 0L

    fun loadEventsByControlPointId(controlPointId: Long) {
        _currentControlPointId = controlPointId
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.eventDao().getEventsByControlPointId(controlPointId)
            }
            _events.value = result
        }
    }

    fun addEvent(event: EventEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.eventDao().insert(event)
            }
            loadEventsByControlPointId(_currentControlPointId)
        }
    }

    fun updateEvent(event: EventEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.eventDao().update(event)
            }
            loadEventsByControlPointId(_currentControlPointId)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.eventDao().delete(event)
            }
            loadEventsByControlPointId(_currentControlPointId)
        }
    }
}