// app/src/main/java/com/example/kipia/ui/ViewEquipmentScreen.kt
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kipia.database.AppDatabase
import com.example.kipia.database.DetailedEquipmentEntity
import com.example.kipia.model.EquipmentType

@Composable
fun ViewEquipmentScreen(
    equipmentId: Long,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val equipmentViewModel: EquipmentViewModel = viewModel(
        factory = EquipmentViewModelFactory(AppDatabase.getInstance(context))
    )

    var currentEquipment by remember { mutableStateOf<DetailedEquipmentEntity?>(null) }

    // Загружаем оборудование при открытии
    LaunchedEffect(equipmentId) {
        val equipment = equipmentViewModel.getEquipmentById(equipmentId)
        currentEquipment = equipment
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Просмотр оборудования") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onEditClick,
                        enabled = currentEquipment != null
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                }
            )
        }
    ) { innerPadding ->
        currentEquipment?.let { equipment ->
            EquipmentView(
                equipment = equipment,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            )
        } ?: run {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun EquipmentView(
    equipment: DetailedEquipmentEntity,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Основная информация
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Основная информация",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                EquipmentInfoRow("Название", equipment.name)
                EquipmentInfoRow("Тип оборудования", EquipmentType.valueOf(equipment.equipmentType).displayName)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Общие характеристики
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Общие характеристики",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                EquipmentInfoRow("Модель", equipment.model)
                EquipmentInfoRow("Производитель", equipment.manufacturer)
                EquipmentInfoRow("Заводской номер", equipment.serialNumber)
                EquipmentInfoRow("Год выпуска", equipment.productionYear)
                EquipmentInfoRow("Год поверки", equipment.verificationYear)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

// В функции EquipmentView в ViewEquipmentScreen.kt обновляем блок специфических характеристик:

// Специфические характеристики
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Специфические характеристики",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                EquipmentInfoRow("Номинал", equipment.nominal)
                EquipmentInfoRow("Граница давления", equipment.pressureLimit)
                EquipmentInfoRow("Версия ПО", equipment.softwareVersion)

                // Дополнительные характеристики для БУР, БКЭП и других релейных защит
                if (equipment.equipmentType in listOf("BUR", "BKEP", "DPS", "BKP", "UZR")) {
                    EquipmentInfoRow("МО", equipment.mo)
                    EquipmentInfoRow("МЗ", equipment.mz)
                    EquipmentInfoRow("МТО", equipment.mto)
                    EquipmentInfoRow("МТЗ", equipment.mtz)
                    EquipmentInfoRow("МУО", equipment.muo)
                    EquipmentInfoRow("МУЗ", equipment.muz)
                    EquipmentInfoRow("Кол.об.вых.зв", equipment.outputContacts)
                }
            }
        }
    }
}

@Composable
fun EquipmentInfoRow(label: String, value: String) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.body2
            )
        }
    }
}