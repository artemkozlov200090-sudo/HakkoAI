package com.example.hakkoai

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF11111B)
private val Panel = Color(0xFF1B1B29)
private val Muted = Color(0xFF9A9AAF)
private val Violet = Color(0xFF8B6CFF)
private val Pink = Color(0xFFFF78B7)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HakkoApp() }
    }
}

@Composable
private fun HakkoApp() {
    var tab by remember { mutableStateOf(0) }
    var selectedCharacter by remember { mutableStateOf("Neneko") }
    var chatText by remember { mutableStateOf("") }
    var conversation by remember { mutableStateOf(listOf<ChatMessage>()) }
    var isLoading by remember { mutableStateOf(false) }
    var chatError by remember { mutableStateOf<String?>(null) }
    val chatExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Ink, surface = Panel, primary = Violet,
            secondary = Pink, onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Ink,
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF171722), contentColor = Color.White) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.ChatBubble, null) }, label = { Text("Chat") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.Explore, null) }, label = { Text("Explore") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Add, null) }, label = { Text("Create") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3 },
                        icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
                }
            }
        ) { padding ->
            when (tab) {
                0 -> ChatHome(
                    selectedCharacter = selectedCharacter,
                    onSelect = { selectedCharacter = it },
                    messages = conversation,
                    isLoading = isLoading,
                    error = chatError,
                    text = chatText,
                    onText = { chatText = it },
                    onSend = { outgoing ->
                        if (outgoing.isNotBlank() && !isLoading) {
                            val updatedHistory = conversation + ChatMessage("user", outgoing)
                            conversation = updatedHistory
                            chatText = ""
                            isLoading = true
                            chatError = null
                            val character = selectedCharacter
                            chatExecutor.execute {
                                try {
                                    val answer = ChatApi.reply(character, updatedHistory)
                                    mainHandler.post {
                                        conversation = conversation + ChatMessage("assistant", answer)
                                        isLoading = false
                                    }
                                } catch (error: Exception) {
                                    mainHandler.post {
                                        chatError = error.message ?: "Не удалось получить ответ ИИ."
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
                1 -> ExploreScreen(onSelect = {
                    selectedCharacter = it
                    tab = 0
                }, modifier = Modifier.padding(padding))
                2 -> CreateScreen(modifier = Modifier.padding(padding))
                else -> ProfileScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun ChatHome(
    selectedCharacter: String,
    onSelect: (String) -> Unit,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    error: String?,
    text: String,
    onText: (String) -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("HAKKO AI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                    Text("Your AI companion", color = Muted, fontSize = 12.sp)
                }
                Box(Modifier.size(42.dp).clip(CircleShape).background(Panel), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Search, null, tint = Color.White)
                }
            }
        }
        item {
            Text("Your companions", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CharacterChip("Neneko", "🌸", selectedCharacter == "Neneko") { onSelect("Neneko") }
                CharacterChip("Nika", "🖤", selectedCharacter == "Nika") { onSelect("Nika") }
                CharacterChip("Yutao", "✨", selectedCharacter == "Yutao") { onSelect("Yutao") }
            }
        }
        item {
            Box(
                Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF493D83), Color(0xFF9C5C9F), Color(0xFFDF8BAA))))
                    .padding(22.dp)
            ) {
                Column(Modifier.align(Alignment.BottomStart)) {
                    Text("✦  YOUR COMPANION", color = Color.White.copy(alpha = .8f), fontSize = 11.sp, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(selectedCharacter, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Text("Ready to talk whenever you are.", color = Color.White.copy(alpha = .85f), fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Ink)) {
                        Text("Start voice chat")
                    }
                }
                Text(if (selectedCharacter == "Neneko") "🌸" else if (selectedCharacter == "Nika") "🖤" else "✨",
                    modifier = Modifier.align(Alignment.TopEnd), fontSize = 64.sp)
            }
        }
        item {
            Text("Continue your conversation", fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
            Surface(color = Panel, shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (selectedCharacter == "Neneko") "🌸" else "✨", fontSize = 28.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(selectedCharacter, fontWeight = FontWeight.SemiBold)
                            Text("Online · AI companion", color = Muted, fontSize = 12.sp)
                        }
                    }
                    if (messages.isEmpty()) {
                        Text("Привет! Я готов ответить на твои вопросы. О чём поговорим?",
                            color = Color(0xFFE2E0EC), fontSize = 14.sp)
                    }
                    messages.forEach { message ->
                        val isUser = message.role == "user"
                        Surface(
                            color = if (isUser) Color(0xFF393052) else Color(0xFF252536),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(if (isUser) "Ты" else selectedCharacter,
                                    color = if (isUser) Color(0xFFC9BFFF) else Pink,
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(message.text, color = Color(0xFFE2E0EC), fontSize = 14.sp)
                            }
                        }
                    }
                    if (isLoading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Violet)
                            Spacer(Modifier.width(10.dp))
                            Text("$selectedCharacter печатает…", color = Muted, fontSize = 12.sp)
                        }
                    }
                    if (error != null) {
                        Text(error, color = Color(0xFFFF9B9B), fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = text, onValueChange = onText,
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Message...", color = Muted) },
                            shape = RoundedCornerShape(18.dp), singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Violet, unfocusedBorderColor = Color(0xFF414052),
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { onSend(text.trim()) },
                            enabled = text.isNotBlank() && !isLoading,
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(if (text.isNotBlank() && !isLoading) Violet else Color(0xFF414052))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterChip(name: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) Color(0xFF393052) else Panel,
        shape = RoundedCornerShape(18.dp),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, Violet) else null,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(horizontal = 15.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(name, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ExploreScreen(onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    val characters = listOf(
        Triple("Neneko", "Gentle, cheerful companion", "🌸"),
        Triple("Nika", "Bold, witty and playful", "🖤"),
        Triple("Yutao", "Curious, bright and adventurous", "✨")
    )
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Explore", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Find a companion for every moment.", color = Muted)
        }
        item {
            OutlinedTextField(value = "", onValueChange = {}, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search characters") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Violet, unfocusedBorderColor = Color(0xFF414052)
                ))
        }
        item { Text("Featured companions", fontSize = 20.sp, fontWeight = FontWeight.SemiBold) }
        items(characters) { item ->
            Surface(color = Panel, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().clickable { onSelect(item.first) }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(Violet, Pink))),
                        contentAlignment = Alignment.Center) { Text(item.third, fontSize = 34.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.first, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(item.second, color = Muted, fontSize = 13.sp)
                    }
                    Text("Open  ›", color = Color(0xFFC6B7FF), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CreateScreen(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Create a companion", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Build a character with a personality of your own.", color = Muted)
        Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF3F356C), Color(0xFF9A5C9F)))),
            contentAlignment = Alignment.Center) { Text("＋", color = Color.White, fontSize = 54.sp) }
        OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Character name") }, shape = RoundedCornerShape(16.dp))
        OutlinedTextField(description, { description = it }, modifier = Modifier.fillMaxWidth().height(130.dp),
            label = { Text("Personality and description") }, shape = RoundedCornerShape(16.dp))
        Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Violet)) { Text("Create character") }
    }
}

@Composable
private fun ProfileScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("Profile", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Surface(color = Panel, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Violet, Pink))),
                    contentAlignment = Alignment.Center) { Text("☺", fontSize = 30.sp) }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Guest account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Sign in to sync your companions", color = Muted, fontSize = 12.sp)
                }
            }
        }
        listOf("My characters", "Memories", "Voice settings", "App settings", "Help & feedback").forEach {
            Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(it, fontWeight = FontWeight.Medium)
                    Text("›", color = Muted)
                }
            }
        }
    }
}
