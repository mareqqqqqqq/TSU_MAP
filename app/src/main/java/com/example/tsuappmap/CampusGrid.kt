package com.example.tsuappmap

import android.content.Context
import androidx.compose.ui.graphics.vector.Path

object CampusGrid {
    var rows: Int = 0
        private set
    var cols: Int = 0
        private set
    var xOrigin: Double = 0.0
        private set
    var yOrigin: Double = 0.0
        private set
    val cellSize: Double = 8.0

    private lateinit var data: Array<BooleanArray>

    val isLoaded: Boolean get() = ::data.isInitialized

    fun load(context: Context) {
        val points = mutableListOf<Triple<Double, Double, Int>>()
        val uniqueX = sortedSetOf<Double>()
        val uniqueY = sortedSetOf<Double>()

        context.assets.open("grid.csv").bufferedReader().use { reader -> reader.forEachLine { line ->
            val parts = line.trim().split(",")
            if (parts.size < 3) return@forEachLine
            val x = parts[0].toDoubleOrNull() ?: return@forEachLine
            val y = parts[1].toDoubleOrNull() ?: return@forEachLine
            val w = parts[2].trim().toIntOrNull() ?: return@forEachLine
            points.add(Triple(x, y, w))
            uniqueX.add(x)
            uniqueY.add(y)
        } }

        val xToCol = uniqueX.withIndex().associate { (i, x) -> x to i }
        val yToRow = uniqueY.withIndex().associate { (i, y) -> y to i }

        rows = uniqueY.size
        cols = uniqueX.size
        xOrigin = uniqueX.first()
        yOrigin = uniqueY.first()

        data = Array(rows) { BooleanArray(cols) }
        for ((x, y, walkable) in points) {
            val col = xToCol[x] ?: continue
            val row = yToRow[y] ?: continue
            data[row][col] = walkable == 1
        }
    }

    fun isWalkable(row: Int, col: Int): Boolean {
        if (!isLoaded) return false
        if (row !in 0 until rows || col !in 0 until cols) return false
        return data[row][col]
    }

    fun latLonToCell(lat: Double, lon: Double): Pair<Int, Int>? {
        val xMerc = Math.toRadians(lon) * 6378137.0
        val yMerc = Math.log(Math.tan(Math.PI / 4.0 + Math.toRadians(lat) / 2.0)) * 6378137.0
        val col = ((xMerc - xOrigin) / cellSize).toInt()
        val row = ((yMerc - yOrigin) / cellSize).toInt()
        return if (row in 0 until rows && col in 0 until cols) Pair(row, col) else null
    }

    fun cellToLatLon(row: Int, col: Int): Pair<Double, Double> {
        val xMerc = xOrigin + col * cellSize + cellSize / 2.0
        val yMerc = yOrigin + row * cellSize + cellSize / 2.0
        val lon = Math.toDegrees(xMerc / 6378137.0)
        val lat = Math.toDegrees(2.0 * Math.atan(Math.exp(yMerc / 6378137.0)) - Math.PI / 2.0)
        return Pair(lat, lon)
    }

    fun nearestWalkable(row: Int, col: Int): Pair<Int, Int>? {
        if (isWalkable(row, col)) return Pair(row, col)
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                if (isWalkable(row + dr, col + dc)) return Pair(row + dr, col + dc)
            }
        }
        return null
    }
}