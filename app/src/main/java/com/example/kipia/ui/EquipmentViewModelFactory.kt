package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kipia.database.AppDatabase

class EquipmentViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EquipmentViewModel::class.java)) {
            return EquipmentViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}