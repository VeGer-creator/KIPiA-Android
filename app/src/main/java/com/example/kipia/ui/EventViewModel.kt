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
import java.text.SimpleDateFormat
import java.util.*

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

    fun addEvent(
        title: String,
        description: String = "",
        type: String = "Проверка",
        date: String = getCurrentDate(),
        time: String = ""
    ) {
        viewModelScope.launch {
            val event = EventEntity(
                controlPointId = _currentControlPointId,
                title = title,
                description = description,
                type = type,
                date = date,
                time = time,
                isCompleted = false
            )
            withContext(Dispatchers.IO) {
                database.eventDao().insert(event)
            }
            loadEventsByControlPointId(_currentControlPointId)
        }
    }

    fun updateEventCompletion(eventId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            val event = withContext(Dispatchers.IO) {
                database.eventDao().getEventById(eventId)
            }
            event?.let {
                val updatedEvent = it.copy(isCompleted = isCompleted)
                withContext(Dispatchers.IO) {
                    database.eventDao().update(updatedEvent)
                }
                loadEventsByControlPointId(_currentControlPointId)
            }
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