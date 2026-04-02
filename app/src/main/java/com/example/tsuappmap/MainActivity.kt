package com.example.tsuappmap

import android.graphics.Color
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import kotlinx.coroutines.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.tsuappmap.algorithm.Astar.AStar
import com.example.tsuappmap.algorithm.Astar.CustomObstracle
import com.example.tsuappmap.map.CampusGrid
import com.example.tsuappmap.map.CampusMapView
import com.example.tsuappmap.map.MapRoute
import com.example.tsuappmap.map.drawFinalGrid

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

    var selectedTab by remember {mutableStateOf(1)}



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

                if(selectedTab == 1)
                {
                    TabContentAStar(selectedTab)
                }
                else if(selectedTab == 2)
                {
                    TabContentClaster(selectedTab)
                }
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
                    var isObstrackleMode = false
                    var startPoint: Pair<Int, Int>? = null
                    var animationJob: Job? = null

                    map.addOnMapClickListener { latLng ->
                        val cell = CampusGrid.latLonToCell(latLng.latitude, latLng.longitude)
                            ?: return@addOnMapClickListener true

                        if (isObstrackleMode) {
                            CustomObstracle.toggle(cell.first, cell.second)
                        } else {
                            if (!CustomObstracle.isWalkable(cell.first, cell.second)) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Это препятствие!",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@addOnMapClickListener true
                            }

                            if (startPoint == null) {
                                startPoint = cell
                                android.widget.Toast.makeText(
                                    context,
                                    "Старт выбран, выбери конечную точку",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val (sRow, sCol) = startPoint!!
                                val (eRow, eCol) = cell
                                startPoint = null

                                animationJob?.cancel()
                                MapRoute.clearSearch(map)
                                MapRoute.clearRoute(map)

                                animationJob = CoroutineScope(Dispatchers.Main).launch {
                                    val steps = withContext(Dispatchers.Default) {
                                        AStar.findPathWithSteps(sRow, sCol, eRow, eCol)
                                    }
                                    for (step in steps) {
                                        if (step.visited != null && step.frontier != null) {
                                            MapRoute.drawSearchStep(
                                                map,
                                                step.visited,
                                                step.frontier,
                                                step.current
                                            )
                                        }
                                        delay(50L)
                                    }
                                    val lastStep = steps.last()
                                    MapRoute.clearSearch(map)
                                    if (lastStep.path != null) {
                                        MapRoute.drawRoute(map, lastStep.path)
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "Маршрут не найден",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
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

fun drawFinalGrid(map: MapLibreMap, latMin: Double, latMax: Double, lngMin: Double, lngMax: Double, centerLat: Double) {
    val stepMeters = 8.0
    val latStep = stepMeters / 111320.0
    val lngStep = stepMeters / (111320.0 * Math.cos(Math.toRadians(centerLat)))

    val lineColor = Color.argb(100, 0, 0, 0)

    val lineWidth = 0.5f

    val startLat = Math.floor(latMin / latStep) * latStep
    val startLng = Math.floor(lngMin / lngStep) * lngStep


    var currentLat = startLat
    while (currentLat <= latMax) {
        map.addPolyline(PolylineOptions()
            .add(LatLng(currentLat, lngMin), LatLng(currentLat, lngMax))
            .color(lineColor).width(lineWidth))
        currentLat += latStep
    }


    var currentLng = startLng
    while (currentLng <= lngMax) {
        map.addPolyline(PolylineOptions()
            .add(LatLng(latMin, currentLng), LatLng(latMax, currentLng))
            .color(lineColor).width(lineWidth))
        currentLng += lngStep
    }
}