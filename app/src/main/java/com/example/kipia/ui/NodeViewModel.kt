// app/src/main/java/com/example/kipia/ui/NodeViewModel.kt
package com.example.kipia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.NodeEntity
import com.example.kipia.model.NodeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NodeViewModel(private val database: AppDatabase) : ViewModel() {

    // Основной поток для текущего отображения
    private val _currentNodes = MutableStateFlow<List<NodeEntity>>(emptyList())
    val nodes: StateFlow<List<NodeEntity>> = _currentNodes

    // Отдельные потоки для каждой трубы
    private val _nodesByTubeId = mutableMapOf<Long, MutableStateFlow<List<NodeEntity>>>()

    // Получить Flow для конкретной трубы
    fun getNodesFlowForTube(tubeId: Long): StateFlow<List<NodeEntity>> {
        if (!_nodesByTubeId.containsKey(tubeId)) {
            _nodesByTubeId[tubeId] = MutableStateFlow(emptyList())
        }
        return _nodesByTubeId[tubeId]!!
    }

    // Загрузка узлов для конкретной трубы
    fun loadNodesByTubeId(tubeId: Long) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                database.nodeDao().getNodesByTubeId(tubeId)
            }
            println("DEBUG: Loaded nodes for tube $tubeId: $result")

            // Обновляем хранилище для этой трубы
            if (!_nodesByTubeId.containsKey(tubeId)) {
                _nodesByTubeId[tubeId] = MutableStateFlow(emptyList())
            }
            _nodesByTubeId[tubeId]?.value = result

            // Устанавливаем текущие узлы для отображения
            _currentNodes.value = result
        }
    }

    fun addNode(fullName: String, tubeId: Long, nodeType: NodeType) {
        viewModelScope.launch {
            // Получаем текущие узлы для определения orderIndex
            val currentNodes = withContext(Dispatchers.IO) {
                database.nodeDao().getNodesByTubeId(tubeId)
            }
            val nextOrderIndex = currentNodes.size

            val node = NodeEntity(
                name = fullName,
                tubeId = tubeId,
                nodeType = nodeType.name,
                orderIndex = nextOrderIndex
            )

            withContext(Dispatchers.IO) {
                database.nodeDao().insert(node)
            }
            loadNodesByTubeId(tubeId)
        }
    }

    fun deleteNode(id: Long, tubeId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database.nodeDao().deleteById(id)
            }
            // После удаления пересчитываем порядок
            reorderNodesAfterDeletion(tubeId)
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

    // Функция для обновления порядка узлов
    fun updateNodesOrder(tubeId: Long, newOrder: List<NodeEntity>) {
        viewModelScope.launch {
            val updatedNodes = newOrder.mapIndexed { index, node ->
                node.copy(orderIndex = index)
            }

            withContext(Dispatchers.IO) {
                updatedNodes.forEach { node ->
                    database.nodeDao().updateOrder(node.id, node.orderIndex)
                }
            }

            loadNodesByTubeId(tubeId)
        }
    }

    // Пересчет порядка после удаления
    private suspend fun reorderNodesAfterDeletion(tubeId: Long) {
        val remainingNodes = withContext(Dispatchers.IO) {
            database.nodeDao().getNodesByTubeId(tubeId)
        }

        val reorderedNodes = remainingNodes.sortedBy { it.orderIndex }.mapIndexed { index, node ->
            node.copy(orderIndex = index)
        }

        withContext(Dispatchers.IO) {
            reorderedNodes.forEach { node ->
                database.nodeDao().updateOrder(node.id, node.orderIndex)
            }
        }

        loadNodesByTubeId(tubeId)
    }

    // Добавьте этот метод в класс NodeViewModel
// В NodeViewModel, в методе moveNode добавьте логирование:
// В NodeViewModel
    fun moveNode(tubeId: Long, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val nodes = withContext(Dispatchers.IO) {
                    database.nodeDao().getNodesByTubeId(tubeId)
                }.sortedBy { it.orderIndex }

                if (fromIndex in nodes.indices && toIndex in nodes.indices) {
                    val updatedNodes = nodes.toMutableList()
                    val movedNode = updatedNodes.removeAt(fromIndex)
                    updatedNodes.add(toIndex, movedNode)

                    // Обновляем порядковые номера
                    val reorderedNodes = updatedNodes.mapIndexed { index, node ->
                        node.copy(orderIndex = index)
                    }

                    // Сохраняем в базу
                    withContext(Dispatchers.IO) {
                        reorderedNodes.forEach { node ->
                            database.nodeDao().updateOrder(node.id, node.orderIndex)
                        }
                    }

                    // Немедленно обновляем UI
                    loadNodesByTubeId(tubeId)
                }
            } catch (e: Exception) {
                // Обработка ошибок
                e.printStackTrace()
            }
        }
    }

    // В NodeViewModel
    fun reorderNodes(tubeId: Long, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            println("🔄 REORDER: Moving from $fromIndex to $toIndex in tube $tubeId")

            try {
                // Получаем текущие узлы
                val nodes = withContext(Dispatchers.IO) {
                    database.nodeDao().getNodesByTubeId(tubeId)
                }.sortedBy { it.orderIndex }

                println("📊 BEFORE: ${nodes.map { it.name }}")

                if (fromIndex in nodes.indices && toIndex in nodes.indices) {
                    // Создаем копию и перемещаем элемент
                    val updatedNodes = nodes.toMutableList()
                    val movedNode = updatedNodes.removeAt(fromIndex)
                    updatedNodes.add(toIndex, movedNode)

                    // Обновляем порядковые номера
                    val reorderedNodes = updatedNodes.mapIndexed { index, node ->
                        node.copy(orderIndex = index)
                    }

                    println("📊 AFTER: ${reorderedNodes.map { it.name }}")

                    // Сохраняем в базу
                    withContext(Dispatchers.IO) {
                        reorderedNodes.forEach { node ->
                            database.nodeDao().updateOrder(node.id, node.orderIndex)
                        }
                    }

                    // Обновляем UI
                    loadNodesByTubeId(tubeId)
                }
            } catch (e: Exception) {
                println("❌ ERROR: ${e.message}")
            }
        }
    }
}