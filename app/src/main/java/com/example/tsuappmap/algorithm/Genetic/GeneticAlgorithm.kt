package com.example.tsuappmap.algorithm.Genetic

import kotlin.random.Random

data class Route(
    val establishments: List<FoodEstablishment>,
    val fitness: Double,
    val totalDistMeters: Double,
    val totalTimeMin: Double,
    val pathSegments: List<List<Pair<Int, Int>>>
)

class GeneticAlgorithm(
    private val data: PrecomputedData,
    private val requiredItems: Set<String>,
    private val currentHour: Int,
    private val currentMinute: Int,
    val totalGenerations: Int = 250,
    private val populationSize: Int = 120,
    private val mutationRate: Double = 0.18,
    private val eliteCount: Int = 12,
    private val walkingSpeedKmH: Double = 5.0
) {
    var bestRoute: Route? = null
        private set

    var currentGeneration: Int = 0
        private set

    var onGenerationUpdate: ((Route, Int) -> Unit)? = null

    private val chromosomeLen = data.establishments.size

    private class Individual(val genes: IntArray, val route: Route?) {
        val score: Double get() = route?.fitness ?: Double.POSITIVE_INFINITY
    }

    fun run(): Route? {

        if (chromosomeLen == 0) return null

        if (chromosomeLen == 1) {
            val singleRoute = decode(IntArray(1) { 0 })
            bestRoute = singleRoute
            singleRoute?.let { onGenerationUpdate?.invoke(it, totalGenerations) }
            return singleRoute
        }

        var population = spawnInitialPopulation()
        if (population.all { it.route == null }) return null

        var champion = population.minByOrNull { it.score }!!
        bestRoute = champion.route
        champion.route?.let { onGenerationUpdate?.invoke(it, 0) }

        var gen = 1
        while (gen <= totalGenerations) {
            currentGeneration = gen

            population = nextGeneration(population)
            val genLeader = population.minByOrNull { it.score }
            if (genLeader != null && genLeader.score < champion.score) {
                champion = genLeader
                bestRoute = genLeader.route
            }
            champion.route?.let { onGenerationUpdate?.invoke(it, gen) }

            gen++
        }
        return bestRoute
    }


    private fun spawnInitialPopulation(): List<Individual> {
        val pool = ArrayList<Individual>(populationSize)
        repeat(populationSize) {
            val g = IntArray(chromosomeLen) { it }
            g.shuffle()
            pool += Individual(g, decode(g))
        }
        return pool
    }

    private fun nextGeneration(current: List<Individual>): List<Individual> {
        val ranked = current.sortedBy { it.score }

        val next = ArrayList<Individual>(populationSize)

        for (k in 0 until eliteCount.coerceAtMost(ranked.size)) {
            next += ranked[k]
        }

        while (next.size < populationSize) {
            val mom = selectByRank(ranked)
            val dad = selectByRank(ranked)
            var childGenes = crossoverPMX(mom.genes, dad.genes)
            if (Random.nextDouble() < mutationRate) {
                childGenes = mutate(childGenes)
            }
            next += Individual(childGenes, decode(childGenes))
        }
        return next
    }


    private fun decode(genes: IntArray): Route? {
        val remaining = HashSet(requiredItems)
        val visitedEst = ArrayList<Int>(requiredItems.size.coerceAtLeast(1))

        var k = 0
        while (k < genes.size && remaining.isNotEmpty()) {
            val estIdx = genes[k]
            val est = data.establishments[estIdx]

            var addedAny = false
            for (mi in est.menu) {
                if (remaining.remove(mi.name)) addedAny = true
            }
            if (addedAny) visitedEst += estIdx
            k++
        }

        if (remaining.isNotEmpty()) return null
        return composeRoute(visitedEst)
    }

    private fun composeRoute(visitedEst: List<Int>): Route {
        val nodeSeq = IntArray(visitedEst.size + 1).also { arr ->
            arr[0] = 0
            for (i in visitedEst.indices) arr[i + 1] = visitedEst[i] + 1
        }

        var travelledM = 0.0
        var structuralPenalty = 0.0
        val legs = ArrayList<List<Pair<Int, Int>>>(visitedEst.size)

        for (i in 0 until nodeSeq.size - 1) {
            val a = nodeSeq[i]
            val b = nodeSeq[i + 1]
            val raw = data.distMatrix[a][b]
            if (raw >= UNREACHABLE_CUTOFF) {
                travelledM += PROXY_UNREACHABLE_M
                structuralPenalty += UNREACHABLE_PENALTY
            } else {
                travelledM += raw
            }
            legs += data.pathMatrix[a][b] ?: listOf(data.cells[a], data.cells[b])
        }

        val timingPenalty = timingPenaltyFor(visitedEst)
        val travelMin = (travelledM / 1000.0 / walkingSpeedKmH) * 60.0
        val totalTimeMin = travelMin + visitedEst.size * STOP_SERVICE_MIN

        return Route(
            establishments = visitedEst.map { data.establishments[it] },
            fitness = travelledM + structuralPenalty + timingPenalty,
            totalDistMeters = travelledM,
            totalTimeMin = totalTimeMin,
            pathSegments = legs
        )
    }

    private fun timingPenaltyFor(visitedEst: List<Int>): Double {
        var accM = 0.0
        var prev = 0
        var penalty = 0.0
        for (estIdx in visitedEst) {
            val node = estIdx + 1
            val raw = data.distMatrix[prev][node]
            accM += if (raw >= UNREACHABLE_CUTOFF) PROXY_UNREACHABLE_M else raw

            val travelMin = (accM / 1000.0 / walkingSpeedKmH) * 60.0
            val clockAbs = currentHour * 60 + currentMinute + travelMin
            val h = (clockAbs / 60.0).toInt()
            val m = (clockAbs - h * 60).toInt()

            val slack = data.establishments[estIdx].minutesUntilClose(h, m)
            penalty += when {
                slack <= 0 -> CLOSED_ARRIVAL_PENALTY
                slack <= 30 -> (30.0 - slack) * NEAR_CLOSE_COEF
                else -> 0.0
            }
            prev = node
        }
        return penalty
    }

    private fun selectByRank(ranked: List<Individual>): Individual {
        val n = ranked.size
        val a = Random.nextInt(n)
        val b = Random.nextInt(n)
        val c = Random.nextInt(n)
        return ranked[minOf(a, b, c)]
    }


    private fun crossoverPMX(p1: IntArray, p2: IntArray): IntArray {
        val size = p1.size
        if (size < 2) return p1.copyOf()

        val x = Random.nextInt(size)
        val y = Random.nextInt(size)
        val lo = minOf(x, y)
        val hi = maxOf(x, y)

        val child = IntArray(size) { -1 }

        val segValues = HashSet<Int>((hi - lo + 1) * 2)
        for (k in lo..hi) {
            child[k] = p1[k]
            segValues += p1[k]
        }

        val posInP2 = IntArray(size)
        for (k in 0 until size) posInP2[p2[k]] = k

        for (k in lo..hi) {
            val v = p2[k]
            if (v in segValues) continue

            var pos = k
            while (pos in lo..hi) {
                pos = posInP2[p1[pos]]
            }
            child[pos] = v
        }

        for (k in 0 until size) {
            if (child[k] == -1) child[k] = p2[k]
        }
        return child
    }


    private fun mutate(genes: IntArray): IntArray = when (Random.nextInt(3)) {
        0 -> scramble(genes)
        1 -> displacement(genes)
        else -> inversion(genes)
    }

    private fun scramble(genes: IntArray): IntArray {
        val n = genes.size
        if (n < 3) return genes.copyOf()
        val a = Random.nextInt(n);
        val b = Random.nextInt(n)
        val lo = minOf(a, b);
        val hi = maxOf(a, b)
        val out = genes.copyOf()
        for (i in hi downTo lo + 1) {
            val j = lo + Random.nextInt(i - lo + 1)
            val t = out[i]; out[i] = out[j]; out[j] = t
        }
        return out
    }

    private fun displacement(genes: IntArray): IntArray {
        val n = genes.size
        if (n < 3) return genes.copyOf()
        val a = Random.nextInt(n);
        val b = Random.nextInt(n)
        val lo = minOf(a, b);
        val hi = maxOf(a, b)
        val block = IntArray(hi - lo + 1) { genes[lo + it] }
        val rest = IntArray(n - block.size).also { arr ->
            var w = 0
            for (k in 0 until n) if (k !in lo..hi) {
                arr[w++] = genes[k]
            }
        }
        val insertAt = Random.nextInt(rest.size + 1)
        val out = IntArray(n)
        for (k in 0 until insertAt) out[k] = rest[k]
        for (k in block.indices) out[insertAt + k] = block[k]
        for (k in insertAt until rest.size) out[k + block.size] = rest[k]
        return out
    }

    private fun inversion(genes: IntArray): IntArray {
        val n = genes.size
        if (n < 2) return genes.copyOf()
        val a = Random.nextInt(n);
        val b = Random.nextInt(n)
        val lo = minOf(a, b);
        val hi = maxOf(a, b)
        val out = genes.copyOf()
        var l = lo;
        var r = hi
        while (l < r) {
            val t = out[l]; out[l] = out[r]; out[r] = t
            l++; r--
        }
        return out
    }


    companion object {
        private const val UNREACHABLE_CUTOFF = Double.MAX_VALUE / 2
        private const val PROXY_UNREACHABLE_M = 500_000.0
        private const val UNREACHABLE_PENALTY = 300_000.0
        private const val CLOSED_ARRIVAL_PENALTY = 250_000.0
        private const val NEAR_CLOSE_COEF = 600.0
        private const val STOP_SERVICE_MIN = 3.0
    }
}
