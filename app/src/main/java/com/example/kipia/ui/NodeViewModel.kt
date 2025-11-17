package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.NodeEntity
import com.example.kipia.model.NodeType
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
            println("DEBUG: Loaded nodes for tube $tubeId: $result")
            _nodes.value = result
        }
    }

    fun addNode(fullName: String, tubeId: Long, nodeType: NodeType) {
        println("DEBUG: addNode called with fullName=$fullName, tubeId=$tubeId, type=$nodeType")
        viewModelScope.launch {
            val node = NodeEntity(
                name = fullName, // Используем переданное полное имя
                tubeId = tubeId,
                nodeType = nodeType.name
            )
            println("DEBUG: About to insert node=$node")
            withContext(Dispatchers.IO) {
                database.nodeDao().insert(node)
            }
            println("DEBUG: Node inserted, now loading nodes")
            loadNodesByTubeId(tubeId)
            println("DEBUG: Nodes loaded")
        }
    }

    fun deleteNode(id: Long, tubeId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.nodeDao().deleteById(id)
            }
            println("DEBUG: Node deleted, now loading nodes")
            loadNodesByTubeId(tubeId)
            println("DEBUG: Nodes loaded")
        }
    }

    fun updateNode(id: Long, newName: String, tubeId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.nodeDao().update(id, newName)
            }
            loadNodesByTubeId(tubeId)
        }
    }
}