package com.example.tsuappmap

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
    "budget" to "Какой у тебя бюджет",
    "time_available" to "Сколько у тебя времене",
    "time_avaliable" to "Сколько у тебя времени?",
    "food_type" to "Готов ли ты стоять в очереди",
    "queue_tolerance" to "Котов ли ты стоять в очереди?",
    "weather" to "Какая сейчас погода?"
)

private val featureValueLabels = mapOf(
    "main_building" to "Главный корпус",
    "second_building" to "Второй корпус",
    "campus_center" to "Центр кампуса",
    "bus_stop" to "ОСтановка",
    "low" to "Низкий",
    "medium" to "Средний",
    "high" to "Высокий",
    "very_short" to "Очень мало",
    "short" to "Мало",
    "coffee" to "Кофе",
    "pancakes" to "Блины",
    "full_meal" to "Полный обед",
    "snack" to "Перекус",
    "good" to "Хорошая",
    "bad" to "Плохая"
)

private val placeLabels = mapOf(
    "Starbooks" to "Starbooks",
    "Siberian_Pancakes" to "Сибирские блины",
    "Main_Cafeteria" to "Столовая ТГУ",
    "Yarche" to "Ярче",
    "Bus_Stop_Coffee" to "Кофе на остановке",
    "Second_Building_Cafe" to "Кофе у второго корпуса",
    "Vending_Machine" to "Автомат"
)

private fun localFeature(f: String) = featureQuestions[f] ?: f
private fun localValue(v: String) = featureValueLabels[v] ?: v
private fun localPlace(p: String) = placeLabels[p] ?: p

private val TsuBlue    = Color(0xFF00539C)
private val TsuDark    = Color(0xFF002855)
private val TsuLight   = Color(0xFFE8F1FA)
private val TsuAccent  = Color(0xFF4A9FDF)
private val TsuGray    = Color(0xFFF5F7FA)
private val TsuSuccess = Color(0xFF2E7D32)

@Composable
fun DecisionTreeModal(onDismiss: () -> Unit) {
    val (samples, featureNames) = remember { parseCSV(DEFAULT_CSV) }
    val tree = remember { buildTree(samples, featureNames ) }

    var mode by remember { mutableStateOf(0 )}

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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
                        .clip(RoundedCornerShape(topStart = 25.dp, topEnd = 24.dp))
                        .background(TsuGray)
                )
                {
                    Column(
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
                            TextButton(onClick = onDismiss) {
                                Text("✕", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                           modifier = Modifier
                               .fillMaxWidth()
                               .clip(RoundedCornerShape(10.dp))
                               .background(TsuBlue.copy(alpha = 0.4f))
                               .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Выбрать место", "Показать дерево").forEachIndexed { idx, label ->
                                val selected = mode == idx
                                Button(
                                    onClick = { mode = idx },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) Color.White else Color.Transparent,
                                        contentColor = if (selected) TsuDark else Color.White
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = if (selected) 2.dp else 0.dp
                                    ),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "mode_switch"
                    ) {
                        currentMode ->
                        when (currentMode) {

                        }
                    }
                }
            }

    }
}

