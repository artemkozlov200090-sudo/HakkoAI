package com.example.hakkoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Bg = Color(0xFF09090B)
private val Panel = Color(0xFF19191E)
private val Muted = Color(0xFF92929B)
private val Pink = Color(0xFFEC5CB7)
private val Purple = Color(0xFF7658F5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HakkoApp() }
    }
}

data class Character(val name: String, val description: String, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HakkoApp() {
    val characters = listOf(
        Character("Николь", "Твоя новая знакомая. Начните разговор.", "Рекомендуемое"),
        Character("Алиса", "Умная, спокойная и немного загадочная.", "Школа"),
        Character("Рэн Ямаширо", "Сильный лидер с непростым характером.", "Фэнтези"),
        Character("Белла", "Всегда готова поддержать беседу.", "Лучший друг"),
        Character("Мия", "Любит приключения и новые знакомства.", "В тренде"),
        Character("Акира", "Творческая натура и любительница музыки.", "Официальный")
    )
    var tab by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<Character?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Рекомендуемое") }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Pink,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            if (selected != null) {
                ChatScreen(selected!!, onBack = { selected = null })
            } else {
                Scaffold(
                    containerColor = Bg,
                    bottomBar = {
                        NavigationBar(containerColor = Color.Black, contentColor = Color.White) {
                            val items = listOf(
                                Icons.Default.Home to "Главная",
                                Icons.Default.Videocam to "Видео",
                                Icons.Default.AddCircle to "Создать",
                                Icons.Default.ChatBubble to "Чаты",
                                Icons.Default.Person to "Профиль"
                            )
                            items.forEachIndexed { i, pair ->
                                NavigationBarItem(
                                    selected = tab == i,
                                    onClick = { if (i == 2) showCreate = true else tab = i },
                                    icon = { Icon(pair.first, pair.second) },
                                    label = { Text(pair.second, fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Pink, indicatorColor = Color(0xFF30202D),
                                        unselectedIconColor = Muted, selectedTextColor = Pink,
                                        unselectedTextColor = Muted
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    when (tab) {
                        0 -> HomeScreen(characters, query, { query = it }, category, { category = it }, { selected = it }, padding)
                        1 -> FeatureScreen("Видео и звонки", listOf("Голосовые звонки с персонажами", "Видеосцены и фоны", "Настройка режима общения"), padding)
                        3 -> FeatureScreen("Чаты", listOf("Личные переписки", "Комментарии и реакции", "Уведомления", "Новые подписчики"), padding)
                        4 -> FeatureScreen("Профиль", listOf("Мои компаньоны", "Мои голоса", "Альбом", "Виджеты", "События", "Настройки аккаунта"), padding)
                    }
                }
                if (showCreate) {
                    ModalBottomSheet(onDismissRequest = { showCreate = false }, containerColor = Panel) {
                        Text("НАЧАТЬ СОЗДАНИЕ", Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(18.dp))
                        listOf("Персонаж" to "Создать нового AI-персонажа", "Голос" to "Настроить голос персонажа").forEach { (title, sub) ->
                            ListItem(
                                headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(sub, color = Muted) },
                                leadingContent = { Icon(Icons.Default.AutoAwesome, null, tint = Pink) },
                                modifier = Modifier.clickable { showCreate = false },
                                colors = ListItemDefaults.colors(containerColor = Panel)
                            )
                        }
                        Spacer(Modifier.height(30.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    characters: List<Character>, query: String, onQuery: (String) -> Unit,
    category: String, onCategory: (String) -> Unit, onOpen: (Character) -> Unit,
    padding: PaddingValues
) {
    val cats = listOf("Рекомендуемое", "В тренде", "Официальный", "Мафия", "Заклятый враг", "Школа", "Лучший друг")
    Column(Modifier.fillMaxSize().padding(padding)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("HakkoAI", fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            Text("Get H+", color = Pink, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(14.dp))
            Icon(Icons.Default.Search, null, tint = Color.White)
        }
        OutlinedTextField(
            value = query, onValueChange = onQuery, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            placeholder = { Text("Поиск персонажей…", color = Muted) }, singleLine = true,
            shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Pink, unfocusedBorderColor = Panel
            )
        )
        Spacer(Modifier.height(10.dp))
        androidx.compose.foundation.lazy.LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cats) { c ->
                FilterChip(selected = category == c, onClick = { onCategory(c) }, label = { Text(c) })
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(characters.filter { it.name.contains(query, true) || it.description.contains(query, true) }) { character ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp).clickable { onOpen(character) },
                    shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                            Text(character.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(character.description, color = Muted, fontSize = 11.sp, maxLines = 2)
                            Text(character.category, color = Pink, fontSize = 10.sp)
                        }
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF77717D), modifier = Modifier.align(Alignment.Center).size(42.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(character: Character, onBack: () -> Unit) {
    var message by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(listOf<String>()) }
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ArrowBack, null, Modifier.clickable { onBack() }, tint = Color.White)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(character.name, fontWeight = FontWeight.Bold)
                Text("AI-персонаж · демо-режим", color = Muted, fontSize = 11.sp)
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.Bottom) {
            Text("Начни знакомство с ${character.name}.", color = Muted)
            sent.forEach { Text(it, Modifier.padding(top = 10.dp).background(Panel, RoundedCornerShape(14.dp)).padding(12.dp)) }
        }
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(message, { message = it }, Modifier.weight(1f), placeholder = { Text("Сообщение…") }, shape = RoundedCornerShape(24.dp))
            IconButton(onClick = { if (message.isNotBlank()) { sent = sent + message; message = "" } }) {
                Icon(Icons.Default.Send, null, tint = Pink)
            }
        }
    }
}

@Composable
fun FeatureScreen(title: String, features: List<String>, padding: PaddingValues) {
    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        features.forEach {
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), colors = CardDefaults.cardColors(containerColor = Panel)) {
                Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Pink)
                    Spacer(Modifier.width(14.dp))
                    Text(it)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = Muted)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Раздел подготовлен для подключения полноценного серверного функционала.", color = Muted, fontSize = 12.sp)
    }
}
