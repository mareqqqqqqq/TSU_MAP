package com.example.tsuappmap.algorithm.Genetic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsuappmap.map.MapRoute
import kotlinx.coroutines.*
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.maps.MapLibreMap
import java.util.Calendar


private enum class FoodPhase {
    SELECT,
    PRECOMPUTING,
    RUNNING,
    RESULT
}


fun makeNumberedIcon(context: Context, label: String, bgColor: Int): Icon {
    val size = 72
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }

    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = Paint.Style.FILL
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = if (label.length > 1) 24f else 32f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint)
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, circlePaint)
    canvas.drawText(label, size / 2f, textY, textPaint)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}


@Composable
fun FoodRouteContent(
    mapRef: MapLibreMap?,
    startCell: Pair<Int, Int>?,
    onRequestPlaceStart: () -> Unit,
    onRouteBuilt: (Route?) -> Unit,
    context: android.content.Context
) {
    var phase by remember { mutableStateOf(FoodPhase.SELECT) }
    var pickedItems by remember { mutableStateOf(setOf<String>()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var aStarDone by remember { mutableIntStateOf(0) }
    var aStarTotal by remember { mutableIntStateOf(1) }
    var genCurrent by remember { mutableIntStateOf(0) }
    val genTotal = 250

    var foundRoute by remember { mutableStateOf<Route?>(null) }
    val jobRef = remember { mutableStateOf<Job?>(null) }

    when (phase) {

        FoodPhase.SELECT -> DishPickerScreen(
            picked = pickedItems,
            startCell = startCell,
            error = errorText,
            onToggle = { item ->
                pickedItems = if (item in pickedItems) pickedItems - item else pickedItems + item
            },
            onSetStart = onRequestPlaceStart,
            onBuild = {
                if (startCell == null) {
                    errorText = "Сначала укажите своё местоположение на карте"
                    return@DishPickerScreen
                }
                if (pickedItems.isEmpty()) {
                    errorText = "Выберите хотя бы одно блюдо"
                    return@DishPickerScreen
                }

                val cal = Calendar.getInstance()
                val h = cal.get(Calendar.HOUR_OF_DAY)
                val m = cal.get(Calendar.MINUTE)

                val closed = pickedItems.filter { dish ->
                    FoodDatabase.findEstablishmentsForItem(dish).none { it.isOpenAt(h, m) }
                }
                if (closed.isNotEmpty()) {
                    errorText = "Сейчас закрыто: ${closed.joinToString(", ")}"
                    return@DishPickerScreen
                }
                errorText = null

                val candidates = FoodDatabase.allEstablishments.filter { est ->
                    est.isOpenAt(h, m) && est.menu.any { it.name in pickedItems }
                }

                phase = FoodPhase.PRECOMPUTING
                aStarDone = 0; aStarTotal = 1

                jobRef.value = CoroutineScope(Dispatchers.Default).launch {
                    val data = RoutePrecomputer.compute(
                        startCell = startCell,
                        candidateEstablishments = candidates,
                        onProgress = { done, total ->
                            CoroutineScope(Dispatchers.Main).launch {
                                aStarDone = done; aStarTotal = total
                            }
                        }
                    )

                    if (data == null || data.establishments.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            errorText = "Не удалось проложить маршрут — попробуйте другую точку"
                            phase = FoodPhase.SELECT
                        }
                        return@launch
                    }

                    withContext(Dispatchers.Main) { phase = FoodPhase.RUNNING }

                    val ga = GeneticAlgorithm(
                        data = data,
                        requiredItems = pickedItems,
                        currentHour = h,
                        currentMinute = m,
                        totalGenerations = genTotal
                    )

                    ga.onGenerationUpdate = { route, gen ->
                        CoroutineScope(Dispatchers.Main).launch {
                            foundRoute = route
                            genCurrent = gen
                            if (gen % 15 == 0 || gen == 1) {
                                mapRef?.let { MapRoute.drawFoodRoute(it, route, isFinal = false) }
                            }
                        }
                    }

                    val result = ga.run()

                    withContext(Dispatchers.Main) {
                        foundRoute = result
                        if (result != null) {
                            mapRef?.let { map ->
                                MapRoute.drawFoodRoute(map, result, isFinal = true)
                                MapRoute.drawFoodEstablishmentMarkers(map, result, context)
                            }
                        }
                        phase = FoodPhase.RESULT
                        onRouteBuilt(result)
                    }
                }
            }
        )

        FoodPhase.PRECOMPUTING -> ComputingScreen(
            label = "Прокладываю дороги между точками...",
            progress = aStarDone.toFloat() / aStarTotal.coerceAtLeast(1),
            sub = "$aStarDone / $aStarTotal",
            onStop = {
                jobRef.value?.cancel()
                mapRef?.let { MapRoute.clearFoodRoute(it) }
                phase = FoodPhase.SELECT
                onRouteBuilt(null)
            }
        )

        FoodPhase.RUNNING -> ComputingScreen(
            label = "Генетический алгоритм ищет маршрут...",
            progress = genCurrent.toFloat() / genTotal,
            sub = "Поколение $genCurrent / $genTotal" +
                    (foundRoute?.let { "  •  ${it.totalDistMeters.toInt()} м" } ?: ""),
            onStop = {
                jobRef.value?.cancel()
                mapRef?.let { MapRoute.clearFoodRoute(it) }
                foundRoute = null
                phase = FoodPhase.SELECT
                onRouteBuilt(null)
            }
        )

        FoodPhase.RESULT -> RouteResultScreen(
            route = foundRoute,
            picked = pickedItems,
            onAgain = {
                jobRef.value?.cancel()
                mapRef?.let { MapRoute.clearFoodRoute(it) }
                foundRoute = null
                genCurrent = 0
                pickedItems = emptySet()
                phase = FoodPhase.SELECT
                onRouteBuilt(null)
            }
        )
    }
}


@Composable
private fun DishPickerScreen(
    picked: Set<String>,
    startCell: Pair<Int, Int>?,
    error: String?,
    onToggle: (String) -> Unit,
    onSetStart: () -> Unit,
    onBuild: () -> Unit
) {

    val allDishes = remember {
        FoodDatabase.allEstablishments
            .flatMap { it.menu }
            .distinctBy { it.name }
            .sortedWith(compareBy({ it.category.ordinal }, { it.name }))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 480.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Button(
            onClick = onSetStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (startCell != null)
                    Color(0xFF1B5E20) else Color(0xFF7B3A00),
                contentColor = Color.White
            )
        ) {
            Text(
                if (startCell != null) "Моё местоположение установлено ✓"
                else "Указать моё местоположение на карте",
                fontSize = 13.sp
            )
        }

        Text(
            "Что хотите купить?",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(allDishes) { dish ->
                val isOn = dish.name in picked
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            color = if (isOn) Color(0xFF005EB8) else Color(0xFF2A2A2A),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (isOn) 2.dp else 1.dp,
                            color = if (isOn) Color(0xFF64B5F6) else Color(0xFF555555),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggle(dish.name) }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dish.name,
                        fontSize = 10.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        maxLines = 3
                    )
                }
            }
        }

        if (error != null) {
            Text(error, color = Color(0xFFFF6B6B), fontSize = 11.sp)
        }

        Button(
            onClick = onBuild,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = picked.isNotEmpty() && startCell != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0072BC),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF333333),
                disabledContentColor = Color(0xFF777777)
            )
        ) {
            Text("Построить маршрут  (выбрано: ${picked.size})", fontSize = 13.sp)
        }
    }
}


@Composable
private fun ComputingScreen(
    label: String,
    progress: Float,
    sub: String,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Color(0xFF0072BC),
            trackColor = Color(0xFF333333)
        )

        Text(sub, fontSize = 12.sp, color = Color(0xFF9CA3AF))

        Spacer(Modifier.height(4.dp))

        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3A3A3A),
                contentColor = Color.White
            )
        ) {
            Text("Отмена", fontSize = 13.sp)
        }
    }
}


@Composable
private fun RouteResultScreen(
    route: Route?,
    picked: Set<String>,
    onAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (route == null) {
            Text(
                "Маршрут не найден",
                color = Color(0xFFFF6B6B),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        } else {
            Text(
                "Маршрут готов!",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF4CAF50)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatBadge("Расстояние", "${route.totalDistMeters.toInt()} м")
                StatBadge("Время", "${route.totalTimeMin.toInt()} мин")
                StatBadge("Точек", "${route.establishments.size}")
            }

            HorizontalDivider(color = Color(0xFF444444))

            Text(
                "Порядок посещения:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9CA3AF)
            )

            route.establishments.forEachIndexed { idx, est ->
                val dishes = est.menu
                    .filter { it.name in picked }
                    .joinToString(", ") { it.name }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF444444), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF005EB8), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${idx + 1}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            est.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        if (dishes.isNotEmpty())
                            Text(dishes, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                        Text(
                            "${est.openHour}:${"%02d".format(est.openMinute)}" +
                                    " – ${est.closeHour}:${"%02d".format(est.closeMinute)}",
                            fontSize = 10.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }

        Button(
            onClick = onAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3A3A3A),
                contentColor = Color.White
            )
        ) {
            Text("Выбрать заново", fontSize = 13.sp)
        }
    }
}


@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = Color(0xFF6B7280))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}