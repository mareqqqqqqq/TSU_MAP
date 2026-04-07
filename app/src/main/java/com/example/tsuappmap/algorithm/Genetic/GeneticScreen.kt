package com.example.tsuappmap.algorithm.Genetic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun GeneticScreen(
    userLat: Double = 56.4695,
    userLon: Double = 84.9475
) {
    val scope = rememberCoroutineScope()
    val selected = remember { mutableStateListOf<String>() }

    var isRunning by remember { mutableStateOf(false) }
    var generation by remember { mutableStateOf(0) }
    var bestRoute by remember { mutableStateOf<GeneticAlgorithm.Individual?>(null) }
    var totalTime by remember { mutableStateOf(0.0) }
    var noSolution by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            "Выберите что хотите купить:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.weight(1f).border(1.dp, Color.LightGray,
                RoundedCornerShape(8.dp))
        ) {
            items(GeneticAlgorithm.allMenuItems) { item ->
                val isSelected = item in selected
                Row(
                    modifier = Modifier.fillMaxWidth().clickable{
                        if (isSelected) selected.remove(item)
                        else selected.add(item)
                    }.background(if (isSelected) Color(0xFFDCEEFF) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, fontSize = 13.sp)
                    if (isSelected) {
                        Text("✓", color = Color(0xFFDCEEFF), fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
            }
        }

        Button(
            onClick = {
                if (selected.isEmpty()) return@Button
                isRunning = true
                noSolution = false
                generation = 0
                bestRoute = null

                scope.launch {
                    val result = withContext(Dispatchers.Default) {
                        GeneticAlgorithm.run(
                            userLat = userLat,
                            userLon = userLon,
                            selectedItems = selected.toList(),
                            generations = 150,
                            popSize = 40,
                            onGeneration = { gen, best ->
                                generation = gen
                                bestRoute = best
                            }
                        )
                    }

                    if (result.route.isEmpty()) {
                        noSolution = true
                    }
                    else {
                        var time = 0.0
                        var pLat = userLat
                        var pLon = userLon
                        result.route.forEach { cafe ->
                            time += GeneticAlgorithm.metersToMinutes(
                                GeneticAlgorithm.euclideanMeters(
                                    pLat, pLon, cafe.location.latitude,
                                    cafe.location.longitude
                                )
                            )
                            pLat = cafe.location.latitude
                            pLon = cafe.location.longitude
                        }
                        totalTime = time
                        bestRoute = result
                    }
                    isRunning = false
                }
            }, modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp), enabled = !isRunning && selected.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDCEEFF)
            )
        ) {
            Text( if (isRunning) "Поиск... покление $generation" else "Найти маршрут")
        }

        if (noSolution) {
            Text("Нет заведений с выбранными позициями", color = Color.Red, fontSize = 13.sp)
        }

        val route = bestRoute
        if (route != null && !isRunning && route.route.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFDCEEFF),
                    RoundedCornerShape(10.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Оптимальный маршрут:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                route.route.forEachIndexed { i, cafe ->
                    val items = GeneticAlgorithm.cafeMenu[cafe.name]?.filter { it in selected }
                        ?.joinToString (", ") ?: ""
                    Text("${i + 1}. ${cafe.name}  $items", fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("Время в пути: ~${totalTime.toInt()} мин", fontSize = 13.sp,
                    color = Color(0xFFDCEEFF), fontWeight = FontWeight.Medium)
            }
        }
    }
}