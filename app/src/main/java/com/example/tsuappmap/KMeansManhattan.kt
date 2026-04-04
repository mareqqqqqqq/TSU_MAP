package com.example.tsuappmap
import org.maplibre.android.geometry.LatLng

class KMeansManhattan {
    private fun manhattanDistance(a: LatLng, b: LatLng): Double {
        val latDiff = Math.abs(a.latitude - b.latitude) * 111320.0
        val lngDiff = Math.abs(a.longitude - b.longitude) * 111320.0

        return latDiff + lngDiff
    }

    fun initCenters(cafes: List<Cafe>, k: Int): List<LatLng> {
        if (k >= cafes.size) {
            return cafes.map { it.location }
        }

        val centers = mutableListOf<LatLng>()
        centers.add(cafes.random().location)

        for (centerIndex in 2..k) {
            val distances = cafes.map { cafe ->
                val minDistance = centers.minOf {
                    center -> manhattanDistance(cafe.location, center)
                }

                minDistance * minDistance
            }

            val totalDistance = distances.sum()

            if (totalDistance == 0.0) {
                break
            }


            val random = Math.random() * totalDistance

            var cumulative = 0.0
            var selectedIndex = 0

            for (i in distances.indices) {
                cumulative += distances[i]
                if (cumulative > random) {
                    selectedIndex = i
                    break
                }
            }

            val newCenter = cafes[selectedIndex].location
            centers.add(newCenter)

        }

        return centers
    }

    fun assignToClusters(cafes: List<Cafe>, centers: List<LatLng>): List<ClusterResult> {
        return cafes.map { cafe ->
            val closestIndex = centers.indices.minBy { idx ->
                manhattanDistance(cafe.location, centers[idx])
            }

            ClusterResult(cafe, closestIndex)
        }
    }

    fun recalculateCenters(assignments: List<ClusterResult>,
                           k: Int, cafes:
                           List<Cafe>): List<LatLng> {
        val clusters = assignments.groupBy {it.clusterId }
        return (0 until k).map  { clusterId ->
            val pointsInCluster = clusters[clusterId] ?: emptyList()

            if (pointsInCluster.isEmpty()) {
                cafes.random().location
            }

            else {
                val sortedLats = pointsInCluster.map { it.cafe.location.latitude }.sorted()
                val sortedLngs = pointsInCluster.map { it.cafe.location.longitude }.sorted()

                val medianLat = sortedLats[sortedLats.size / 2]
                val medianLng = sortedLngs[sortedLngs.size / 2]

                LatLng(medianLat, medianLng)
            }
        }
    }

    fun centersChanged(previous: List<LatLng>, current: List<LatLng>): Boolean {
        if (previous.size != current.size) {
            return true
        }

        return previous.zip(current).any {
            (prev, curr) -> manhattanDistance(prev, curr) > 1.0
        }
    }


    fun cluster(cafes: List<Cafe>, k: Int): List<ClusterResult> {
        if (cafes.isEmpty() || k <= 0 ) { return emptyList() }

        if (k >= cafes.size) {
            return cafes.mapIndexed { index, cafe ->
                ClusterResult(cafe, index)
            }
        }

        var centers = initCenters(cafes, k)
        var assignments = assignToClusters(cafes, centers)
        var previousCenters: List<LatLng>

        do {
            previousCenters = centers
            centers = recalculateCenters(assignments, k, cafes)
            assignments = assignToClusters(cafes, centers)
        } while (centersChanged(previousCenters, centers))

        return assignments
    }
}