package com.example.tsuappmap.algorithm.Astar

import com.example.tsuappmap.map.CampusGrid

object CustomObstacle {

    private val obstacle = mutableSetOf<Pair<Int, Int>>()

    fun isBlocked(row: Int, col: Int): Boolean = obstacle.contains(Pair(row, col))

    fun isWalkable(row: Int, col: Int): Boolean =
        CampusGrid.isWalkable(row, col) && !isBlocked(row, col)

    fun clear() = obstacle.clear()

    fun getAll(): Set<Pair<Int, Int>> = obstacle.toSet()


    fun addLine(row1: Int, col1: Int, row2: Int, col2: Int) {
        var r = row1
        var c = col1
        val dr = Math.abs(row2 - row1)
        val dc = Math.abs(col2 - col1)
        val sr = if (row1 < row2) 1 else -1
        val sc = if (col1 < col2) 1 else -1
        var err = dr - dc

        while (true) {
            obstacle.add(Pair(r, c))
            if (r == row2 && c == col2) break
            val e2 = 2 * err
            if (e2 > -dc) {
                err -= dc; r += sr
            }
            if (e2 < dr) {
                err += dr; c += sc
            }
        }
    }
}