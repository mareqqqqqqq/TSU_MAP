package com.example.tsuappmap.algorithm.Claster
import org.maplibre.android.geometry.LatLng
import kotlin.math.sqrt

data class ClusterResult (
    val cafe: Cafe,
    val clusterId: Int
)

class Kmeans {
    fun initCenters(cafes: List<Cafe>, k: Int): List<LatLng> {
        if (k >= cafes.size) {
            return cafes.map { it.location }
        }

        val centers = mutableListOf<LatLng>() // список центроид

        val firstCenter = cafes.random().location;
        centers.add(firstCenter)

        for (centerIndex in 2..k) {
            // посчитаем расстояние от первой цнетроиды и до каждого кафу и возведём в квадрат
            val distances = cafes.map { cafe ->
                val minDistance = centers.minOf {
                    center -> euclideanDistance(cafe.location, center)
                }

                minDistance * minDistance
            }

            // сумма
            val totalDistance = distances.sum();

            if (totalDistance == 0.0) {
                break
            }

            // вероятностный метод, штука из k-means++
            // рандомное число от 0 до 1 умножен на тотал дсит
            val random = Math.random() * totalDistance

            // сумма квадратов расстояний, копим
            var cumulative = 0.0
            var selectedIndex = 0

            for (i in distances.indices) {
                cumulative += distances[i]
                if (cumulative >= random) {
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
                euclideanDistance(cafe.location, centers[idx])
            }

            ClusterResult(cafe, closestIndex)
        }
    }

    fun recalculateCenters(assignments: List<ClusterResult>,
                           k: Int,
                           allCafes: List<Cafe>): List<LatLng> {
        val clusters = assignments.groupBy { it.clusterId }

        return (0 until k).map { clusterId ->
            val pointsInCluster = clusters[clusterId] ?: emptyList()

            if (pointsInCluster.isEmpty()) {
                allCafes.random().location
            }

            else {
                val avgLat = pointsInCluster.map { it.cafe.location.latitude }.average()
                val avgLng = pointsInCluster.map { it.cafe.location.longitude }.average()
                LatLng(avgLat, avgLng)
            }
        }
    }

    fun centersChanged(previous: List<LatLng>, current: List<LatLng>): Boolean {
        if (previous.size != current.size) return true;

        // группирует по парам типо старое новое
        return previous.zip(current).any { (prev, curr) ->
            euclideanDistance(prev, curr) > 1.0
        }
    }

    fun cluster(cafes: List<Cafe>, k: Int): List<ClusterResult> {
        if (cafes.isEmpty() || k <= 0) return emptyList()

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













    private fun euclideanDistance(a: LatLng, b: LatLng): Double {
        val latDiff = (a.latitude - b.latitude) * 111320.0
        val lngDiff = (a.longitude - b.longitude) * 111320.0

        return sqrt(latDiff * latDiff + lngDiff * lngDiff)
    }


}