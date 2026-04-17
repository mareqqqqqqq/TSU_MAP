package com.example.tsuappmap.algorithm.Astar

import com.example.tsuappmap.map.CampusGrid
import kotlinx.coroutines.channels.Channel
import java.util.PriorityQueue
import kotlin.math.sqrt

object AStar {

    data class SearchStep(
        val current: Pair<Int, Int>,
        val visited: Set<Pair<Int, Int>>,
        val frontier: Set<Pair<Int, Int>>,
        val path: List<Pair<Int, Int>>? = null,
        val done: Boolean = false
    )

    suspend fun findPathWithChannel(
        startRow: Int, startCol: Int,
        endRow: Int, endCol: Int,
        channel: Channel<SearchStep>
    ) {
        val openSet = PriorityQueue<Node>(compareBy { it.f })
        val gScore = Array(CampusGrid.rows) {
            DoubleArray(CampusGrid.cols) { Double.MAX_VALUE }
        }
        val cameFrom = Array(CampusGrid.rows) {
            arrayOfNulls<Pair<Int, Int>>(CampusGrid.cols)
        }
        val visited = mutableSetOf<Pair<Int, Int>>()
        val frontier = mutableSetOf<Pair<Int, Int>>()

        gScore[startRow][startCol] = 0.0
        openSet.add(
            Node(
                startRow, startCol, 0.0,
                heuristic(startRow, startCol, endRow, endCol)
            )
        )
        frontier.add(Pair(startRow, startCol))

        val animStep = 50
        var counter = 0

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()!!
            val currentCell = Pair(current.row, current.col)

            if (current.g > gScore[current.row][current.col]) continue

            frontier.remove(currentCell)
            visited.add(currentCell)

            counter++
            if (counter % animStep == 0) {
                channel.send(
                    SearchStep(
                        current = currentCell,
                        visited = visited.toSet(),
                        frontier = frontier.toSet()
                    )
                )
            }

            if (current.row == endRow && current.col == endCol) {
                val path = reconstructPath(cameFrom, endRow, endCol)
                channel.send(
                    SearchStep(
                        current = currentCell,
                        visited = visited.toSet(),
                        frontier = frontier.toSet(),
                        path = path,
                        done = true
                    )
                )
                channel.close()
                return
            }

            for ((nRow, nCol) in getNeighbours(current.row, current.col)) {
                val stepCost = if (nRow != current.row && nCol != current.col) 1.414 else 1.0
                val newG = gScore[current.row][current.col] + stepCost
                if (newG < gScore[nRow][nCol]) {
                    gScore[nRow][nCol] = newG
                    cameFrom[nRow][nCol] = currentCell
                    val h = heuristic(nRow, nCol, endRow, endCol)
                    openSet.add(Node(nRow, nCol, newG, newG + h))
                    frontier.add(Pair(nRow, nCol))
                }
            }
        }

        channel.send(
            SearchStep(
                current = Pair(startRow, startCol),
                visited = visited.toSet(),
                frontier = frontier.toSet(),
                path = null,
                done = true
            )
        )
        channel.close()
    }

    private data class Node(val row: Int, val col: Int, val g: Double, val f: Double)

    private fun heuristic(row: Int, col: Int, endRow: Int, endCol: Int): Double {
        val dr = (row - endRow).toDouble()
        val dc = (col - endCol).toDouble()
        return sqrt(dr * dr + dc * dc)
    }

    private fun getNeighbours(row: Int, col: Int): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nRow = row + dr
                val nCol = col + dc
                if (!CustomObstacle.isWalkable(nRow, nCol)) continue
                if (dr != 0 && dc != 0) {
                    if (!CustomObstacle.isWalkable(nRow, col) || !CustomObstacle.isWalkable(
                            row, nCol
                        )
                    ) continue
                }
                result.add(Pair(nRow, nCol))
            }
        }
        return result
    }

    private fun reconstructPath(
        cameFrom: Array<Array<Pair<Int, Int>?>>,
        endRow: Int, endCol: Int
    ): List<Pair<Int, Int>> {
        val path = mutableListOf<Pair<Int, Int>>()
        var current: Pair<Int, Int>? = Pair(endRow, endCol)
        while (current != null) {
            path.add(current)
            val (r, c) = current
            current = cameFrom[r][c]
        }
        path.reverse()
        return path
    }

    fun findPath(
        startRow: Int, startCol: Int,
        endRow: Int, endCol: Int
    ): List<Pair<Int, Int>>? {
        if (startRow == endRow && startCol == endCol) return listOf(Pair(startRow, startCol))

        val openSet = PriorityQueue<Node>(compareBy { it.f })
        val gScore = HashMap<Long, Double>()
        val cameFrom = HashMap<Long, Long>()

        fun key(r: Int, c: Int): Long = r.toLong() * CampusGrid.cols + c

        val startKey = key(startRow, startCol)
        gScore[startKey] = 0.0
        openSet.add(Node(startRow, startCol, 0.0, heuristic(startRow, startCol, endRow, endCol)))

        val closedSet = HashSet<Long>()

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()!!
            val currentKey = key(current.row, current.col)

            if (current.row == endRow && current.col == endCol) {
                val path = mutableListOf<Pair<Int, Int>>()
                var k: Long? = currentKey
                while (k != null) {
                    val r = (k / CampusGrid.cols).toInt()
                    val c = (k % CampusGrid.cols).toInt()
                    path.add(Pair(r, c))
                    k = cameFrom[k]
                }
                path.reverse()
                return path
            }

            if (!closedSet.add(currentKey)) continue
            if (current.g > (gScore[currentKey] ?: Double.MAX_VALUE)) continue

            for ((nRow, nCol) in getNeighbours(current.row, current.col)) {
                val nKey = key(nRow, nCol)
                if (nKey in closedSet) continue
                val stepCost = if (nRow != current.row && nCol != current.col) 1.414 else 1.0
                val newG = current.g + stepCost
                if (newG < (gScore[nKey] ?: Double.MAX_VALUE)) {
                    gScore[nKey] = newG
                    cameFrom[nKey] = currentKey
                    openSet.add(
                        Node(
                            nRow,
                            nCol,
                            newG,
                            newG + heuristic(nRow, nCol, endRow, endCol)
                        )
                    )
                }
            }
        }
        return null
    }

    fun pathDistance(path: List<Pair<Int, Int>>): Double {
        var dist = 0.0
        for (i in 0 until path.size - 1) {
            val (r1, c1) = path[i]
            val (r2, c2) = path[i + 1]
            dist += if (r1 != r2 && c1 != c2) 1.414 else 1.0
        }
        return dist
    }

    fun pathDistanceMeters(path: List<Pair<Int, Int>>): Double {
        return pathDistance(path) * CampusGrid.cellSize
    }
}