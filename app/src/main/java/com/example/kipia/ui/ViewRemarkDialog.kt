package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.kipia.database.RemarkEntity
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import com.example.kipia.utils.PhotoStorageUtils

@Composable
fun ViewRemarkDialog(
    remark: RemarkEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val photoPaths = remark.getPhotoList()

    // Полноэкранный просмотр фото
    if (selectedPhotoUri != null) {
        FullScreenPhotoView(
            photoUri = selectedPhotoUri!!,
            onDismiss = { selectedPhotoUri = null }
        )
        return
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
                // Заголовок и кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Просмотр замечания",
                        style = MaterialTheme.typography.h6
                    )

                    Row {
                        IconButton(
                            onClick = {
                                onEdit()
                                onDismiss()
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Основная информация
                Card(elevation = 2.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = remark.title,
                            style = MaterialTheme.typography.h6
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (remark.description.isNotEmpty()) {
                            Text(
                                text = remark.description,
                                style = MaterialTheme.typography.body1
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        SimpleDetailRow("Категория", remark.category)
                        SimpleDetailRow("Приоритет", remark.priority)
                        SimpleDetailRow("Статус", remark.status)
                        SimpleDetailRow("Дата создания", remark.createdDate)
                        SimpleDetailRow("Срок устранения", remark.deadline)
                    }
                }

                // Фото с возможностью полноэкранного просмотра
                if (photoPaths.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(elevation = 2.dp) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Прикрепленные фото (${photoPaths.size})",
                                style = MaterialTheme.typography.subtitle1
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // ФОТО С ВОЗМОЖНОСТЬЮ КЛИКА - ИСПОЛЬЗУЕМ ПОСТОЯННЫЕ ПУТИ
                            Column {
                                photoPaths.forEachIndexed { index, path ->
                                    // СОЗДАЕМ URI ИЗ ПОСТОЯННОГО ПУТИ
                                    val uri = PhotoStorageUtils.getUriForAppFile(context, path)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                selectedPhotoUri = uri // ОТКРЫВАЕМ ПОЛНОЭКРАННЫЙ ПРОСМОТР
                                            },
                                        elevation = 1.dp
                                    ) {
                                        Column {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = "Фото замечания",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp),
                                                contentScale = ContentScale.Crop
                                            )
                                            Text(
                                                text = "Нажмите для просмотра в полный размер",
                                                style = MaterialTheme.typography.caption,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка закрытия
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}


@Composable
fun SimpleDetailRow(label: String, value: String) {
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