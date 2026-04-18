package com.example.tsuappmap.algorithm.Claster

import com.example.tsuappmap.algorithm.Astar.AStar
import com.example.tsuappmap.map.CampusGrid
import org.maplibre.android.geometry.LatLng

class KMeansAstar {
    private fun astarDistance(a: LatLng, b: LatLng): Double {
        val cellA =
            CampusGrid.latLonToCell(a.latitude, a.longitude) ?: return euclideanFallback(a, b)
        val cellB =
            CampusGrid.latLonToCell(b.latitude, b.longitude) ?: return euclideanFallback(a, b)

        val wA =
            CampusGrid.nearestWalkable(cellA.first, cellA.second) ?: return euclideanFallback(a, b)
        val wB =
            CampusGrid.nearestWalkable(cellB.first, cellB.second) ?: return euclideanFallback(a, b)

        val result = AStar.findPathOnly(wA.first, wA.second, wB.first, wB.second)

        return if (result != null) {
            result.length * CampusGrid.cellSize
        } else {
            euclideanFallback(a, b) * 2.0
        }
    }

    private fun euclideanFallback(a: LatLng, b: LatLng): Double {
        val latDiff = (a.latitude - b.latitude) * 111320.0
        val lngDiff = (a.longitude - b.longitude) * 111320.0
        return kotlin.math.sqrt(latDiff * latDiff + lngDiff * lngDiff)
    }

    fun initCenters(cafes: List<Cafe>, k: Int, distMatrix: Array<DoubleArray>): List<Int> {
        if (k >= cafes.size) return cafes.indices.toList()

        val centerIndices = mutableListOf<Int>()
        centerIndices.add((cafes.indices).random())

        for (step in 2..k) {
            val distances = cafes.indices.map { i ->
                val minDist = centerIndices.minOf { ci -> distMatrix[i][ci] }
                minDist * minDist
            }

            val total = distances.sum()
            if (total == 0.0) break

            val rnd = Math.random() * total
            var cumulative = 0.0
            var selected = 0
            for (i in distances.indices) {
                cumulative += distances[i]
                if (cumulative >= rnd) {
                    selected = i; break
                }
            }
            centerIndices.add(selected)
        }

        return centerIndices
    }

    private fun assignToClusters(
        cafes: List<Cafe>, centerIndices: List<Int>, distMatrix: Array<DoubleArray>
    ): List<ClusterResult> {
        return cafes.indices.map { i ->
            val closestCenter = centerIndices.indices.minBy { ci ->
                distMatrix[i][centerIndices[ci]]
            }
            ClusterResult(cafes[i], closestCenter)
        }
    }

    private fun recalculateCenters(
        assignments: List<ClusterResult>, cafes: List<Cafe>, k: Int, distMatrix: Array<DoubleArray>
    ): List<Int> {
        val clusters = assignments.groupBy { it.clusterId }
        return (0 until k).map { clusterId ->
            val members = clusters[clusterId]?.map { r -> cafes.indexOf(r.cafe) }
                ?: return@map (cafes.indices).random()

            if (members.size == 1) return@map members[0]

            members.minBy { i -> members.sumOf { j -> distMatrix[i][j] } }
        }
    }

    fun cluster(
        cafes: List<Cafe>, k: Int, onProgress: ((Float) -> Unit)? = null
    ): List<ClusterResult> {
        if (cafes.isEmpty() || k <= 0) return emptyList()
        if (k >= cafes.size) return cafes.mapIndexed { i, c -> ClusterResult(c, i) }

        val n = cafes.size

        val distMatrix = Array(n) { DoubleArray(n) }
        var computed = 0 // сколько пар уже посчитали
        val totalPairs = n * (n - 1) / 2 // сколько всего нужно посчитать

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val d = astarDistance(cafes[i].location, cafes[j].location)
                distMatrix[i][j] = d
                distMatrix[j][i] = d
                computed++
                onProgress?.invoke(computed.toFloat() / totalPairs)
            }
        }

        var centerIndices = initCenters(cafes, k, distMatrix)
        var assignments = assignToClusters(cafes, centerIndices, distMatrix)
        var prevCenterIndices: List<Int>

        var iterations = 0
        do {
            prevCenterIndices = centerIndices
            centerIndices = recalculateCenters(assignments, cafes, k, distMatrix)
            assignments = assignToClusters(cafes, centerIndices, distMatrix)
            iterations++
        } while (centerIndices != prevCenterIndices && iterations < 100)

        return assignments
    }
}