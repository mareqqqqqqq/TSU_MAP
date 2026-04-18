package com.example.tsuappmap.algorithm.Claster

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.maps.MapLibreMap

object ClusterDisplay {
    val CLUSTER_COLORS = listOf(
        Color.rgb(220, 50, 50),
        Color.rgb(30, 120, 210),
        Color.rgb(40, 170, 80),
        Color.rgb(230, 150, 0),
        Color.rgb(150, 60, 200),
        Color.rgb(0, 180, 180),
    )

    val CLUSTER_COLOR_NAMES = listOf(
        "Красный", "Синий", "Зелёный", "Оранжевый", "Фиолетовый", "Голубой"
    )

    const val METHOD_EUCLIDEAN = "Евклидово расстояние"
    const val METHOD_MANHATTAN = "Манхэттенское расстояние"
    const val METHOD_ASTAR = "Пешеходное расстояние (A*)"

    fun colorForCluster(clusterId: Int): Int = CLUSTER_COLORS[clusterId % CLUSTER_COLORS.size]

    fun colorNameForCluster(clusterId: Int): String =
        CLUSTER_COLOR_NAMES[clusterId % CLUSTER_COLOR_NAMES.size]

    fun createClusterIcon(context: Context, clusterId: Int): Icon {
        val color = colorForCluster(clusterId)
        val size = 72
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawCircle(size / 2f + 2f, size / 2f + 2f, size / 2f - 4f, shadowPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, fillPaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, strokePaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(
            "${clusterId + 1}",
            size / 2f,
            size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f,
            textPaint
        )

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    fun drawClusters(
        map: MapLibreMap, results: List<ClusterResult>, context: Context, method: String, k: Int
    ): List<Marker> {
        val markers = mutableListOf<Marker>()

        results.forEach { result ->
            val icon = createClusterIcon(context, result.clusterId)
            val colorName = colorNameForCluster(result.clusterId)
            val clusterNum = result.clusterId + 1

            val snippet = buildString {
                appendLine(" ${result.cafe.name}")
                appendLine("Алгоритм: $method")
                appendLine("Кластер $clusterNum — $colorName")
            }.trimEnd()

            val marker = map.addMarker(
                MarkerOptions().position(result.cafe.location).title(result.cafe.name)
                    .setSnippet(snippet).icon(icon)
            )
            markers.add(marker)
        }

        return markers
    }

    fun buildLegendItems(k: Int): List<Pair<Int, String>> {
        return (0 until k).map { id ->
            Pair(colorForCluster(id), "Кластер ${id + 1}: ${colorNameForCluster(id)}")
        }
    }
}