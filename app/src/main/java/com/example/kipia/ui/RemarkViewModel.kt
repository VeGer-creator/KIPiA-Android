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
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.example.kipia.utils.PhotoStorageUtils

class RemarkViewModel(private val database: AppDatabase) : ViewModel() {

    private val _remarks = MutableStateFlow<List<RemarkEntity>>(emptyList())
    val remarks: StateFlow<List<RemarkEntity>> = _remarks

    private val _activeRemarks = MutableStateFlow<List<RemarkEntity>>(emptyList())
    val activeRemarks: StateFlow<List<RemarkEntity>> = _activeRemarks

    private val _archivedRemarks = MutableStateFlow<List<RemarkEntity>>(emptyList())
    val archivedRemarks: StateFlow<List<RemarkEntity>> = _archivedRemarks

    private var _currentControlPointId: Long = 0L

    fun loadRemarksByControlPointId(controlPointId: Long) {
        _currentControlPointId = controlPointId
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.remarkDao().getRemarksByControlPointId(controlPointId)
            }
            _remarks.value = result
        }
    }

    // ДОБАВЛЕНО: Загрузка активных замечаний
    fun loadActiveRemarksByControlPointId(controlPointId: Long) {
        _currentControlPointId = controlPointId
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.remarkDao().getActiveRemarksByControlPointId(controlPointId)
            }
            _activeRemarks.value = result
        }
    }

    // ДОБАВЛЕНО: Загрузка архивных замечаний
    fun loadArchivedRemarksByControlPointId(controlPointId: Long) {
        _currentControlPointId = controlPointId
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.remarkDao().getArchivedRemarksByControlPointId(controlPointId)
            }
            _archivedRemarks.value = result
        }
    }

    // ДОБАВЛЕНО: Загрузка всех данных сразу
    fun loadAllRemarksByControlPointId(controlPointId: Long) {
        _currentControlPointId = controlPointId
        viewModelScope.launch {
            val activeResult = withContext(Dispatchers.IO) {
                database.remarkDao().getActiveRemarksByControlPointId(controlPointId)
            }
            val archivedResult = withContext(Dispatchers.IO) {
                database.remarkDao().getArchivedRemarksByControlPointId(controlPointId)
            }
            _activeRemarks.value = activeResult
            _archivedRemarks.value = archivedResult
            _remarks.value = activeResult + archivedResult
        }
    }

    fun addRemark(
        title: String,
        description: String = "",
        category: String = "Оборудование",
        priority: String = "Средний",
        deadline: String = getNextWeekDate()
    ) {
        viewModelScope.launch {
            val currentDate = getCurrentDate()
            val remark = RemarkEntity(
                controlPointId = _currentControlPointId,
                title = title,
                description = description,
                category = category,
                priority = priority,
                status = "Открыто",
                createdDate = currentDate,
                deadline = deadline
            )
            withContext(Dispatchers.IO) {
                database.remarkDao().insert(remark)
            }
            loadAllRemarksByControlPointId(_currentControlPointId)
        }
    }

    fun addRemarkWithPhotos(
        title: String,
        description: String = "",
        category: String = "Оборудование",
        priority: String = "Средний",
        deadline: String = getNextWeekDate(),
        photoPaths: List<String> = emptyList()
    ) {
        Log.d("RemarkViewModel", "Adding remark with ${photoPaths.size} photos")
        viewModelScope.launch {
            val currentDate = getCurrentDate()
            val remark = RemarkEntity(
                controlPointId = _currentControlPointId,
                title = title,
                description = description,
                category = category,
                priority = priority,
                status = "Открыто",
                createdDate = currentDate,
                deadline = deadline
            ).withPhotos(photoPaths)

            Log.d("RemarkViewModel", "Saving remark to database")
            withContext(Dispatchers.IO) {
                database.remarkDao().insert(remark)
            }
            Log.d("RemarkViewModel", "Remark saved, reloading list")
            loadAllRemarksByControlPointId(_currentControlPointId)
        }
    }

    fun updateRemarkStatus(remarkId: Long, newStatus: String) {
        viewModelScope.launch {
            val completedDate = if (newStatus == "Выполнено") getCurrentDate() else ""
            withContext(Dispatchers.IO) {
                database.remarkDao().updateStatus(remarkId, newStatus, completedDate)

                // Автоматически архивируем выполненные замечания
                if (newStatus == "Выполнено") {
                    database.remarkDao().archiveRemark(remarkId)
                }
            }
            loadAllRemarksByControlPointId(_currentControlPointId)
        }
    }

    // ДОБАВЛЕНО: Архивирование замечания
    fun archiveRemark(remarkId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.remarkDao().archiveRemark(remarkId)
            }
            loadAllRemarksByControlPointId(_currentControlPointId)
        }
    }

    // ДОБАВЛЕНО: Возврат из архива
    fun unarchiveRemark(remarkId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.remarkDao().unarchiveRemark(remarkId)
            }
            loadAllRemarksByControlPointId(_currentControlPointId)
        }
    }

    fun updateRemark(remark: RemarkEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.remarkDao().update(remark)
            }
            loadAllRemarksByControlPointId(_currentControlPointId)
        }
    }

    fun deleteRemark(context: android.content.Context, remark: RemarkEntity) {
        viewModelScope.launch {
            // УДАЛЯЕМ ФОТО ИЗ ХРАНИЛИЩА
            withContext(Dispatchers.IO) {
                remark.getPhotoList().forEach { path ->
                    PhotoStorageUtils.deletePhotoFromStorage(context, path)
                }
                database.remarkDao().delete(remark)
            }
            loadAllRemarksByControlPointId(_currentControlPointId)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getNextWeekDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(calendar.time)
    }
}

// Функции для использования в UI
fun getNextWeekDate(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 7)
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(calendar.time)
}

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(Date())
}