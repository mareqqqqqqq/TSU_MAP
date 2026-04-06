package com.example.tsuappmap.algorithm.AntColony

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.graphics.Matrix
import kotlin.math.pow
import kotlin.random.Random

object AntColony {
    fun solve(distMatrix: Array<DoubleArray>): List<Int> {
        val alpha = 1.0
        val beta = 2.0
        val isp = 0.1
        val q = 100.0
        val iterations = 300

        val n = distMatrix.size

        val pheromone = Array(n) { DoubleArray(n) { 1.0 } }

        var bestPath: List<Int> = emptyList()

        var bestLength = Double.MAX_VALUE


        repeat(iterations)
        {
            val paths = Array(n)
            { antIndex ->
                buildTour(antIndex, n, distMatrix, pheromone, alpha, beta)
            }

            for (i in 0 until n)
            {
                for (j in 0 until n)
                {
                    pheromone[i][j] *= (1.0 - isp)
                }
            }

            for (path in paths)
            {
                val length = tourLength(path, distMatrix)

                val amountOfPheromone = q / length

                for (i in 0 until n)
                {
                    val from = path[i]
                    val to = path[(i+1) % n]
                    pheromone[from][to] += amountOfPheromone
                    pheromone[to][from] += amountOfPheromone
                }

                if (length < bestLength)
                {
                    bestLength = length
                    bestPath = path
                }

            }

        }




        return bestPath + bestPath[0]
    }

    private fun buildTour(
        start: Int,
        n: Int,
        distMatrix: Array<DoubleArray>,
        pheromone: Array<DoubleArray>,
        alpha: Double,
        beta: Double
    ): List<Int> {

        val visited = BooleanArray(n)
        val tour = mutableListOf<Int>()

        var current = start
        visited[current] = true
        tour.add(current)

        repeat(n)
        {
            val next = chooseNext(current, visited,n,distMatrix,pheromone,alpha,beta)
            visited[next] = true
            tour.add(next)
            current = next
        }

        return tour

    }



    private fun chooseNext(
        current: Int,
        visited: BooleanArray,
        n: Int,
        distMatrix: Array<DoubleArray>,
        pheromone: Array<DoubleArray>,
        alpha: Double,
        beta: Double

    ): Int {
        val scores = DoubleArray(n) {
            j -> if (visited[j] || distMatrix[current][j] == 0.0) 0.0
            else pheromone[current][j].pow(alpha) * (1.0 / distMatrix[current][j]).pow(beta)
        }

        val total = scores.sum()

        var otv = 0
        val rand = Random.nextDouble(total)
        var cumulative = 0.0
        for (j in 0 until n)
        {
            cumulative += scores[j]
            if (rand <= cumulative)
            {
                otv = j
                break
            }
        }

        return otv

    }

    fun tourLength(tour: List<Int>, distMatrix: Array<DoubleArray>): Double
    {
        var length = 0.0
        for (k in 0 until tour.size - 1)
        {
            length += distMatrix[tour[k]][tour[k+1]]
        }

        length += distMatrix[tour.last()][tour.first()]

        return length
    }



}


