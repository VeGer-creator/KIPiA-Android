package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.kipia.utils.rememberPhotoPickerLauncher
import com.example.kipia.utils.PhotoStorageUtils

@Composable
fun AddRemarkDialog(
    controlPointName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Оборудование") }
    var priority by remember { mutableStateOf("Средний") }
    var deadline by remember { mutableStateOf(getNextWeekDate()) }
    var photoPaths by remember { mutableStateOf<List<String>>(emptyList()) }

    val context = LocalContext.current

    // РЕАЛЬНЫЙ ПИКЕР ФОТО
    val photoPickerLauncher = rememberPhotoPickerLauncher { uris ->
        try {
            if (uris.isNotEmpty()) {
                // КОПИРУЕМ ФОТО В ПОСТОЯННОЕ ХРАНИЛИЩЕ
                val persistentPaths = PhotoStorageUtils.convertUrisToPersistentPaths(context, uris)
                if (persistentPaths.isNotEmpty()) {
                    photoPaths = photoPaths + persistentPaths
                    Log.d("PhotoPicker", "Added ${persistentPaths.size} persistent photos, total: ${photoPaths.size}")
                }
            }
        } catch (e: Exception) {
            Log.e("PhotoPicker", "Error adding photos", e)
        }
    }


    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Новое замечание",
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Для: $controlPointName",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Поля формы
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Категория и приоритет
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Категория", style = MaterialTheme.typography.caption)
                        SimpleCategoryDropdown(
                            category = category,
                            onCategoryChange = { newCategory -> category = newCategory }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Приоритет", style = MaterialTheme.typography.caption)
                        SimplePriorityDropdown(
                            priority = priority,
                            onPriorityChange = { newPriority -> priority = newPriority }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // СЕКЦИЯ ФОТО - РЕАЛЬНАЯ ВЕРСИЯ
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Прикрепленные фото",
                        style = MaterialTheme.typography.subtitle2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Фото: ${photoPaths.size}",
                            style = MaterialTheme.typography.body2
                        )

                        Button(
                            onClick = {
                                try {
                                    Log.d("PhotoPicker", "Launching real photo picker")
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } catch (e: Exception) {
                                    Log.e("PhotoPicker", "Cannot launch photo picker", e)
                                }
                            },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Добавить фото")
                        }
                    }

                    if (photoPaths.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column {
                            photoPaths.forEachIndexed { index, path ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Фото ${index + 1}",
                                        style = MaterialTheme.typography.caption,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )

                                    TextButton(
                                        onClick = {
                                            photoPaths = photoPaths.toMutableList().apply {
                                                removeAt(index)
                                            }
                                        }
                                    ) {
                                        Text("Удалить")
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Срок устранения: $deadline",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            Log.d("PhotoPicker", "Creating remark with ${photoPaths.size} photos")
                            onConfirm(title, description, category, priority, deadline, photoPaths)
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text("Создать")
                    }
                }
            }
        }
    }
}

// ИСПРАВЛЕННЫЕ ФУНКЦИИ БЕЗ ERROR
@Composable
fun SimplePriorityDropdown(priority: String, onPriorityChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val priorities = listOf("Высокий", "Средний", "Низкий")

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text = priority,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body2
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "Выбрать приоритет"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            priorities.forEach { prio ->
                DropdownMenuItem(onClick = {
                    onPriorityChange(prio)
                    expanded = false
                }) {
                    Text(prio)
                }
            }
        }
    }
}

@Composable
fun SimpleCategoryDropdown(category: String, onCategoryChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Оборудование", "Безопасность", "Документация", "Прочее")

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Text(
                text = category,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body2
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Выбрать категорию"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(onClick = {
                    onCategoryChange(cat)
                    expanded = false
                }) {
                    Text(cat)
                }
            }
        }
    }
}