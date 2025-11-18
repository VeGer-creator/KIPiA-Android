package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.RemarkEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RemarkViewModel(private val database: AppDatabase) : ViewModel() {

    private val _remarks = MutableStateFlow<List<RemarkEntity>>(emptyList())
    val remarks: StateFlow<List<RemarkEntity>> = _remarks

    fun loadRemarksByControlPointId(controlPointId: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.remarkDao().getRemarksByControlPointId(controlPointId)
            }
            _remarks.value = result
        }
    }

    fun updateRemarkCompletion(remarkId: Long, isCompleted: Boolean, completionDate: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.remarkDao().updateCompletionStatus(remarkId, isCompleted, completionDate)
            }
            // Обновляем список
            loadRemarksByControlPointId(_remarks.value.firstOrNull()?.controlPointId ?: 0L)
        }
    }
}
