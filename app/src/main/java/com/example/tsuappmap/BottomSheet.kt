package com.example.tsuappmap

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsuappmap.algorithm.AntColony.Attractions

@Composable
fun TabButton(label: String, onClick: () -> Unit, fontSize: Int) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(65.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(red = 0, green = 114, blue = 188), contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            label,
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            fontSize = fontSize.sp,
        )
    }
}

@Composable
fun TabContent(
    selectedTab: Int,
    isObstacleMode: Boolean,
    onPlaceStart: () -> Unit,
    onPlaceEnd: () -> Unit,
    onToggleObstacle: () -> Unit,
    onPlaceAntStart: () -> Unit,
    onRunAntColony: (Set<Int>) -> Unit,
    antStartSet: Boolean,
    onClearMap: () -> Unit,
    clearCounter: Int = 0,
    onShowManhattan: (Int) -> Unit,
    onShowEuclidian: (Int) -> Unit,
    onShowAstar: (Int) -> Unit,
    isAstarClustering: Boolean = false,
    astarProgress: Float = 0f,
    onShowDecisionTree: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0, 1 -> Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
                ) {
                    Button(
                        onClick = onPlaceStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(red = 0, green = 0, blue = 188),
                            contentColor = Color.White
                        )
                    ) { Text("Стартовая точка") }

                    Button(
                        onClick = onPlaceEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(red = 0, green = 0, blue = 188),
                            contentColor = Color.White
                        )
                    ) { Text("Конечная точка") }

                    Button(
                        onClick = onToggleObstacle,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(red = 0, green = 0, blue = 188),
                            contentColor = Color.White
                        )
                    ) { Text(if (isObstacleMode) "Барьеры: ВКЛ" else "Барьеры: ВЫКЛ") }
                }

                2 -> {
                    var clusterCount by remember { mutableStateOf(3) }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onShowEuclidian(clusterCount) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF003F8A), contentColor = Color.White
                            )
                        ) { Text("K-means (Евклид)") }

                        Button(
                            onClick = { onShowManhattan(clusterCount) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF005E8A), contentColor = Color.White
                            )
                        ) { Text("K-means (Манхэттен)") }

                        Button(
                            onClick = { if (!isAstarClustering) onShowAstar(clusterCount) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAstarClustering) Color(0xFF444444)
                                else Color(0xFF006B55), contentColor = Color.White
                            )
                        ) {
                            if (isAstarClustering) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        progress = { astarProgress },
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Text("Считается... ${(astarProgress * 100).toInt()}%")
                                }
                            } else {
                                Text("K-means (Пешеходный A*)")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1A1A2E), shape = RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Кластеров:",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.weight(1f))
                            (2..6).forEach { n ->
                                val isSelected = clusterCount == n
                                Button(
                                    onClick = { clusterCount = n },
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF0078D4) else Color(
                                            0xFF3A3A4A
                                        )
                                    )
                                ) {
                                    Text(
                                        "$n",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(red = 100, green = 100, blue = 100))
                ) { Text("Контент кнопки 3") }

                4 -> {
                    val selectedIndices = remember { mutableStateListOf<Int>() }
                    LaunchedEffect(clearCounter) { selectedIndices.clear() }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onPlaceAntStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (antStartSet) Color(
                                    red = 0, green = 150, blue = 80
                                )
                                else Color(red = 0, green = 114, blue = 188),
                                contentColor = Color.White
                            )
                        ) { Text(if (antStartSet) "Старт установлен" else "Поставить старт") }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(Attractions.allPoint) { index, poi ->
                                val isSelected = index in selectedIndices
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .background(
                                            color = if (isSelected) Color(0xFF005EB8) else Color(
                                                0xFF2A2A2A
                                            ), shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF64B5F6) else Color(
                                                0xFF555555
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            if (isSelected) selectedIndices.remove(index)
                                            else selectedIndices.add(index)
                                        }
                                        .padding(6.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = poi.name,
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 12.sp,
                                        maxLines = 3
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onRunAntColony(selectedIndices.toSet()) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(red = 180, green = 80, blue = 0),
                                contentColor = Color.White
                            ),
                            enabled = antStartSet && selectedIndices.isNotEmpty()
                        ) { Text("Построить маршрут обхода") }
                    }
                }

                5 -> Box(modifier = Modifier.fillMaxSize())

                6 -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(red = 100, green = 100, blue = 100))
                ) { Text("Контент кнопки 6") }
            }
        }

        Button(
            onClick = onClearMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .padding(top = 4.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(red = 120, green = 120, blue = 120),
                contentColor = Color.White
            )
        ) { Text("Очистить карту", fontSize = 13.sp) }
    }
}