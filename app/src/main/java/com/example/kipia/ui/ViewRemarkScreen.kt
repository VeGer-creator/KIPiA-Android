package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.kipia.utils.PhotoStorageUtils

@Composable
fun ViewRemarkScreen(
    remarkId: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current

    // TODO: Загрузить замечание по ID из ViewModel
    // Пока используем заглушку
    val remark = remember {
        // В реальном приложении загружаем из базы
        com.example.kipia.database.RemarkEntity(
            id = remarkId,
            controlPointId = 1,
            title = "Пример замечания для просмотра",
            description = "Это подробное описание замечания с деталями проблемы и рекомендациями по устранению.",
            category = "Оборудование",
            priority = "Высокий",
            status = "В работе",
            createdDate = "20.11.2023",
            deadline = "27.11.2023",
            completedDate = "",
            photos = "" // В реальном приложении здесь будут постоянные пути
        )
    }

    val photoPaths = remark.getPhotoList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Просмотр замечания") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
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
            // Основная информация
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

                    Spacer(modifier = Modifier.height(12.dp))

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

                    // Детали (используем функцию из ViewRemarkUtils)
                    ViewRemarkDetailItem("Категория", remark.category)
                    ViewRemarkDetailItem("Статус", remark.status)
                    ViewRemarkDetailItem("Дата создания", remark.createdDate)
                    ViewRemarkDetailItem("Срок устранения", remark.deadline)

                    if (remark.completedDate.isNotEmpty()) {
                        ViewRemarkDetailItem("Дата выполнения", remark.completedDate)
                    }
                }
            }

            // Секция с фото - ИСПОЛЬЗУЕМ ПОСТОЯННЫЕ ПУТИ
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Отображение фото в сетке 2 колонки
                        val columns = 2
                        Column {
                            for (i in photoPaths.indices step columns) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    for (j in 0 until columns) {
                                        val index = i + j
                                        if (index < photoPaths.size) {
                                            // СОЗДАЕМ URI ИЗ ПОСТОЯННОГО ПУТИ
                                            val uri = PhotoStorageUtils.getUriForAppFile(context, photoPaths[index])
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(8.dp)
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
        }
    }
}