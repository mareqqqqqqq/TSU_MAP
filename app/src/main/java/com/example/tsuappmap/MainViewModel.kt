package com.example.tsuappmap

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tsuappmap.algorithm.AntColony.AntColony
import com.example.tsuappmap.algorithm.AntColony.Attractions
import com.example.tsuappmap.algorithm.AntColony.PointOfAttractions
import com.example.tsuappmap.algorithm.Astar.AStar
import com.example.tsuappmap.algorithm.Astar.CustomObstacle
import com.example.tsuappmap.algorithm.Claster.CafeData
import com.example.tsuappmap.algorithm.Claster.ClusterDisplay
import com.example.tsuappmap.algorithm.Claster.KMeansAstar
import com.example.tsuappmap.algorithm.Claster.KMeansManhattan
import com.example.tsuappmap.algorithm.Claster.Kmeans
import com.example.tsuappmap.map.CampusGrid
import com.example.tsuappmap.map.MapRoute
import com.example.tsuappmap.map.MapRouteForAnt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.annotations.Marker
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

class MainViewModel : ViewModel() {

    var mapRef: MapLibreMap? = null
    var selectedTab by mutableStateOf(0)

    var isObstacleMode by mutableStateOf(false)
    var placingStart by mutableStateOf(false)
    var placingEnd by mutableStateOf(false)

    var startPoint by mutableStateOf<Pair<Int, Int>?>(null)
    var endPoint by mutableStateOf<Pair<Int, Int>?>(null)
    var barierStart by mutableStateOf<Pair<Int, Int>?>(null)

    private var animationJob: Job? = null

    var placingAntStart by mutableStateOf(false)
    var antStartPoint by mutableStateOf<Pair<Int, Int>?>(null)
    var antStartSet by mutableStateOf(false)
    var clearCounter by mutableStateOf(0)

    var showDigitScreen by mutableStateOf(false)
    var showDecisionTree by mutableStateOf(false)

    var activeLegendK by mutableStateOf(0)
    var activeLegendMethod by mutableStateOf("")

    var isAstarClustering by mutableStateOf(false)
    var astarProgress by mutableStateOf(0f)

    private var clusterMarkers = mutableListOf<Marker>()

    fun clearMap() {
        val map = mapRef ?: return
        animationJob?.cancel()

        MapRoute.clearSearch(map)
        MapRoute.clearRoute(map)
        MapRoute.clearMarkers(map)
        MapRoute.clearAttractionRoute(map)
        MapRoute.clearClusters(map)
        MapRoute.clearFoodRoute(map)
        MapRouteForAnt.clearAntRoute(map)
        MapRouteForAnt.clearStartMarker(map)
        CustomObstacle.clear()
        MapRoute.drawObstacles(map)

        clusterMarkers.forEach { map.removeMarker(it) }
        clusterMarkers.clear()
        activeLegendK = 0
        activeLegendMethod = ""

        startPoint = null
        endPoint = null
        barierStart = null
        antStartSet = false
        antStartPoint = null
        isObstacleMode = false
        placingStart = false
        placingEnd = false
        placingAntStart = false
        clearCounter++
    }

    fun onPlaceStartClicked(context: Context) {
        isObstacleMode = false
        placingStart = true
        placingEnd = false
        placingAntStart = false
        Toast.makeText(context, "Выбери стартовую точку", Toast.LENGTH_SHORT).show()
    }

    fun onPlaceEndClicked(context: Context) {
        isObstacleMode = false
        placingStart = false
        placingEnd = true
        placingAntStart = false
        Toast.makeText(context, "Выбери конечную точку", Toast.LENGTH_SHORT).show()
    }

    fun onToggleObstacle(context: Context) {
        isObstacleMode = !isObstacleMode
        barierStart = null
        placingStart = false
        placingEnd = false
        placingAntStart = false
        val msg = if (isObstacleMode) "Режим барьера включён" else "Режим барьера выключен"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun onPlaceAntStart(context: Context) {
        isObstacleMode = false
        placingAntStart = true
        placingStart = false
        placingEnd = false
        Toast.makeText(context, "Тапни на карте — стартовая точка", Toast.LENGTH_SHORT).show()
    }

    fun onMapClick(latLng: LatLng, map: MapLibreMap, context: Context): Boolean {
        val cell = CampusGrid.latLonToCell(latLng.latitude, latLng.longitude) ?: return true
        when {
            placingAntStart -> handleAntStartClick(cell, map, context)
            isObstacleMode -> handleObstacleClick(cell, map, context)
            placingStart -> handleStartClick(cell, map, context)
            placingEnd -> handleEndClick(cell, map, context)
        }
        return true
    }

    private fun handleAntStartClick(cell: Pair<Int, Int>, map: MapLibreMap, context: Context) {
        val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
        if (walkable == null) {
            Toast.makeText(context, "Нет доступных точек рядом", Toast.LENGTH_SHORT).show()
            return
        }
        antStartPoint = walkable
        antStartSet = true
        placingAntStart = false
        MapRouteForAnt.setStartMarker(context, map, walkable)
        Toast.makeText(context, "Стартовая точка установлена", Toast.LENGTH_SHORT).show()
    }

    private fun handleObstacleClick(cell: Pair<Int, Int>, map: MapLibreMap, context: Context) {
        if (barierStart == null) {
            barierStart = cell
            Toast.makeText(context, "Выбери конец барьера", Toast.LENGTH_SHORT).show()
        } else {
            val (r1, c1) = barierStart!!
            val (r2, c2) = cell
            CustomObstacle.addLine(r1, c1, r2, c2)
            barierStart = null
            MapRoute.drawObstacles(map)
        }
    }

    private fun handleStartClick(cell: Pair<Int, Int>, map: MapLibreMap, context: Context) {
        val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
        if (walkable == null) {
            Toast.makeText(context, "Нет доступных точек рядом", Toast.LENGTH_SHORT).show()
            return
        }
        startPoint = walkable
        placingStart = false
        MapRoute.setStartMarker(map, walkable)
        Toast.makeText(context, "Старт установлен", Toast.LENGTH_SHORT).show()
        endPoint?.let { end -> startAStar(map, context, walkable, end) }
    }

    private fun handleEndClick(cell: Pair<Int, Int>, map: MapLibreMap, context: Context) {
        val walkable = CampusGrid.nearestWalkable(cell.first, cell.second)
        if (walkable == null) {
            Toast.makeText(context, "Нет доступных точек рядом", Toast.LENGTH_SHORT).show()
            return
        }
        endPoint = walkable
        placingEnd = false
        MapRoute.setEndMarker(map, walkable)
        Toast.makeText(context, "Конечная точка установлена", Toast.LENGTH_SHORT).show()
        startPoint?.let { start -> startAStar(map, context, start, walkable) }
    }

    private fun startAStar(
        map: MapLibreMap, context: Context, start: Pair<Int, Int>, end: Pair<Int, Int>
    ) {
        animationJob?.cancel()
        MapRoute.clearSearch(map)
        MapRoute.clearRoute(map)

        animationJob = viewModelScope.launch {
            val channel = Channel<AStar.SearchStep>(capacity = 4)
            val aStarJob = launch(Dispatchers.Default) {
                AStar.findPathWithChannel(start.first, start.second, end.first, end.second, channel)
            }
            for (step in channel) {
                MapRoute.drawSearchStep(map, step.visited, step.frontier, step.current)
                delay(50L)
                if (step.done) {
                    MapRoute.clearSearch(map)
                    if (step.path != null) {
                        MapRoute.drawRoute(map, step.path)
                    } else {
                        Toast.makeText(context, "Маршрут не найден", Toast.LENGTH_SHORT).show()
                    }
                    break
                }
            }
            aStarJob.cancel()
        }
    }

    private fun clearClusterMarkers(map: MapLibreMap) {
        clusterMarkers.forEach { map.removeMarker(it) }
        clusterMarkers.clear()
        MapRoute.clearClusters(map)
    }

    fun showEuclideanClusters(context: Context, k: Int = 3) {
        val map = mapRef ?: return
        clearClusterMarkers(map)
        val cafes = CafeData.getAllCafes()
        val result = Kmeans().cluster(cafes, k = k)
        val markers = ClusterDisplay.drawClusters(
            map, result, context, method = ClusterDisplay.METHOD_EUCLIDEAN, k = k
        )
        clusterMarkers.addAll(markers)
        activeLegendK = k
        activeLegendMethod = ClusterDisplay.METHOD_EUCLIDEAN
        Toast.makeText(context, "Евклид, кластеров: $k", Toast.LENGTH_SHORT).show()
    }

    fun showManhattanClusters(context: Context, k: Int = 3) {
        val map = mapRef ?: return
        clearClusterMarkers(map)
        val cafes = CafeData.getAllCafes()
        val result = KMeansManhattan().cluster(cafes, k = k)
        val markers = ClusterDisplay.drawClusters(
            map, result, context, method = ClusterDisplay.METHOD_MANHATTAN, k = k
        )
        clusterMarkers.addAll(markers)
        activeLegendK = k
        activeLegendMethod = ClusterDisplay.METHOD_MANHATTAN
        Toast.makeText(context, "Манхэттен, кластеров: $k", Toast.LENGTH_SHORT).show()
    }

    fun showAstarClusters(context: Context, k: Int = 3) {
        val map = mapRef ?: return
        if (isAstarClustering) {
            Toast.makeText(context, "Уже считается...", Toast.LENGTH_SHORT).show()
            return
        }
        isAstarClustering = true
        astarProgress = 0f
        clearClusterMarkers(map)

        viewModelScope.launch {
            Toast.makeText(context, "Считаем пешеходные расстояния...", Toast.LENGTH_LONG).show()
            val cafes = CafeData.getAllCafes()
            val result = withContext(Dispatchers.Default) {
                KMeansAstar().cluster(cafes, k) { progress ->
                    astarProgress = progress
                }
            }
            val markers = ClusterDisplay.drawClusters(
                map, result, context, method = ClusterDisplay.METHOD_ASTAR, k = k
            )
            clusterMarkers.addAll(markers)
            activeLegendK = k
            activeLegendMethod = ClusterDisplay.METHOD_ASTAR
            isAstarClustering = false
            astarProgress = 0f
            Toast.makeText(context, "A* кластеры готовы, k=$k", Toast.LENGTH_SHORT).show()
        }
    }

    fun runAntColony(selectedIndices: Set<Int>, context: Context) {
        val map = mapRef ?: return
        val start = antStartPoint
        if (start == null) {
            Toast.makeText(context, "Сначала установи стартовую точку", Toast.LENGTH_SHORT).show()
            return
        }
        val selectedPois = selectedIndices.sorted().map { Attractions.allPoint[it] }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                buildAntColonyRoute(start, selectedPois)
            }
            if (result == null) {
                Toast.makeText(context, "Не удалось построить маршрут", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val (segments, orderedCells) = result
            MapRouteForAnt.drawAntRoute(context, map, segments, orderedCells)
            Toast.makeText(context, "Маршрут построен!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildAntColonyRoute(
        startCell: Pair<Int, Int>, selectedPois: List<PointOfAttractions>
    ): Triple<List<List<Pair<Int, Int>>>, List<Pair<Int, Int>>, Int>? {
        val cells = mutableListOf<Pair<Int, Int>>()
        cells.add(startCell)

        for (poi in selectedPois) {
            val cell = CampusGrid.latLonToCell(poi.lat, poi.lon)
            val walkable =
                if (cell != null) CampusGrid.nearestWalkable(cell.first, cell.second) else null
            if (walkable != null && walkable !in cells) cells.add(walkable)
        }

        val n = cells.size
        if (n < 2) return null

        val distMatrix = Array(n) { DoubleArray(n) }
        val paths = Array(n) { arrayOfNulls<List<Pair<Int, Int>>>(n) }

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val astarResult = AStar.findPathOnly(
                    cells[i].first, cells[i].second, cells[j].first, cells[j].second
                )
                val length = astarResult?.length ?: (Double.MAX_VALUE / 2)
                val path = astarResult?.path ?: emptyList()
                distMatrix[i][j] = length
                distMatrix[j][i] = length
                paths[i][j] = path
                paths[j][i] = path.reversed()
            }
        }

        val tourOrder = AntColony.solve(distMatrix)
        val segments = mutableListOf<List<Pair<Int, Int>>>()
        for (k in 0 until tourOrder.size - 1) {
            val from = tourOrder[k]
            val to = tourOrder[k + 1]
            segments.add(paths[from][to] ?: emptyList())
        }
        val orderedCells = tourOrder.dropLast(1).map { cells[it] }
        return Triple(segments, orderedCells, cells.size)
    }
}