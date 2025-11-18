// app/src/main/java/com/example/kipia/ui/SimpleDraggableNodeItem.kt
package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LazyItemScope.SimpleDraggableNodeItem(
    node: com.example.kipia.database.NodeEntity,
    onEdit: (com.example.kipia.database.NodeEntity) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка для визуального обозначения возможности перетаскивания
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Перетащить",
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp),
                tint = androidx.compose.ui.graphics.Color.Gray
            )

            Text(
                text = node.name,
                style = androidx.compose.material.MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f)
            )

            // Кнопка редактирования объекта
            IconButton(
                onClick = { onEdit(node) },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Редактировать объект",
                    modifier = Modifier.size(14.dp)
                )
            }

            // Кнопка удаления объекта
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить объект",
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}