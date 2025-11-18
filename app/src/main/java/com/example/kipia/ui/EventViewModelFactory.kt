// app/src/main/java/com/example/kipia/ui/EventViewModelFactory.kt
package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kipia.database.AppDatabase

class EventViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
            return EventViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}