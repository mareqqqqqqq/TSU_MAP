package com.example.tsuappmap

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val featureQuestions = mapOf(
    "location" to "Где ты находишься?",
    "budget" to "Какой у тебя бюджет?",
    "time_available" to "Сколько у тебя времени?",
    "food_type" to "Что хочешь съесть/выпить?",
    "queue_tolerance" to "Готов ли стоять в очереди?",
    "weather" to "Какая сейчас погода?"
)

private val featureValueLabels = mapOf(
    "main_building" to "Гл. корпус",
    "second_building" to "2-й корпус",
    "campus_center" to "Центр кампуса",
    "bus_stop" to "Остановка",
    "low" to "Низкий",
    "medium" to "Средний",
    "high" to "Высокий",
    "very_short" to "Очень мало",
    "short" to "Мало",
    "coffee" to "Кофе",
    "pancakes" to "Блины",
    "full_meal" to "Обед",
    "snack" to "Перекус",
    "good" to "Хорошая",
    "bad" to "Плохая"
)

private val placeLabels = mapOf(
    "Starbooks" to "Starbooks",
    "Siberian_Pancakes" to "Сиб. блины",
    "Main_Cafeteria" to "Столовая",
    "Yarche" to "Ярче",
    "Bus_Stop_Coffee" to "Кофе у ост.",
    "Second_Building_Cafe" to "Кафе 2 корп.",
    "Vending_Machine" to "Автомат"
)

internal fun localFeature(f: String) = featureQuestions[f] ?: f
internal fun localValue(v: String) = featureValueLabels[v] ?: v
internal fun localPlace(p: String) = placeLabels[p] ?: p

internal val TsuBlue = Color(0xFF00539C)
internal val TsuDark = Color(0xFF002855)
internal val TsuLight = Color(0xFFE8F1FA)
internal val TsuAccent = Color(0xFF4A9FDF)
internal val TsuGray = Color(0xFFF5F7FA)
internal val TsuSuccess = Color(0xFF2E7D32)
internal val TsuWarn = Color(0xFFB71C1C)

@Composable
fun DecisionTreeModal(onDismiss: () -> Unit) {
    var currentCsv by remember { mutableStateOf(DEFAULT_CSV) }

    val parsedData = remember(currentCsv) {
        try {
            parseCSV(currentCsv)
        } catch (e: Exception) {
            null
        }
    }
    val tree = remember(parsedData) {
        parsedData?.let { (samples, features) -> buildTree(samples, features) }
    }

    var mode by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss, // для закрытия
        properties = DialogProperties(
            usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(TsuGray)
            ) {
                Column( // шапка
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TsuDark)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Дерево решений",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { showSettings = !showSettings }) {
                                Text(
                                    if (showSettings) "✕ CSV" else "⚙ CSV",
                                    color = if (showSettings) TsuAccent else Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                            TextButton(onClick = onDismiss) {
                                Text(
                                    "✕", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp
                                )
                            }
                        }
                    }

                    if (!showSettings) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(TsuBlue.copy(alpha = 0.4f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Выбрать место", "Дерево").forEachIndexed { idx, label ->
                                val selected = mode == idx
                                Button(
                                    onClick = { mode = idx },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) Color.White else Color.Transparent,
                                        contentColor = if (selected) TsuDark else Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (selected) 2.dp else 0.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        label,
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                AnimatedContent(
                    targetState = showSettings,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    //label = "settings_switch"
                ) { inSettings ->
                    if (inSettings) {
                        CsvEditorContent(
                            currentCsv = currentCsv,
                            onSave = { newCsv -> currentCsv = newCsv; showSettings = false },
                            onCancel = { showSettings = false })
                    } else if (tree == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ошибка в CSV. Откройте настройки и исправьте.",
                                color = TsuWarn,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    } else {
                        AnimatedContent(
                            targetState = mode,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "mode_switch"
                        ) { currentMode ->
                            when (currentMode) {
                                0 -> QuizContent(tree = tree, samples = parsedData!!.first)
                                else -> TreeGraphContent(tree = tree)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizContent(tree: TreeNode, samples: List<Sample>) {
    val answers = remember { mutableStateMapOf<String, String>() }
    val history = remember { mutableStateListOf<String>() }

    val currentNode = remember(answers.toMap()) {
        var node: TreeNode = tree
        for (feature in history) {
            val ans = answers[feature] ?: break
            if (node is TreeNode.Decision) node = node.branches[ans] ?: break
        }
        node
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (history.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, TsuBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Мои ответы:",
                    color = TsuDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(2.dp))
                history.forEach { feat ->
                    val ans = answers[feat]
                    if (ans != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                localFeature(feat),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                localValue(ans),
                                color = TsuDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        when (val node = currentNode) {
            is TreeNode.Leaf -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(TsuSuccess.copy(alpha = 0.1f))
                        .border(2.dp, TsuSuccess, RoundedCornerShape(16.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("✅", fontSize = 40.sp)
                    Text(
                        "Рекомендуем:",
                        color = TsuSuccess,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        localPlace(node.label),
                        color = TsuDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { answers.clear(); history.clear() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TsuBlue)
                ) {
                    Text("🔄 Начать заново", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            is TreeNode.Decision -> {
                val feature = node.feature
                val options = node.branches.keys.toList().sorted()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, TsuBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TsuLight), contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${history.size + 1}",
                                color = TsuBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            localFeature(feature),
                            color = TsuDark,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        options.forEach { value ->
                            Button(
                                onClick = { answers[feature] = value; history.add(feature) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TsuLight, contentColor = TsuDark
                                )
                            ) {
                                Text(
                                    localValue(value),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                if (history.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            val last = history.removeLastOrNull(); if (last != null) answers.remove(
                            last
                        )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TsuBlue)
                    ) {
                        Text("← Назад", color = TsuBlue, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}