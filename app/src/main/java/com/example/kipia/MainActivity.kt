package com.example.kipia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.kipia.database.AppDatabase
import com.example.kipia.ui.PKUViewModel
import com.example.kipia.ui.theme.KIPITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KIPITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    PKUScreen()
                }
            }
        }
    }
}

@Composable
fun PKUScreen() {
    val context = LocalContext.current
    val viewModel = remember {
        PKUViewModel(AppDatabase.getDatabase(context))
    }

    val pkus by viewModel.pkus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Список ПКУ", style = MaterialTheme.typography.h5)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(pkus) { pku ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = pku.name, style = MaterialTheme.typography.body1)
                            if (pku.description.isNotEmpty()) {
                                Text(
                                    text = pku.description,
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.deletePKU(pku.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.addPKU("Новый ПКУ", "Описание") }) {
            Text("Добавить ПКУ")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    KIPITheme {
        PKUScreen()
    }
}