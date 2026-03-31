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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
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
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    var selectedTab by remember {mutableStateOf(0)}



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
                    Button(
                        onClick = {selectedTab = 1},
                        modifier = Modifier
                            .width(110.dp)
                            .height(65.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(red = 0, green = 114, blue = 188),
                            contentColor = ComposeColor.White
                        )
                    ) {
                        Text("Кнопка 1")
                    }

                    Button(
                        onClick = {selectedTab = 2},
                        modifier = Modifier
                            .width(110.dp)
                            .height(65.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(red = 0, green = 114, blue = 188),
                            contentColor = ComposeColor.White
                        )

                    ) {
                        Text("Кнопка 2")
                    }

                    Button(
                        onClick = {selectedTab = 3},
                        modifier = Modifier
                            .width(110.dp)
                            .height(65.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(red = 0, green = 114, blue = 188),
                            contentColor = ComposeColor.White
                        )
                    ) {
                        Text("Кнопка 3")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = {selectedTab = 4},
                        modifier = Modifier
                            .width(110.dp)
                            .height(65.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(red = 0, green = 114, blue = 188),
                            contentColor = ComposeColor.White
                        )
                    ) {
                        Text("Кнопка 4")
                    }

                    Button(
                        onClick = {selectedTab = 5},
                        modifier = Modifier
                            .width(110.dp)
                            .height(65.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(red = 0, green = 114, blue = 188),
                            contentColor = ComposeColor.White
                        )
                    ) {
                        Text("Кнопка 5")
                    }

                    Button(
                        onClick = {selectedTab = 6},
                        modifier = Modifier
                            .width(110.dp)
                            .height(65.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComposeColor(red = 0, green = 114, blue = 188),
                            contentColor = ComposeColor.White
                        )
                    ) {
                        Text("Кнопка 6")
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp).padding(start =20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
                )
                {
                    when (selectedTab)
                    {
                        0,1 -> Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp,Alignment.CenterVertically)

                        )
                        {
                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .width(360.dp)
                                    .height(70.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ComposeColor(red = 0, green = 0, blue = 188),
                                    contentColor = ComposeColor.White
                                )
                            ) {
                                Text("Кнопка 6")
                            }

                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .width(360.dp)
                                    .height(70.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ComposeColor(red = 0, green = 0, blue = 188),
                                    contentColor = ComposeColor.White
                                )
                            ) {
                                Text("Кнопка 10")
                            }

                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .width(360.dp)
                                    .height(70.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ComposeColor(red = 0, green = 0, blue = 188),
                                    contentColor = ComposeColor.White
                                )
                            ) {
                                Text("Кнопка 12")
                            }
                        }

                        2 -> Box(
                            modifier = Modifier.fillMaxSize().background(ComposeColor(red = 100, green = 100, blue = 100))

                        )
                        {
                            Text("Контент кнопки 2")
                        }
                        3 -> Box(
                            modifier = Modifier.fillMaxSize().background(ComposeColor(red = 100, green = 100, blue = 100))

                        )
                        {
                            Text("Контент кнопки 3")
                        }
                        4 -> Box(
                            modifier = Modifier.fillMaxSize().background(ComposeColor(red = 100, green = 100, blue = 100))

                        )
                        {
                            Text("Контент кнопки 4")
                        }
                        5 -> Box(
                            modifier = Modifier.fillMaxSize().background(ComposeColor(red = 100, green = 100, blue = 100))

                        )
                        {
                            Text("Контент кнопки 5")
                        }
                        6 -> Box(
                            modifier = Modifier.fillMaxSize().background(ComposeColor(red = 100, green = 100, blue = 100))

                        )
                        {
                            Text("Контент кнопки 6")
                        }
                    }
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


            AndroidView(factory = {
                mapView.apply {
                    getMapAsync { map ->
                        map.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                            val centerTsu = LatLng(56.4695, 84.9475)

                            val baseRadius = 700.0
                            val southExtra = 300.0
                            val northExtra = 0.0
                            val westExtra = 150.0
                            val eastExtra = -450.0


                            val cosLat = Math.cos(Math.toRadians(centerTsu.latitude))

                            val latMin = centerTsu.latitude - ((baseRadius + southExtra) / 111320.0)
                            val latMax = centerTsu.latitude + ((baseRadius + northExtra) / 111320.0)
                            val lngMin =
                                centerTsu.longitude - ((baseRadius + westExtra) / (111320.0 * cosLat))
                            val lngMax =
                                centerTsu.longitude + ((baseRadius + eastExtra) / (111320.0 * cosLat))

                            val tsuBounds = LatLngBounds.Builder()
                                .include(LatLng(latMin, lngMin))
                                .include(LatLng(latMax, lngMax))
                                .build()


                            map.setLatLngBoundsForCameraTarget(tsuBounds)
                            map.setMinZoomPreference(14.5)
                            map.uiSettings.isCompassEnabled = false
                            map.uiSettings.isLogoEnabled = false
                            map.uiSettings.isAttributionEnabled = false

                            map.cameraPosition = CameraPosition.Builder()
                                .target(centerTsu)
                                .zoom(16.0)
                                .build()


                            drawFinalGrid(map, latMin, latMax, lngMin, lngMax, centerTsu.latitude)

                            var isObstrackleMode = false
                            var startPoint: Pair<Int, Int>? = null
                            var animationJob: Job? = null

                            map.addOnMapClickListener { latLng ->
                                val cell =
                                    CampusGrid.latLonToCell(latLng.latitude, latLng.longitude)
                                        ?: return@addOnMapClickListener true

                                if (isObstrackleMode) {
                                    CustomObstracle.toggle(cell.first, cell.second)
                                    MapRoute.drawObstracles(map)
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
                                                MapRoute.drawSearchStep(
                                                    map,
                                                    step.visited,
                                                    step.frontier,
                                                    step.current
                                                )
                                                delay(16L)
                                            }

                                            val lastStep = steps.last()
                                            if (lastStep.path != null) {
                                                MapRoute.clearSearch(map)
                                                MapRoute.drawRoute(map, lastStep.path)
                                            } else {
                                                MapRoute.clearSearch(map)
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
                        }
                    }
                }
            }, modifier = Modifier.fillMaxSize())

            Image(
                painter = painterResource(id = R.drawable.hits),
                contentDescription = null,
                modifier = Modifier.size(50.dp).align(Alignment.TopStart).padding(8.dp)
            )
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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