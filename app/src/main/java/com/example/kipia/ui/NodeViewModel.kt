// app/src/main/java/com/example/kipia/ui/NodeViewModel.kt
package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.NodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NodeViewModel(private val database: AppDatabase) : ViewModel() {

    private val _nodes: MutableStateFlow<List<NodeEntity>> = MutableStateFlow(emptyList())
    val nodes: StateFlow<List<NodeEntity>> = _nodes

    fun loadNodesByTubeId(tubeId: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.nodeDao().getNodesByTubeId(tubeId)
            }
            println("DEBUG: Loaded nodes for tube $tubeId: $result") // <-- Добавьте это
            _nodes.value = result
        }
    }

    fun addNode(name: String, tubeId: Long) {
        println("DEBUG: addNode called with name=$name, tubeId=$tubeId") // <-- Добавь это
        viewModelScope.launch {
            val node = NodeEntity(name = name, tubeId = tubeId)
            println("DEBUG: About to insert node=$node") // <-- Добавь это
            withContext(Dispatchers.IO) {
                database.nodeDao().insert(node)
            }
            println("DEBUG: Node inserted, now loading nodes") // <-- Добавь это
            loadNodesByTubeId(tubeId)
            println("DEBUG: Nodes loaded") // <-- Добавь это
        }
    }


    fun deleteNode(id: Long, tubeId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.nodeDao().deleteById(id)
            }
            println("DEBUG: Node deleted, now loading nodes") // <-- Добавь это
            loadNodesByTubeId(tubeId)
            println("DEBUG: Nodes loaded") // <-- Добавь это
        }
    }
}