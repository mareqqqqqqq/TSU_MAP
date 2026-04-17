package com.example.tsuappmap

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.tsuappmap.algorithm.Astar.AStar
import com.example.tsuappmap.algorithm.Astar.CustomObstacle
import com.example.tsuappmap.algorithm.Genetic.Route
import com.example.tsuappmap.map.CampusGrid
import com.example.tsuappmap.map.CampusMapView
import com.example.tsuappmap.map.MapRoute
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        CampusGrid.load(this)
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1001
        )
        setContent { TsuMapScreen() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TsuMapScreen() {
    val context = LocalContext.current

    var isObstacleMode by remember { mutableStateOf(false) }
    var placingStart by remember { mutableStateOf(false) }
    var placingEnd by remember { mutableStateOf(false) }
    var placingFoodStart by remember { mutableStateOf(false) }

    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var startPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var endPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var barierStart by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var foodStartCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var selectedTab by remember { mutableStateOf(0) }
    val animationJob = remember { arrayOf<Job?>(null) }

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
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    TabButton("Построить маршрут (А*)", { selectedTab = 1 }, 12)
                    TabButton("Кластеризация", { selectedTab = 2 }, 12)
                    TabButton("Приобрести еду", { selectedTab = 3 }, 12)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        10.dp,
                        Alignment.CenterHorizontally
                    )
                ) {
                    TabButton("Выбор и обход достоприм.", { selectedTab = 4 }, 12)
                    TabButton("Кнопка 5", { selectedTab = 5 }, 12)
                    TabButton("Кнопка 6", { selectedTab = 6 }, 12)
                }

                TabContent(
                    selectedTab = selectedTab,
                    isObstacleMode = isObstacleMode,
                    context = context,
                    mapRef = mapRef,
                    foodStartCell = foodStartCell,
                    onFoodRouteBuilt = { route: Route? ->
                        if (route == null) {
                            mapRef?.let { MapRoute.clearFoodRoute(it) }
                        }
                    },
                    onRequestFoodStart = {
                        foodStartCell = null
                        placingFoodStart = true
                        placingStart = false
                        placingEnd = false
                        isObstacleMode = false
                        android.widget.Toast.makeText(
                            context, "Нажмите на карту, чтобы указать ваше местоположение",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onPlaceStart = {
                        isObstacleMode = false
                        placingStart = true
                        placingEnd = false
                        android.widget.Toast.makeText(
                            context, "Выбери стартовую точку", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onPlaceEnd = {
                        isObstacleMode = false
                        placingStart = false
                        placingEnd = true
                        android.widget.Toast.makeText(
                            context, "Выбери конечную точку", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onToggleObstacle = {
                        isObstacleMode = !isObstacleMode
                        barierStart = null
                        placingStart = false
                        placingEnd = false
                        android.widget.Toast.makeText(
                            context,
                            if (isObstacleMode) "Режим барьера включён" else "Режим барьера выключен",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onReset = {
                        startPoint = null
                        endPoint = null
                        isObstacleMode = false
                        barierStart = null
                        CustomObstacle.clear()
                        mapRef?.let { map ->
                            MapRoute.clearMarkers(map)
                            MapRoute.clearRoute(map)
                            MapRoute.clearSearch(map)
                            MapRoute.drawObstacles(map)
                        }
                        android.widget.Toast.makeText(
                            context, "Всё сброшено", android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onMyLocationStart = {
                        val loc = getMyLocation(context)
                        if (loc == null) {
                            android.widget.Toast.makeText(
                                context,
                                "Нет доступа к геолокации",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val (lat, lon) = loc
                            val rawCell = CampusGrid.latLonToCell(lat, lon)
                            val cell = if (rawCell != null)
                                CampusGrid.nearestWalkable(rawCell.first, rawCell.second) else null
                            if (cell == null) {
                                android.widget.Toast.makeText(
                                    context, "Вы вне зоны карты", android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                startPoint = cell
                                mapRef?.let { map ->
                                    MapRoute.setStartMarker(map, cell)
                                    endPoint?.let { end ->
                                        launchAStar(
                                            map,
                                            context,
                                            cell,
                                            end,
                                            animationJob
                                        )
                                    }
                                }
                                android.widget.Toast.makeText(
                                    context, "Старт — ваше текущее местоположение",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    onMyLocationEnd = {
                        val loc = getMyLocation(context)
                        if (loc == null) {
                            android.widget.Toast.makeText(
                                context,
                                "Нет доступа к геолокации",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            val (lat, lon) = loc
                            val rawCell = CampusGrid.latLonToCell(lat, lon)
                            val cell = if (rawCell != null)
                                CampusGrid.nearestWalkable(rawCell.first, rawCell.second) else null
                            if (cell == null) {
                                android.widget.Toast.makeText(
                                    context, "Вы вне зоны карты", android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                endPoint = cell
                                mapRef?.let { map ->
                                    MapRoute.setEndMarker(map, cell)
                                    startPoint?.let { start ->
                                        launchAStar(
                                            map,
                                            context,
                                            start,
                                            cell,
                                            animationJob
                                        )
                                    }
                                }
                                android.widget.Toast.makeText(
                                    context, "Финиш — ваше текущее местоположение",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
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
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Black)) {

            CampusMapView(
                onMapReady = { map ->
                    mapRef = map

                    map.addOnMapClickListener { latLng ->
                        val cell = CampusGrid.latLonToCell(latLng.latitude, latLng.longitude)
                            ?: return@addOnMapClickListener true

                        when {
                            placingFoodStart -> {
                                val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
                                if (walkable == null) {
                                    android.widget.Toast.makeText(
                                        context, "Нет доступных точек рядом",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    foodStartCell = walkable
                                    placingFoodStart = false
                                    MapRoute.setStartMarker(map, walkable)
                                    android.widget.Toast.makeText(
                                        context, "Начальная точка установлена",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            isObstacleMode -> {
                                if (barierStart == null) {
                                    barierStart = cell
                                    android.widget.Toast.makeText(
                                        context, "Выбери конец барьера",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val (r1, c1) = barierStart!!
                                    val (r2, c2) = cell
                                    CustomObstacle.addLine(r1, c1, r2, c2)
                                    barierStart = null
                                    MapRoute.drawObstacles(map)
                                }
                            }

                            placingStart -> {
                                val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
                                if (walkable == null) {
                                    android.widget.Toast.makeText(
                                        context, "Нет доступных точек рядом",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    startPoint = walkable
                                    placingStart = false
                                    MapRoute.setStartMarker(map, walkable)
                                    android.widget.Toast.makeText(
                                        context,
                                        "Старт установлен",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    endPoint?.let { end ->
                                        launchAStar(
                                            map,
                                            context,
                                            walkable,
                                            end,
                                            animationJob
                                        )
                                    }
                                }
                            }

                            placingEnd -> {
                                val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
                                if (walkable == null) {
                                    android.widget.Toast.makeText(
                                        context, "Нет доступных точек рядом",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    endPoint = walkable
                                    placingEnd = false
                                    MapRoute.setEndMarker(map, walkable)
                                    android.widget.Toast.makeText(
                                        context, "Конечная точка установлена",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    startPoint?.let { start ->
                                        launchAStar(
                                            map,
                                            context,
                                            start,
                                            walkable,
                                            animationJob
                                        )
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
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }
    }
}


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
        val channel = Channel<AStar.SearchStep>(capacity = 4)

        val aStarJob = launch(Dispatchers.Default) {
            AStar.findPathWithChannel(start.first, start.second, end.first, end.second, channel)
        }

        for (step in channel) {
            MapRoute.drawSearchStep(map, step.visited, step.frontier, step.current)
            delay(80L)
            if (step.done) {
                MapRoute.clearSearch(map)
                if (step.path != null) {
                    MapRoute.drawRoute(map, step.path)
                } else {
                    android.widget.Toast.makeText(
                        context, "Маршрут не найден", android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
                break
            }
        }
        aStarJob.cancel()
    }
}


fun getMyLocation(context: android.content.Context): Pair<Double, Double>? {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) return null

    val lm = context.getSystemService(android.content.Context.LOCATION_SERVICE) as LocationManager
    for (provider in listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
        val loc = lm.getLastKnownLocation(provider)
        if (loc != null) return Pair(loc.latitude, loc.longitude)
    }
    return null
}