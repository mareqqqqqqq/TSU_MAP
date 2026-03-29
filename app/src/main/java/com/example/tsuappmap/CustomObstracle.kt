package com.example.tsuappmap

object CustomObstracle {

    private val obstracles = mutableSetOf<Pair<Int, Int>>()

    fun toggle(row: Int, col: Int): Boolean{
        val cell = Pair(row, col)
        return if (obstracles.contains(cell)) {
            obstracles.remove(cell)
            false
        }
        else {
            obstracles.add(cell)
            true
        }
    }

    fun isBlocked(row: Int, col: Int): Boolean = obstracles.contains(Pair(row, col))

    fun isWalkable(row: Int, col: Int): Boolean = CampusGrid.isWalkable(row, col) && !isBlocked(row, col)

    fun clear() = obstracles.clear()

    fun getAll(): Set<Pair<Int, Int>> = obstracles.toSet()
}