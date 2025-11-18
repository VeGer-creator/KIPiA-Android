package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NodeItem(
    node: com.example.kipia.database.NodeEntity,
    onEdit: (com.example.kipia.database.NodeEntity) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 2.dp, bottom = 2.dp),
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface // Используем цвет поверхности темы
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = node.name,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colors.onSurface // Используем цвет текста темы
            )
            IconButton(
                onClick = { onEdit(node) },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Редактировать объект",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colors.onSurface // Цвет иконки по теме
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить объект",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colors.onSurface // Цвет иконки по теме
                )
            }
        }
    }
}