package com.example.kipia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.kipia.database.RemarkEntity

@Composable
fun RemarkItemCard(
    remark: RemarkEntity,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit,
    showPhotos: Boolean = true
) {
    val photoPaths = remark.getPhotoList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = getRemarkCardColor(remark.priority, remark.status)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Первая строка: заголовок + приоритет + дата
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = remark.title,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Бейдж приоритета
                PriorityBadge(remark.priority)

                Text(
                    text = remark.deadline,
                    style = MaterialTheme.typography.caption,
                    color = getDeadlineColor(remark.deadline),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Вторая строка: описание (если есть)
            if (remark.description.isNotEmpty()) {
                Text(
                    text = remark.description,
                    style = MaterialTheme.typography.body2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Индикатор фото (если есть фото)
            if (showPhotos && photoPaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📷 ${photoPaths.size} фото",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Третья строка: категория + статус + действия
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Категория
                Text(
                    text = when (remark.category) {
                        "Документация" -> "Документы"
                        "Оборудование" -> "Оборуд."
                        else -> remark.category
                    },
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Селектор статуса
                StatusDropdown(
                    currentStatus = remark.status,
                    onStatusChange = onStatusChange
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Кнопка редактирования
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Редактировать",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(getPriorityColor(priority).copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = getPriorityEmoji(priority),
            style = MaterialTheme.typography.caption
        )
    }
}

@Composable
fun StatusDropdown(
    currentStatus: String,
    onStatusChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(
                contentColor = getStatusColor(currentStatus)
            )
        ) {
            Text(
                text = currentStatus,
                style = MaterialTheme.typography.caption
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Изменить статус",
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listOf("Открыто", "В работе", "Выполнено").forEach { status ->
                DropdownMenuItem(onClick = {
                    onStatusChange(status)
                    expanded = false
                }) {
                    Text(status)
                }
            }
        }
    }
}

@Composable
fun RemarkStats(remarks: List<RemarkEntity>) {
    val total = remarks.size
    val completed = remarks.count { it.status == "Выполнено" }
    val inProgress = remarks.count { it.status == "В работе" }
    val highPriority = remarks.count { it.priority == "Высокий" && it.status != "Выполнено" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Всего", total.toString())
            StatItem("Выполнено", completed.toString(), getStatusColor("Выполнено"))
            StatItem("В работе", inProgress.toString(), getStatusColor("В работе"))
            StatItem("Срочные", highPriority.toString(), getPriorityColor("Высокий"))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = MaterialTheme.colors.primary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.h6,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun RemarkFilters(
    selectedCategory: String = "Все",
    onCategoryChange: (String) -> Unit = {}
) {
    val categories = listOf("Все", "Оборудование", "Безопасность", "Документация", "Прочее")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        categories.forEach { category ->
            FilterChip(
                label = category,
                isSelected = category == selectedCategory,
                onSelected = { onCategoryChange(category) }
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colors.primary else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onSurface

    // Сокращаем длинные названия
    val displayLabel = when (label) {
        "Документация" -> "Документы"
        "Оборудование" -> "Оборуд."
        else -> label
    }

    Card(
        modifier = Modifier
            .clickable { onSelected() },
        elevation = if (isSelected) 4.dp else 0.dp,
        backgroundColor = backgroundColor,
        border = if (!isSelected) {
            ButtonDefaults.outlinedBorder
        } else null
    ) {
        Text(
            text = displayLabel,
            color = contentColor,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}