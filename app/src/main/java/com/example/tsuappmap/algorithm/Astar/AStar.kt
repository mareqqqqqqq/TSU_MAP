package com.example.tsuappmap.algorithm.Astar

import com.example.tsuappmap.map.CampusGrid
import java.util.PriorityQueue
import kotlin.math.sqrt

object AStar {

    data class SearchStep(
        val current: Pair<Int, Int>,
        val visited: Set<Pair<Int, Int>>? = null,
        val frontier: Set<Pair<Int, Int>>? = null,
        val path: List<Pair<Int, Int>>? = null
    )

    fun findPathWithSteps(
        startRow: Int, startCol: Int,
        endRow: Int, endCol: Int
    ): List<SearchStep> {
        val steps = mutableListOf<SearchStep>()

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
        openSet.add(Node(startRow, startCol, 0.0, heuristic(startRow, startCol, endRow, endCol)))
        frontier.add(Pair(startRow, startCol))

        val animationStep = 50
        val maxSteps = 400
        var stepCounter = 0

        while (openSet.isNotEmpty()) {
            val current = openSet.poll()!!
            val currentCell = Pair(current.row, current.col)

            if (current.g > gScore[current.row][current.col]) continue

            frontier.remove(currentCell)
            visited.add(currentCell)

            stepCounter++
            if (stepCounter % animationStep == 0 && steps.size < maxSteps) {
                steps.add(
                    SearchStep(
                        current = currentCell,
                        visited = visited.toSet(),
                        frontier = frontier.toSet()
                    )
                )
            }

            if (current.row == endRow && current.col == endCol) {
                val path = reconstructPath(cameFrom, endRow, endCol)
                steps.add(
                    SearchStep(
                        current = currentCell,
                        visited = visited.toSet(),
                        frontier = frontier.toSet(),
                        path = path
                    )
                )
                return steps
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

        steps.add(SearchStep(Pair(startRow, startCol), visited.toSet(), frontier.toSet(),
            null))
        return steps
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
                            row, nCol)) continue
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
        while (current != null){
            path.add(current)
            val(r, c) = current
            current = cameFrom[r][c]
        }
        path.reverse()
        return path
    }
}