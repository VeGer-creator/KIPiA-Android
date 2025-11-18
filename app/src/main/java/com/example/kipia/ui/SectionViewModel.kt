package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.SectionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SectionViewModel(private val database: AppDatabase) : ViewModel() {

    private val _sections = MutableStateFlow<List<SectionEntity>>(emptyList())
    val sections: StateFlow<List<SectionEntity>> = _sections

    fun loadSectionsByPKUId(pkuId: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.sectionDao().getSectionsByPKUId(pkuId)
            }
            _sections.value = result
        }
    }

    fun addSection(name: String, pkuId: Long) {
        viewModelScope.launch {
            val section = SectionEntity(name = name, pkuId = pkuId)
            withContext(Dispatchers.IO) {
                database.sectionDao().insert(section)
            }
            loadSectionsByPKUId(pkuId)
        }
    }

    fun deleteSection(section: SectionEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.sectionDao().delete(section)
            }
            loadSectionsByPKUId(section.pkuId)
        }
    }
}