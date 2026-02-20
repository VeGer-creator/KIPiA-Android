// app/src/main/java/com/example/kipia/ui/HelpScreen.kt
package com.example.kipia.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HelpScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Справка и описание") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Приложение КИПиА",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Назначение:",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Приложение предназначено для фиксации данных обследования оборудования КИПиА, " +
                        "включая добавление фото и комментариев, а также управления замечаниями и мероприятиями по ним. " +
                        "Оно позволяет структурировать информацию о контрольных пунктах (КП), пунктах контроля и управления (ПКУ), " +
                        "участках магистральных нефтепроводов (МН), объектах (ОД, В, Задвижки) и установленном оборудовании.",
                style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Возможности:",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "• Создание иерархии: КП -> ПКУ/Участки МН -> Объекты -> Оборудование\n" +
                        "• Добавление, редактирование и удаление элементов структуры\n" +
                        "• Добавление замечаний с приоритетами, сроками и фото\n" +
                        "• Архивирование выполненных замечаний\n" +
                        "• Просмотр и редактирование характеристик оборудования\n" +
                        "• Просмотр фото в полноэкранном режиме\n" +
                        "• Справочник типов оборудования\n" +
                        "• Локальное хранение данных",
                style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Использование:",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "1. Начните с создания Контрольного Пункта (КП).\n" +
                        "2. Добавьте ПКУ или Участки МН к КП.\n" +
                        "3. Внутри ПКУ создайте отсеки (Инженерный, Трансформаторный).\n" +
                        "4. На Участке МН добавляйте объекты (ОД, В, Задвижки).\n" +
                        "5. К объектам и отсекам привязывайте оборудование.\n" +
                        "6. Используйте вкладку 'Замечания' для фиксации и отслеживания проблем.",
                style = MaterialTheme.typography.body1
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Версия: 1.0",
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}