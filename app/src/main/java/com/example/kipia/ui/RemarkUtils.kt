package com.example.kipia.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.kipia.database.RemarkEntity
import java.text.SimpleDateFormat
import java.util.*

// Функции для цветов статусов и приоритетов
fun getPriorityColor(priority: String): Color {
    return when (priority) {
        "Высокий" -> Color(0xFFFF5252)    // Красный
        "Средний" -> Color(0xFFFFB74D)    // Оранжевый
        else -> Color(0xFF4CAF50)         // Зеленый
    }
}

fun getStatusColor(status: String): Color {
    return when (status) {
        "Открыто" -> Color(0xFFFF5252)    // Красный
        "В работе" -> Color(0xFFFFB74D)   // Оранжевый
        "Выполнено" -> Color(0xFF4CAF50)  // Зеленый
        else -> Color.Gray
    }
}

@Composable
fun getRemarkCardColor(priority: String, status: String): Color {
    return when {
        status == "Выполнено" -> MaterialTheme.colors.surface
        status == "В работе" -> MaterialTheme.colors.secondary.copy(alpha = 0.1f)
        priority == "Высокий" -> MaterialTheme.colors.error.copy(alpha = 0.1f)
        priority == "Средний" -> MaterialTheme.colors.secondary.copy(alpha = 0.1f)
        else -> MaterialTheme.colors.surface
    }
}

fun getDeadlineColor(deadline: String): Color {
    return try {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val deadlineDate = sdf.parse(deadline)
        val currentDate = Date()

        deadlineDate?.let {
            val daysDiff = ((it.time - currentDate.time) / (1000 * 60 * 60 * 24)).toInt()
            when {
                daysDiff < 0 -> Color(0xFFFF5252)    // Просрочено - красный
                daysDiff <= 3 -> Color(0xFFFFB74D)   // Скоро срок - оранжевый
                else -> Color(0xFF4CAF50)            // В норме - зеленый
            }
        } ?: Color.Gray
    } catch (e: Exception) {
        Color.Gray
    }
}

// Функция для получения эмодзи приоритета
fun getPriorityEmoji(priority: String): String {
    return when (priority) {
        "Высокий" -> "‼️"
        "Средний" -> "⚠️"
        else -> "✅"
    }
}