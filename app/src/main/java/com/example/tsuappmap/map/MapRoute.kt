package com.example.tsuappmap.map

import android.graphics.Color
import com.example.tsuappmap.algorithm.Astar.CustomObstacle
import com.example.tsuappmap.algorithm.Genetic.Route
import com.example.tsuappmap.algorithm.Genetic.makeNumberedIcon
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.Polygon
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

object MapRoute {
    private var routeLine: Polyline? = null
    private var obstacklePolygons: List<Polygon> = emptyList()
    private var drawnVisited = mutableSetOf<Pair<Int, Int>>()
    private var drawnFrontiers = mutableMapOf<Pair<Int, Int>, Polygon>()
    private var visitedPolygons = mutableListOf<Polygon>()
    private var startMarker: Marker? = null
    private var endMarker: Marker? = null
    private var animPolygon = mutableListOf<Polygon>()
    private var obstaclePolygons: List<Polygon> = emptyList()

    private const val MAX_VISITED_PER_FRAME = 300
    private const val MAX_FRONTIER_PER_FRAME = 150

    fun drawRoute(map: MapLibreMap, path: List<Pair<Int, Int>>) {
        clearRoute(map)
        val points = path.map { (row, col) ->
            val (lat, lon) = CampusGrid.cellToLatLon(row, col)
            LatLng(lat, lon)
        }
        routeLine = map.addPolyline(
            PolylineOptions().addAll(points).color(Color.rgb(0, 120, 255)).width(4f)
        )
    }

    fun clearRoute(map: MapLibreMap) {
        routeLine?.let { map.removePolyline(it) }
        routeLine = null
    }

    fun drawSearchStep(
        map: MapLibreMap,
        visited: Set<Pair<Int, Int>>,
        frontier: Set<Pair<Int, Int>>,
        current: Pair<Int, Int>
    ) {
        animPolygon.forEach { map.removePolygon(it) }
        animPolygon.clear()

        val frontierToShow = if (frontier.size <= MAX_FRONTIER_PER_FRAME) frontier
        else frontier.sortedBy { (r, c) ->
            val dr = r - current.first;
            val dc = c - current.second
            dr * dc + dc * dc
        }.take(MAX_FRONTIER_PER_FRAME).toSet()

        val visitedToShow = if (visited.size <= MAX_VISITED_PER_FRAME) visited
        else visited.sortedBy { (r, c) ->
            val dr = r - current.first;
            val dc = c - current.second
            dr * dr + dc * dc
        }.take(MAX_VISITED_PER_FRAME).toSet()

        for (cell in visitedToShow) animPolygon.add(
            drawCell(
                map,
                cell,
                Color.argb(80, 150, 150, 150)
            )
        )
        for (cell in frontierToShow) animPolygon.add(
            drawCell(
                map,
                cell,
                Color.argb(180, 255, 200, 0)
            )
        )
    }

    fun clearSearch(map: MapLibreMap) {
        animPolygon.forEach { map.removePolygon(it) }
        animPolygon.clear()
        visitedPolygons.forEach { map.removePolygon(it) }
        visitedPolygons.clear()
        drawnVisited.clear()
        drawnFrontiers.values.forEach { map.removePolygon(it) }
        drawnFrontiers.clear()
    }

    fun drawObstacles(map: MapLibreMap) {
        obstacklePolygons.forEach { map.removePolygon(it) }
        obstacklePolygons = CustomObstacle.getAll().map {
            drawCell(map, it, Color.argb(180, 220, 50, 50))
        }
    }

    fun setStartMarker(map: MapLibreMap, cell: Pair<Int, Int>) {
        startMarker?.let { map.removeMarker(it) }
        val (lat, lon) = CampusGrid.cellToLatLon(cell.first, cell.second)
        startMarker = map.addMarker(MarkerOptions().position(LatLng(lat, lon)).title("Старт"))
    }

    fun setEndMarker(map: MapLibreMap, cell: Pair<Int, Int>) {
        endMarker?.let { map.removeMarker(it) }
        val (lat, lon) = CampusGrid.cellToLatLon(cell.first, cell.second)
        endMarker = map.addMarker(MarkerOptions().position(LatLng(lat, lon)).title("Конец"))
    }

    fun clearMarkers(map: MapLibreMap) {
        startMarker?.let { map.removeMarker(it) }
        endMarker?.let { map.removeMarker(it) }
        startMarker = null; endMarker = null
    }

    private var foodRouteLines: List<Polyline> = emptyList()

    fun drawFoodRoute(map: MapLibreMap, route: Route, isFinal: Boolean) {
        foodRouteLines.forEach { map.removePolyline(it) }
        val lines = mutableListOf<Polyline>()
        val color = if (isFinal) Color.rgb(0, 200, 80) else Color.rgb(255, 140, 0)
        for (segment in route.pathSegments) {
            if (segment.size < 2) continue
            val pts = segment.map { (r, c) ->
                val (lat, lon) = CampusGrid.cellToLatLon(r, c)
                LatLng(lat, lon)
            }
            lines += map.addPolyline(PolylineOptions().addAll(pts).color(color).width(5f))
        }
        foodRouteLines = lines
    }

    fun clearFoodRoute(map: MapLibreMap) {
        foodRouteLines.forEach { map.removePolyline(it) }
        foodRouteLines = emptyList()
        clearFoodEstablishmentMarkers(map)
    }

    private val foodEstMarkers = mutableListOf<Marker>()

    private val markerColors = listOf(
        android.graphics.Color.rgb(0, 160, 80),
        android.graphics.Color.rgb(0, 100, 200),
        android.graphics.Color.rgb(220, 100, 0),
        android.graphics.Color.rgb(150, 0, 200),
        android.graphics.Color.rgb(200, 0, 80)
    )

    fun drawFoodEstablishmentMarkers(
        map: MapLibreMap,
        route: Route,
        context: android.content.Context
    ) {
        foodEstMarkers.forEach { map.removeMarker(it) }
        foodEstMarkers.clear()

        route.establishments.forEachIndexed { index, est ->
            val color = markerColors[index % markerColors.size]
            val icon = makeNumberedIcon(context, "${index + 1}", color)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(est.location)
                    .title("${index + 1}. ${est.name}")
                    .snippet(
                        est.menu.take(3).joinToString(", ") { it.name }
                            .let { if (est.menu.size > 3) "$it..." else it }
                    )
                    .icon(icon)
            )
            foodEstMarkers.add(marker)
        }
    }

    fun clearFoodEstablishmentMarkers(map: MapLibreMap) {
        foodEstMarkers.forEach { map.removeMarker(it) }
        foodEstMarkers.clear()
    }

    private fun drawCell(map: MapLibreMap, cell: Pair<Int, Int>, color: Int): Polygon {
        val (row, col) = cell
        val (centerLat, centerLon) = CampusGrid.cellToLatLon(row, col)
        val halfLat = CampusGrid.cellSize / 2.0 / 111320.0
        val halfLon = CampusGrid.cellSize / 2.0 / (111320.0 * Math.cos(Math.toRadians(centerLat)))
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
}