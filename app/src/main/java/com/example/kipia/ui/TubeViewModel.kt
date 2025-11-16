package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.TubeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TubeViewModel(private val database: AppDatabase) : ViewModel() {

    // Указываем тип явно: MutableStateFlow<List<TubeEntity>>
    private val _tubes: MutableStateFlow<List<TubeEntity>> = MutableStateFlow(emptyList())
    // Указываем тип явно: StateFlow<List<TubeEntity>>
    val tubes: StateFlow<List<TubeEntity>> = _tubes

    // Метод loadTubesByControlPointId - загрузка Труб по ID КП (вместо getTubesByPKUId)
    fun loadTubesByControlPointId(controlPointId: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.tubeDao().getTubesByControlPointId(controlPointId)
            }
            _tubes.value = result
        }
    }

    // Метод addTube - добавление Трубы, теперь требует controlPointId
    fun addTube(name: String, controlPointId: Long) {
        viewModelScope.launch {
            val tube = TubeEntity(name = name, controlPointId = controlPointId)
            withContext(Dispatchers.IO) {
                database.tubeDao().insert(tube)
            }
            loadTubesByControlPointId(controlPointId) // обновляем список
        }
    }

    fun deleteTube(id: Long, controlPointId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.tubeDao().deleteById(id)
            }
            loadTubesByControlPointId(controlPointId) // обновляем список
        }
    }
}