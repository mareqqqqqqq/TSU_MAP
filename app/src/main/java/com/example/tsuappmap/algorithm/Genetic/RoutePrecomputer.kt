package com.example.tsuappmap.algorithm.Genetic

import com.example.tsuappmap.algorithm.Astar.AStar
import com.example.tsuappmap.map.CampusGrid

data class PrecomputedData(
    val establishments: List<FoodEstablishment>,
    val cells: List<Pair<Int, Int>>,
    val distMatrix: Array<DoubleArray>,
    val pathMatrix: Array<Array<List<Pair<Int, Int>>?>>
)

object RoutePrecomputer {

    private const val UNREACHABLE = Double.MAX_VALUE / 2

    private val NEIGHBOURS = arrayOf(
        -1 to 0, 1 to 0, 0 to -1, 0 to 1,
        -1 to -1, -1 to 1, 1 to -1, 1 to 1
    )


    fun nearestWalkable(row: Int, col: Int, maxVisits: Int = 2_000): Pair<Int, Int>? {
        if (CampusGrid.isWalkable(row, col)) return row to col

        val seen = HashSet<Long>(maxVisits * 2)
        val frontier = ArrayDeque<Pair<Int, Int>>()

        fun packKey(r: Int, c: Int): Long =
            (r.toLong() shl 32) or (c.toLong() and 0xffffffffL)

        frontier += row to col
        seen += packKey(row, col)
        var visited = 0

        while (frontier.isNotEmpty() && visited < maxVisits) {
            val (r, c) = frontier.removeFirst()
            visited++
            for ((dr, dc) in NEIGHBOURS) {
                val nr = r + dr
                val nc = c + dc
                val key = packKey(nr, nc)
                if (!seen.add(key)) continue
                if (CampusGrid.isWalkable(nr, nc)) return nr to nc
                frontier += nr to nc
            }
        }
        return null
    }

    fun compute(
        startCell: Pair<Int, Int>,
        candidateEstablishments: List<FoodEstablishment>,
        onProgress: ((Int, Int) -> Unit)? = null
    ): PrecomputedData? {

        val bound = ArrayList<Pair<FoodEstablishment, Pair<Int, Int>>>(candidateEstablishments.size)
        for (est in candidateEstablishments) {
            val raw = CampusGrid.latLonToCell(
                est.location.latitude,
                est.location.longitude
            ) ?: continue
            val cell = nearestWalkable(raw.first, raw.second) ?: continue
            bound += est to cell
        }
        if (bound.isEmpty()) return null

        val validEsts = bound.map { it.first }
        val estCells = bound.map { it.second }

        val nodes: List<Pair<Int, Int>> = buildList(estCells.size + 1) {
            add(startCell)
            addAll(estCells)
        }
        val n = nodes.size

        val distances = Array(n) { DoubleArray(n) { UNREACHABLE } }
        val paths: Array<Array<List<Pair<Int, Int>>?>> = Array(n) { arrayOfNulls(n) }
        for (i in 0 until n) {
            distances[i][i] = 0.0
            paths[i][i] = listOf(nodes[i])
        }

        val pairsTotal = n * (n - 1) / 2
        var pairsDone = 0
        for (i in 0 until n - 1) {
            val (ri, ci) = nodes[i]
            for (j in i + 1 until n) {
                val (rj, cj) = nodes[j]
                val p = AStar.findPathOnly(ri, ci, rj, cj)
                val d = if (p != null) AStar.pathDistanceMeters(p.path) else UNREACHABLE

                distances[i][j] = d
                distances[j][i] = d
                paths[i][j] = p?.path
                paths[j][i] = p?.path?.reversed()

                pairsDone++
                onProgress?.invoke(pairsDone, pairsTotal)
            }
        }

        return PrecomputedData(
            establishments = validEsts,
            cells = nodes,
            distMatrix = distances,
            pathMatrix = paths
        )
    }
}
