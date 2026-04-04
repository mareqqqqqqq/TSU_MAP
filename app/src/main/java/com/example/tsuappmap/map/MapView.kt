package com.example.tsuappmap.map

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.annotations.PolylineOptions

@Composable
fun CampusMapView(
    onMapReady: (MapLibreMap) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }

    AndroidView(
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    map.setStyle("https://tiles.openfreemap.org/styles/liberty") {
                        val centerTsu = LatLng(56.4695, 84.9475)

                        val baseRadius = 700.0
                        val southExtra = 500.0
                        val northExtra = 100.0
                        val westExtra = 306.0
                        val eastExtra = -297.33

                        val cosLat = Math.cos(Math.toRadians(centerTsu.latitude))

                        val latMin = centerTsu.latitude - ((baseRadius + southExtra) / 111320.0)
                        val latMax = centerTsu.latitude + ((baseRadius + northExtra) / 111320.0)
                        val lngMin = centerTsu.longitude - ((baseRadius + westExtra) / (111320.0 * cosLat))
                        val lngMax = centerTsu.longitude + ((baseRadius + eastExtra) / (111320.0 * cosLat))

                        val tsuBounds = LatLngBounds.Builder()
                            .include(LatLng(latMin, lngMin))
                            .include(LatLng(latMax, lngMax))
                            .build()

                        map.setLatLngBoundsForCameraTarget(tsuBounds)
                        map.setMinZoomPreference(13.5)
                        map.uiSettings.isCompassEnabled = false
                        map.uiSettings.isLogoEnabled = false
                        map.uiSettings.isAttributionEnabled = false

                        map.cameraPosition = CameraPosition.Builder()
                            .target(centerTsu)
                            .zoom(16.0)
                            .build()

                        drawFinalGrid(map, latMin, latMax, lngMin, lngMax, centerTsu.latitude)

                        onMapReady(map)
                    }
                }
            }
        },
        modifier = modifier
    )

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


fun drawFinalGrid(map: MapLibreMap, latMin: Double, latMax: Double, lngMin: Double, lngMax: Double, centerLat: Double)
{
    val stepMeters = 8.0
    val latStep = stepMeters / 111320.0
    val lngStep = stepMeters / (111320.0 * Math.cos(Math.toRadians(centerLat)))

    val lineColor = Color.argb(100, 0, 0, 0)
    val lineWidth = 0.5f

    val startLat = Math.floor(latMin / latStep) * latStep
    val startLng = Math.floor(lngMin / lngStep) * lngStep

    var currentLat = startLat
    while (currentLat <= latMax) {
        map.addPolyline(
            PolylineOptions()
                .add(LatLng(currentLat, lngMin), LatLng(currentLat, lngMax))
                .color(lineColor)
                .width(lineWidth)
        )
        currentLat += latStep
    }

    var currentLng = startLng + lngStep
    while (currentLng <= lngMax) {
        map.addPolyline(
            PolylineOptions()
                .add(LatLng(latMin, currentLng), LatLng(latMax, currentLng))
                .color(lineColor)
                .width(lineWidth)
        )
        currentLng += lngStep
    }
}