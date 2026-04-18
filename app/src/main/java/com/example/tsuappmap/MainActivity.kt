package com.example.tsuappmap

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tsuappmap.algorithm.Astar.AStar
import com.example.tsuappmap.algorithm.Genetic.Route
import com.example.tsuappmap.algorithm.NeuralNetwork.DigitDrawFullScreen
import com.example.tsuappmap.map.CampusGrid
import com.example.tsuappmap.map.CampusMapView
import com.example.tsuappmap.map.MapRoute
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
fun TsuMapScreen(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current

    var placingFoodStart by remember { mutableStateOf(false) }
    var foodStartCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }

    LaunchedEffect(vm.clearCounter) {
        placingFoodStart = false
        foodStartCell = null
    }

    if (vm.showDigitScreen) {
        DigitDrawFullScreen(onClose = { vm.showDigitScreen = false })
        return
    }

    BottomSheetScaffold(
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(-30.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    TabButton("Построить маршрут (А*)", { vm.selectedTab = 1 }, 12)
                    TabButton("Кластеризация", { vm.selectedTab = 2 }, 12)
                    TabButton("Приобрести еду", { vm.selectedTab = 3 }, 12)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    TabButton("Выбор и обход достоприм.", { vm.selectedTab = 4 }, 12)
                    TabButton("Оценить заведение (бета)", { vm.showDigitScreen = true }, 12)
                    TabButton("Кнопка 6", { vm.selectedTab = 6 }, 12)
                }

                TabContent(
                    selectedTab = vm.selectedTab,
                    isObstacleMode = vm.isObstacleMode,
                    onPlaceStart = { vm.onPlaceStartClicked(context) },
                    onPlaceEnd = { vm.onPlaceEndClicked(context) },
                    onToggleObstacle = { vm.onToggleObstacle(context) },
                    onReset = { vm.clearMap() },
                    onMyLocationStart = {
                        val loc = getMyLocation(context)
                        if (loc == null) {
                            Toast.makeText(context, "Нет доступа к геолокации", Toast.LENGTH_SHORT).show()
                        } else {
                            val (lat, lon) = loc
                            val rawCell = CampusGrid.latLonToCell(lat, lon)
                            val cell = rawCell?.let { CampusGrid.nearestWalkable(it.first, it.second) }
                            if (cell == null) {
                                Toast.makeText(context, "Вы вне зоны карты", Toast.LENGTH_SHORT).show()
                            } else {
                                mapRef?.let { map ->
                                    MapRoute.setStartMarker(map, cell)
                                    Toast.makeText(context, "Старт — ваше местоположение", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onMyLocationEnd = {
                        val loc = getMyLocation(context)
                        if (loc == null) {
                            Toast.makeText(context, "Нет доступа к геолокации", Toast.LENGTH_SHORT).show()
                        } else {
                            val (lat, lon) = loc
                            val rawCell = CampusGrid.latLonToCell(lat, lon)
                            val cell = rawCell?.let { CampusGrid.nearestWalkable(it.first, it.second) }
                            if (cell == null) {
                                Toast.makeText(context, "Вы вне зоны карты", Toast.LENGTH_SHORT).show()
                            } else {
                                mapRef?.let { map ->
                                    MapRoute.setEndMarker(map, cell)
                                    Toast.makeText(context, "Финиш — ваше местоположение", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    context = context,
                    mapRef = mapRef,
                    foodStartCell = foodStartCell,
                    onRequestFoodStart = {
                        foodStartCell = null
                        Toast.makeText(context, "Нажмите на карту — ваше местоположение", Toast.LENGTH_SHORT).show()
                    },
                    onFoodRouteBuilt = { route: Route? ->
                        if (route == null) mapRef?.let { MapRoute.clearFoodRoute(it) }
                    },
                    onShowManhattan = { vm.showManhattanClusters(context) },
                    onShowEuclidian = { vm.showEuclideanClusters(context) },
                    onPlaceAntStart = { vm.onPlaceAntStart(context) },
                    onRunAntColony = { indices -> vm.runAntColony(indices, context) },
                    antStartSet = vm.antStartSet,
                    onClearMap = { vm.clearMap() },
                    onOpenDigitScreen = { vm.showDigitScreen = true },
                    clearCounter = vm.clearCounter
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
        Box(modifier = Modifier.fillMaxSize().background(ComposeColor.Black)) {
            CampusMapView(
                onMapReady = { map ->
                    vm.mapRef = map
                    map.addOnMapClickListener { latLng ->
                        val cell = CampusGrid.latLonToCell(latLng.latitude, latLng.longitude)
                            ?: return@addOnMapClickListener true

                        when {
                            placingFoodStart -> {
                                val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
                                if (walkable == null) {
                                    Toast.makeText(context, "Нет доступных точек рядом", Toast.LENGTH_SHORT).show()
                                } else {
                                    placingFoodStart = false
                                    MapRoute.setStartMarker(map, walkable)
                                    Toast.makeText(context, "Начальная точка установлена", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else -> vm.onMapClick(latLng, map, context)
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