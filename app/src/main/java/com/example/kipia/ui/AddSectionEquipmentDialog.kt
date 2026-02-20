package com.example.kipia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.kipia.model.EquipmentType

@Composable
fun AddSectionEquipmentDialog(
    sectionName: String,
    onDismiss: () -> Unit,
    onConfirm: (EquipmentType) -> Unit
) {
    var selectedType by remember { mutableStateOf<EquipmentType?>(null) }

    // Определяем доступные типы оборудования в зависимости от типа отсека
    val availableEquipmentTypes = remember(sectionName) {
        when {
            sectionName.contains("Инженерный", ignoreCase = true) -> {
                // Оборудование для инженерного отсека
                listOf(
                    EquipmentType.SHTM,
                    EquipmentType.FIRE_ALARM,
                    EquipmentType.SMOKE_DETECTOR,
                    EquipmentType.MANUAL_DETECTOR,
                    EquipmentType.SIREN,
                    EquipmentType.AIR_CONDITIONER,
                    EquipmentType.CONTROL_PANEL,
                    EquipmentType.FLOOD_DETECTOR,
                    EquipmentType.OPENING_DETECTOR,
                    EquipmentType.BKEP
                )
            }
            sectionName.contains("Трансформаторный", ignoreCase = true) -> {
                // Оборудование для трансформаторного отсека
                listOf(
                    EquipmentType.SMOKE_DETECTOR,
                    EquipmentType.MANUAL_DETECTOR,
                    EquipmentType.FLOOD_DETECTOR,
                    EquipmentType.OPENING_DETECTOR
                )
            }
            else -> {
                // Общее оборудование для любых секций
                listOf(
                    EquipmentType.FLOOD_DETECTOR,
                    EquipmentType.OPENING_DETECTOR,
                    EquipmentType.PRESSURE_GAUGE,
                    EquipmentType.PRESSURE_TRANSDUCER
                )
            }
        }
    }

    // Устанавливаем первый элемент по умолчанию
    LaunchedEffect(availableEquipmentTypes) {
        if (availableEquipmentTypes.isNotEmpty() && selectedType == null) {
            selectedType = availableEquipmentTypes.first()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Добавить оборудование в $sectionName",
                    style = MaterialTheme.typography.h6
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Выберите тип оборудования:")
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(availableEquipmentTypes) { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            Text(
                                text = type.displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss) {
                        Text("Отмена")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            selectedType?.let { onConfirm(it) }
                            onDismiss()
                        },
                        enabled = selectedType != null
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}