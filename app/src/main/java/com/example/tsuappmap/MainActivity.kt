package com.example.tsuappmap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import kotlinx.coroutines.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.tsuappmap.algorithm.AntColony.AntColony
import com.example.tsuappmap.algorithm.AntColony.Attractions
import com.example.tsuappmap.algorithm.AntColony.PointOfAttractions
import com.example.tsuappmap.algorithm.Astar.AStar
import com.example.tsuappmap.algorithm.Astar.CustomObstacle
import com.example.tsuappmap.map.CampusGrid
import com.example.tsuappmap.map.CampusMapView
import com.example.tsuappmap.map.MapRoute
import com.example.tsuappmap.map.MapRouteForAnt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        CampusGrid.load(this)
        setContent {
            TsuMapScreen()
        }
    }
}
@Composable
fun TsuMapScreen() {
    val context = LocalContext.current
    var isObstacleMode by remember { mutableStateOf(false) }
    var placingStart by remember { mutableStateOf(false) }
    var placingEnd by remember { mutableStateOf(false) }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var startPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var endPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var barierStart by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var selectedTab by remember {mutableStateOf(0)}
    var placingAntStart by remember { mutableStateOf(false) }
    var antStartPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var antStartSet by remember { mutableStateOf(false) }



    BottomSheetScaffold(
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(-30.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    TabButton("Построить маршрут (А*)",{ selectedTab = 1}, 12)

                    TabButton("Кластеризация", { selectedTab = 2 }, 12)
                    TabButton("Приобрести еду", { selectedTab = 3 }, 12)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    TabButton("Выбор и обход достоприм.", { selectedTab = 4 }, 12)
                    TabButton("Кнопка 5", { selectedTab = 5 }, 12)
                    TabButton("Кнопка 6", { selectedTab = 6 }, 12)
                }

                TabContent(selectedTab = selectedTab,
                    isObstacleMode = isObstacleMode,
                    onPlaceStart = {
                        isObstacleMode = false
                        placingStart = true
                        placingEnd = false
                        android.widget.Toast.makeText(
                            context, "Выбери стартовую точку", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onPlaceEnd = {
                        isObstacleMode = false
                        placingStart = false
                        placingEnd = true
                        android.widget.Toast.makeText(
                            context, "Выбери конечную точку", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onToggleObstacle = {
                        isObstacleMode = !isObstacleMode
                        barierStart = null
                        placingStart = false
                        placingEnd = false
                        android.widget.Toast.makeText(
                            context,
                            if (isObstacleMode) "Режим барьера включёе" else "Режим барьера выключен",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },

                    onPlaceAntStart = {
                        placingAntStart = true; placingStart = false
                        placingEnd = false; isObstacleMode = false
                        android.widget.Toast.makeText(context, "Тапни на карте — стартовая точка", android.widget.Toast.LENGTH_SHORT).show()

                    },

                    onRunAntColony = { selectedIndices ->
                        val start = antStartPoint
                        if (start == null) {
                            android.widget.Toast.makeText(context, "Сначала установи стартовую точку", android.widget.Toast.LENGTH_SHORT).show()
                            return@TabContent
                        }
                        val selectedPois = selectedIndices.sorted().map { Attractions.allPoint[it] }
                        mapRef?.let { map -> launchAntColony(map, context, start, selectedPois) }
                    },
                    antStartSet = antStartSet,
                )
            }
        },
        sheetPeekHeight = 56.dp,
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = ComposeColor.Black,
                width = 60.dp,
                height = 5.dp
            )
        }
    )  {
        Box(modifier = Modifier.fillMaxSize().background(ComposeColor.Black)) {

            CampusMapView(
                onMapReady = { map ->
                    val animationJob = arrayOf<Job?>(null)
                    mapRef = map

                    map.addOnMapClickListener { latLng ->
                        val cell = CampusGrid.latLonToCell(latLng.latitude, latLng.longitude)
                            ?: return@addOnMapClickListener true

                        when {
                            placingAntStart -> {
                                val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
                                if (walkable == null) {
                                    android.widget.Toast.makeText(context, "Нет доступных точек рядом", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    antStartPoint = walkable
                                    antStartSet = true
                                    placingAntStart = false
                                    MapRouteForAnt.setStartMarker(context, map, walkable)
                                    android.widget.Toast.makeText(context, "Стартовая точка установлена", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }

                            isObstacleMode -> {
                                if (barierStart == null) {
                                    barierStart = cell
                                    android.widget.Toast.makeText(
                                        context, "Выбери конец барьера", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else {
                                    val (r1, c1) = barierStart!!
                                    val (r2, c2) = cell
                                    CustomObstacle.addLine(r1, c1, r2, c2)
                                    barierStart = null
                                    MapRoute.drawObstacles(map)
                                }
                            }

                            placingStart -> {
                                val cell = CampusGrid.nearestWalkable(cell.first, cell.second)
                                if (cell == null) {
                                    android.widget.Toast.makeText(
                                        context, "Нет доступных точек рядом",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else {
                                    startPoint = cell
                                    placingStart = false
                                    MapRoute.setStartMarker(map, cell)
                                    android.widget.Toast.makeText(
                                        context, "Старт устанвлен", android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    endPoint?.let { end ->
                                        launchAStar(map, context, cell, end, animationJob)
                                    }
                                }
                            }

                            placingEnd -> {
                                val cell = CampusGrid.nearestWalkable(cell.first, cell.second)
                                if (cell == null) {
                                    android.widget.Toast.makeText(
                                        context, "Нет доступных точек рядом", android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else {
                                    endPoint = cell
                                    placingEnd = false
                                    MapRoute.setEndMarker(map, cell)
                                    android.widget.Toast.makeText(
                                        context, "Конечная точка установлена", android.widget.Toast.LENGTH_SHORT
                                    ).show()

                                    startPoint?.let {  start ->
                                        launchAStar(map, context, start, cell, animationJob)
                                    }
                                }
                            }
                        }
                        true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )


            Image(
                painter = painterResource(id = R.drawable.hits),
                contentDescription = null,
                modifier = Modifier.size(50.dp).align(Alignment.TopStart).padding(8.dp)
            )
        }
    }
}

//lifecyclescope

fun launchAStar(
    map: MapLibreMap,
    context: android.content.Context,
    start: Pair<Int, Int>,
    end: Pair<Int, Int>,
    jobRef: Array<Job?>
) {
    jobRef[0]?.cancel()
    MapRoute.clearSearch(map)
    MapRoute.clearRoute(map)

    jobRef[0] = CoroutineScope(Dispatchers.Main).launch {
        val steps = withContext(Dispatchers.Default) {
            AStar.findPathWithSteps(start.first, start.second, end.first, end.second)
        }
        for (step in steps) {
            if (step.visited != null && step.frontier != null) {
                MapRoute.drawSearchStep(map, step.visited, step.frontier, step.current)
            }
            delay(50L)
        }
        val lastStep = steps.last()
        MapRoute.clearSearch(map)
        if (lastStep.path != null) {
            MapRoute.drawRoute(map, lastStep.path)
        }
        else {
            android.widget.Toast.makeText(
                context, "Маршрут не найден", android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}

fun launchAntColony(
    map: MapLibreMap,
    context: android.content.Context,
    startCell: Pair<Int, Int>,
    selectedPois: List<PointOfAttractions>
) {
    CoroutineScope(Dispatchers.Main).launch {
        MapRouteForAnt.clearAntRoute(map)

        val result = withContext(Dispatchers.Default) {
            val cells = mutableListOf<Pair<Int, Int>>()
            cells.add(startCell)

            for (poi in selectedPois) {
                val cell = CampusGrid.latLonToCell(poi.lat, poi.lon)
                val walkable = if (cell != null) CampusGrid.nearestWalkable(cell.first, cell.second) else null
                if (walkable != null) cells.add(walkable)
            }

            val n = cells.size
            if (n < 2) return@withContext null

            val distMatrix = Array(n) { DoubleArray(n) }
            val paths = Array(n) { arrayOfNulls<List<Pair<Int, Int>>>(n) }

            for (i in 0 until n) {
                for (j in i + 1 until n) {
                    val astarResult = AStar.functiomthatwouldbeadded(
                        cells[i].first, cells[i].second,
                        cells[j].first, cells[j].second
                    )
                    val length = astarResult?.length ?: (Double.MAX_VALUE / 2)
                    val path = astarResult?.path ?: emptyList()
                    distMatrix[i][j] = length
                    distMatrix[j][i] = length
                    paths[i][j] = path
                    paths[j][i] = path.reversed()
                }
            }

            val tourOrder = AntColony.solve(distMatrix)

            val segments = mutableListOf<List<Pair<Int, Int>>>()
            for (k in 0 until tourOrder.size - 1) {
                val from = tourOrder[k]
                val to = tourOrder[k + 1]
                segments.add(paths[from][to] ?: emptyList())
            }
            val orderedCells = tourOrder.dropLast(1).map { cells[it] }

            Triple(segments, orderedCells, cells.size)
        }

        if (result == null) {
            android.widget.Toast.makeText(context, "Не удалось построить маршрут", android.widget.Toast.LENGTH_SHORT).show()
            return@launch
        }

        val (segments, orderedCells, totalPoints) = result
        MapRouteForAnt.drawAntRoude(context, map, segments, orderedCells)
        android.widget.Toast.makeText(
            context, "Маршрут построен! Точек: $totalPoints", android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}