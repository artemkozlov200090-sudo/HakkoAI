package com.example.hakkoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var selected by remember { mutableStateOf("Главная") }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("HakkoAI", style = MaterialTheme.typography.headlineLarge)
                        Text("Твоя AI-платформа")
                        Text(selected, style = MaterialTheme.typography.titleLarge)
                        Button(onClick = { selected = "Создание" }) { Text("Создать") }
                        Button(onClick = { selected = "Чаты" }) { Text("Мои чаты") }
                        Button(onClick = { selected = "Профиль" }) { Text("Профиль") }
                    }
                }
            }
        }
    }
}
