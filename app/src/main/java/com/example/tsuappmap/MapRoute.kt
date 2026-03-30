package com.example.tsuappmap

import android.graphics.Color
import org.maplibre.android.annotations.Polygon
import org.maplibre.android.annotations.PolygonOptions
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

object MapRoute {
    private var routeLine: Polyline? = null
    private var visitedPolygons: List<Polygon> = emptyList()
    private var frontierPolygons: List<Polygon> = emptyList()
    private var obstacklePolygons: List<Polygon> = emptyList()

    fun drawRoute (map: MapLibreMap, path: List<Pair<Int, Int>>) {
        clearRoute(map)
        val points = path.map { (row, col) ->
            val (lat, lon) = CampusGrid.cellToLatLon(row, col)
            LatLng(lat, lon)
        }
        routeLine = map.addPolyline(PolylineOptions().addAll(points).color(
            Color.rgb(0, 120, 255)).width(4f)
        )
    }

    fun drawSearchStep(map: MapLibreMap,
                       visited: Set<Pair<Int, Int>>,
                       frontier: Set<Pair<Int, Int>>,
                       current: Pair<Int, Int>) {
        visitedPolygons.forEach { map.removePolygon(it) }
        frontierPolygons.forEach { map.removePolygon(it) }

        visitedPolygons = visited.map { drawCell (map, it, Color.argb(80, 150, 150, 150))}
        frontierPolygons = frontier.map { drawCell (map, it, Color.argb(180, 255, 200, 0))}
        drawCell(map, current, Color.argb(220, 255, 120, 0))
    }

    fun drawObstracles(map: MapLibreMap) {
        obstacklePolygons.forEach { map.removePolygon(it) }
        obstacklePolygons = CustomObstracle.getAll().map {
            drawCell(map, it, Color.argb(180, 220, 50, 50))
        }
    }

    fun clearRoute(map: MapLibreMap) {
        routeLine?.let { map.removePolyline(it) }
        routeLine = null
    }

    fun clearSearch(map: MapLibreMap) {
        visitedPolygons.forEach { map.removePolygon(it) }
        frontierPolygons.forEach { map.removePolygon(it) }
        visitedPolygons = emptyList()
        frontierPolygons = emptyList()
    }

    //6 22 2
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