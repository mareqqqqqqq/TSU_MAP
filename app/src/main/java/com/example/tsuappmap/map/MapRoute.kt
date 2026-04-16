package com.example.tsuappmap.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.tsuappmap.algorithm.Astar.CustomObstacle
import com.example.tsuappmap.algorithm.Claster.ClusterResult
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Polygon
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions

object MapRoute {
    private var routeLine: Polyline? = null
    private var obstacklePolygons: List<Polygon> = emptyList()
    private var drawnVisited = mutableSetOf<Pair<Int, Int>>()
    private var drawnFrontiers = mutableMapOf<Pair<Int, Int>, Polygon>()
    private var visitedPolygons = mutableListOf<Polygon>()
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null

    private var clusterMarkers = mutableListOf<Marker>()

    private var attractionRoute: Polyline? = null

    private var attractionMarkers = mutableListOf<Marker>()

    fun drawRoute(map: MapLibreMap, path: List<Pair<Int, Int>>) {
        clearRoute(map)
        val points = path.map { (row, col) ->
            val (lat, lon) = CampusGrid.cellToLatLon(row, col)
            LatLng(lat, lon)
        }
        routeLine = map.addPolyline(
            PolylineOptions().addAll(points).color(
                Color.rgb(0, 120, 255)
            ).width(4f)
        )
    }

    fun drawSearchStep(
        map: MapLibreMap,
        visited: Set<Pair<Int, Int>>,
        frontier: Set<Pair<Int, Int>>,
        current: Pair<Int, Int>
    ) {
        val newVisited = visited - drawnVisited

        for (cell in newVisited) {
            drawnFrontiers.remove(cell)?.let { map.removePolygon(it) }
            visitedPolygons.add(drawCell(map, cell, Color.argb(80, 150, 150, 150)))
            drawnVisited.add(cell)
        }

        val newFrontier = frontier - drawnFrontiers.keys - drawnVisited
        for (cell in newFrontier) {
            drawnFrontiers[cell] = drawCell(map, cell, Color.argb(180, 255, 200, 0))
        }
    }

    fun drawObstacles(map: MapLibreMap) {
        obstacklePolygons.forEach { map.removePolygon(it) }
        obstacklePolygons = CustomObstacle.getAll().map {
            drawCell(map, it, Color.argb(180, 220, 50, 50))
        }
    }

    fun clearRoute(map: MapLibreMap) {
        routeLine?.let { map.removePolyline(it) }
        routeLine = null
    }

    fun clearSearch(map: MapLibreMap) {
        visitedPolygons.forEach { map.removePolygon(it) }
        drawnFrontiers.values.forEach { map.removePolygon(it) }
        visitedPolygons.clear()
        drawnVisited.clear()
        drawnFrontiers.clear()
    }

    fun setStartMarker(map: MapLibreMap, cell: Pair<Int, Int>) {
        startMarker?.let { map.removeMarker(it) }
        val (lat, lon) = CampusGrid.cellToLatLon(cell.first, cell.second)
        startMarker = map.addMarker(
            MarkerOptions().position(LatLng(lat, lon)).title("Старт")
        )
    }

    fun setEndMarker(map: MapLibreMap, cell: Pair<Int, Int>) {
        endMarker?.let { map.removeMarker(it) }
        val (lat, lon) = CampusGrid.cellToLatLon(cell.first, cell.second)
        endMarker = map.addMarker(
            MarkerOptions().position(LatLng(lat, lon)).title("Конец")
        )
    }

    fun clearMarkers(map: MapLibreMap) {
        startMarker?.let { map.removeMarker(it) }
        endMarker?.let { map.removeMarker(it) }
        startMarker = null
        endMarker = null
    }

    private fun drawCell(
        map: MapLibreMap, cell: Pair<Int, Int>, color: Int
    ): Polygon {
        val (row, col) = cell
        val (centerLat, centerLon) = CampusGrid.cellToLatLon(row, col)

        val halfLat = (CampusGrid.cellSize / 2.0 / 111320.0)
        val halfLon = (CampusGrid.cellSize / 2.0) / (111320.0 * Math.cos(Math.toRadians(centerLat)))

        val corners = listOf(
            LatLng(centerLat - halfLat, centerLon - halfLon),
            LatLng(centerLat - halfLat, centerLon + halfLon),
            LatLng(centerLat + halfLat, centerLon + halfLon),
            LatLng(centerLat + halfLat, centerLon - halfLon)
        )

        return map.addPolygon(
            PolygonOptions().addAll(corners).fillColor(color).strokeColor(Color.TRANSPARENT)
        )
    }

    private fun createColoredCircle(context: Context, color: Int): Icon {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.argb(60, 0, 0, 0)
        canvas.drawCircle(size / 2f + 2f, size / 2f + 2f, size / 2f - 4f, paint)
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)
        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    private val clusterColors = listOf(
        Color.argb(220, 255, 80, 80),
        Color.argb(220, 80, 160, 255),
        Color.argb(220, 80, 220, 100),
        Color.argb(220, 255, 200, 0),
        Color.argb(220, 220, 80, 255)
    )


    fun clearClusters(map: MapLibreMap) {
        clusterMarkers.forEach { map.removeMarker(it) }
        clusterMarkers.clear()
    }

    fun drawClusters(map: MapLibreMap, results: List<ClusterResult>, context: Context) {
        clearClusters(map)
        for (result in results) {
            val color = clusterColors[result.clusterId % clusterColors.size]
            val icon = createColoredCircle(context, color)

            clusterMarkers.add(
                map.addMarker(
                    MarkerOptions().position(result.cafe.location).title(result.cafe.name)
                        .setSnippet("Кластер ${result.clusterId + 1}").icon(icon)
                )
            )
        }
    }

    fun clearAttractionRoute(map: MapLibreMap) {
        attractionRoute?.let { map.removePolyline(it) }
        attractionRoute = null
        attractionMarkers.forEach { map.removeMarker(it) }
        attractionMarkers.clear()
    }
}