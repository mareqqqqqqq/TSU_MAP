package com.example.tsuappmap.map

import android.graphics.Color
import com.example.tsuappmap.map.CampusGrid
import com.example.tsuappmap.algorithm.Astar.CustomObstacle
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

    private var animPolygon = mutableListOf<Polygon>()

    private var obstaclePolygons: List<Polygon> = emptyList()

    private const val MAX_VISITED_PER_FRAME = 300
    private const val MAX_FRONTIER_PER_FRAME = 150


    fun drawRoute (map: MapLibreMap, path: List<Pair<Int, Int>>) {
        clearRoute(map)
        val points = path.map { (row, col) ->
            val (lat, lon) = CampusGrid.cellToLatLon(row, col)
            LatLng(lat, lon)
        }
        routeLine = map.addPolyline(
            PolylineOptions().addAll(points).color(
            Color.rgb(0, 120, 255)).width(4f)
        )
    }

    fun drawSearchStep(map: MapLibreMap,
                       visited: Set<Pair<Int, Int>>,
                       frontier: Set<Pair<Int, Int>>,
                       current: Pair<Int, Int>) {
        animPolygon.forEach {map.removePolygon(it)}
        animPolygon.clear()

        val frontierToShow = if (frontier.size <= MAX_FRONTIER_PER_FRAME) {
            frontier
        }
        else {
            frontier.sortedBy { (r, c) ->
                val dr = r - current.first
                val dc = c - current.second
                dr * dc + dc * dc
            }.take(MAX_FRONTIER_PER_FRAME).toSet()
        }

        val visitedToShow = if (visited.size <= MAX_VISITED_PER_FRAME) {
            visited
        }
        else {
            visited.sortedBy { (r, c) ->
                val dr = r - current.first
                val dc = c - current.second
                dr * dr + dc * dc
            }.take(MAX_VISITED_PER_FRAME).toSet()
        }

        for (cell in visitedToShow) {
            animPolygon.add ( drawCell (map, cell, Color.argb(80, 150, 150, 150)))
        }

        for (cell in frontierToShow) {
            animPolygon.add(drawCell (map, cell, Color.argb(180, 255, 200, 0)))
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

    private fun drawCell (
        map: MapLibreMap,
        cell: Pair<Int, Int>,
        color: Int
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
            PolygonOptions()
                .addAll(corners)
                .fillColor(color)
                .strokeColor(Color.TRANSPARENT)
        )
    }
}