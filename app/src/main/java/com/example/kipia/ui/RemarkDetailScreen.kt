package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale

@Composable
fun RemarkDetailScreen(
    remarkId: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // TODO: Загрузить данные замечания по ID
    // Пока заглушка - в реальности нужно загрузить из базы данных
    val remark = remember {
        // Заглушка - в реальности нужно загрузить из ViewModel
        com.example.kipia.database.RemarkEntity(
            id = remarkId,
            controlPointId = 1,
            title = "Пример замечания",
            description = "Подробное описание замечания",
            category = "Оборудование",
            priority = "Высокий",
            status = "Открыто",
            createdDate = "20.11.2023",
            deadline = "27.11.2023",
            completedDate = "",
            photos = ""
        )
    }

    val photoPaths = remark.getPhotoList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Замечание") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Карточка с основной информацией
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Заголовок и приоритет
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = remark.title,
                            style = MaterialTheme.typography.h6
                        )
                        PriorityBadge(remark.priority)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Описание
                    if (remark.description.isNotEmpty()) {
                        Text(
                            text = "Описание:",
                            style = MaterialTheme.typography.subtitle2,
                            color = MaterialTheme.colors.primary
                        )
                        Text(
                            text = remark.description,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Детали
                    RemarkDetailItem("Категория", remark.category)
                    RemarkDetailItem("Статус", remark.status)
                    RemarkDetailItem("Дата создания", remark.createdDate)
                    RemarkDetailItem("Срок устранения", remark.deadline)

                    if (remark.completedDate.isNotEmpty()) {
                        RemarkDetailItem("Дата выполнения", remark.completedDate)
                    }
                }
            }

            // Секция с фото
            if (photoPaths.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Прикрепленные фото (${photoPaths.size})",
                            style = MaterialTheme.typography.h6
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Отображение фото в сетке
                        val columns = 2
                        val rows = (photoPaths.size + columns - 1) / columns

                        Column {
                            for (i in 0 until rows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (j in 0 until columns) {
                                        val index = i * columns + j
                                        if (index < photoPaths.size) {
                                            val uri = Uri.parse(photoPaths[index])
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(4.dp)
                                                    .aspectRatio(1f)
                                            ) {
                                                AsyncImage(
                                                    model = uri,
                                                    contentDescription = "Фото замечания",
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(MaterialTheme.shapes.medium),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // История изменений (можно добавить позже)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "История изменений",
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Создано: ${remark.createdDate}",
                        style = MaterialTheme.typography.caption
                    )
                    if (remark.completedDate.isNotEmpty()) {
                        Text(
                            text = "Завершено: ${remark.completedDate}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemarkDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )
    }
}