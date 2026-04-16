package com.example.tsuappmap.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.Polyline
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import org.maplibre.android.annotations.Icon

import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

object MapRouteForAnt {
    private var antRouteLines = mutableListOf<Polyline>()
    private var antMarkers = mutableListOf<Marker>()
    private var antStartMarker: Marker? = null

    private val segmentColors = listOf(
        Color.rgb(0, 120, 255),
        Color.rgb(220, 50, 50),
        Color.rgb(0, 180, 80),
        Color.rgb(220, 150, 0),
        Color.rgb(150, 0, 220),
        Color.rgb(0, 200, 200),
        Color.rgb(220, 0, 150),
        Color.rgb(100, 180, 0),

        Color.rgb(255, 80, 0),
        Color.rgb(0, 100, 200),
        Color.rgb(180, 100, 0),
        Color.rgb(80, 200, 120),
    )

    fun clearAntRoute(map: MapLibreMap)
    {
        antRouteLines.forEach { map.removePolyline(it) }
        antRouteLines.clear()
        antMarkers.forEach { map.removeMarker(it) }
        antMarkers.clear()
    }

    fun setStartMarker(context: Context, map: MapLibreMap, cell: Pair<Int, Int>) {
        antStartMarker?.let { map.removeMarker(it) }
        val (lat, lon) = CampusGrid.cellToLatLon(cell.first, cell.second)
        val icon = makeNumberedIcon(context, "Старт", Color.rgb(0, 160, 80))
        antStartMarker = map.addMarker(
            MarkerOptions().position(LatLng(lat, lon)).icon(icon)
        )
    }

    fun drawAntRoude(
        context: Context,
        map: MapLibreMap,
        segments: List<List<Pair<Int, Int>>>,
        cells: List<Pair<Int, Int>>
    ) {
        clearAntRoute(map)

        segments.forEachIndexed { index, segment ->
            if (segment.isEmpty()) return@forEachIndexed

            val color = segmentColors[index % segmentColors.size]
            val points = segment.map {(row,col) ->
                val (lat,lon) = CampusGrid.cellToLatLon(row,col)
                LatLng(lat,lon)
            }

            val line = map.addPolyline(
                PolylineOptions().addAll(points).color(color).width(5f)
            )

            antRouteLines.add(line)
        }

        cells.forEachIndexed {index, cell ->
            val (lat,lon) = CampusGrid.cellToLatLon(cell.first,cell.second)
            val label = if (index == 0) "ф " else "$index"
            val bgColor = if (index == 0) Color.rgb(0,160,80) else segmentColors[(index - 1) % segmentColors.size]
            val icon = makeNumberedIcon(context,label,bgColor)
            val marker = map.addMarker(
                MarkerOptions().position(LatLng(lat,lon)).icon(icon)
            )

            antMarkers.add(marker)
        }
    }

    fun makeNumberedIcon(context: Context, label: String, bgColor: Int): Icon {
        val size = 72
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = if (label.length > 1) 24f else 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, borderPaint)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, circlePaint)
        canvas.drawText(label, size / 2f, textY, textPaint)

        val iconFactory = IconFactory.getInstance(context)

        return iconFactory.fromBitmap(bitmap)

    }


}