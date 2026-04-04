package com.example.tsuappmap.algorithm.Genetic

import com.example.tsuappmap.Cafe
import com.example.tsuappmap.CafeData
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

object GeneticAlgorithm {
    private const val WALK_SPEED_M_PER_MIN = 83.3

    val cafeMenu: Map<String, List<String>> = mapOf(
        "Старбукс" to listOf("Кофе"),
        "Сибирские блины" to listOf("Кофе"),
        "Ростикс" to listOf("Кофе"),
        "Столовая тгу" to listOf("Кофе"),
        "кафе минутка" to listOf("Кофе"),
        "ресто место" to listOf("Кофе"),
        "сыр бор" to listOf("Кофе"),
        "батина шаурма" to listOf("Кофе"),
        "ярче" to listOf("Кофе"),
        "белка кофе" to listOf("Кофе"),
        "петрушка" to listOf("Кофе"),
        "NOVA" to listOf("Кофе"),
        "Mindaйк кофе" to listOf("Кофе"),
        "Harat's Pub" to listOf("Кофе"),
        "Бристоль" to listOf("Кофе"),
        "Подкова" to listOf("Кофе"),
        "Шашлычный дом" to listOf("Кофе"),
        "Пятерочка" to listOf("Кофе"),
        "Экспресс" to listOf("Кофе"),
        "Мини-Микс" to listOf("Кофе"),
        "Научка" to listOf("Кофе"),
    )

    val allMenuItems: List<String> = cafeMenu.values.flatten().distinct().sorted()

    data class Individual (
        val route: List<Cafe>,
        val fitness: Double
    )

    fun run(
        userLat: Double,
        userLon: Double,
        selectedItems: List<String>,
        generations: Int = 150,
        popSize: Int = 40,
        onGeneration: (gen: Int, best: Individual) -> Unit = {_, _ ->}
    ): Individual {

        val needed = findNeededCafes(selectedItems)

        if (needed.isEmpty()) {
            return Individual(emptyList(), 0.0)
        }

        if (needed.size == 1) {
            val ind = Individual(needed, calcFitness(needed, userLat, userLon))
            onGeneration(1, ind)
            return ind
        }

        var population = (0 until popSize).map {
            makeIndividual(needed.shuffled(), userLat, userLon)
        }

        var best = population.maxBy { it.fitness }
        onGeneration(0, best)

        repeat(generations) { gen ->
            val next = mutableListOf(best)

            while (next.size < popSize) {
                val p1 = tournamentSelect(population)
                val p2 = tournamentSelect(population)
                var child = orderedCrossover(p1, p2, userLat, userLon)
                if (Random.nextDouble() < 0.15) {
                    child = swapMutate(child, userLat, userLon)
                }
                next.add(child)
            }

            population = next
            val genBest = population.maxBy { it.fitness }
            if (genBest.fitness > best.fitness) best = genBest
            onGeneration(gen + 1, best)
        }

        return best
    }

    fun findNeededCafes(selectedItems: List<String>): List<Cafe> {
        val remaining = selectedItems.toMutableList()
        val result = mutableListOf<Cafe>()
        val allCafes = CafeData.getAllCafes()
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)

        while (remaining.isNotEmpty()) {
            val best = allCafes.filter { cafe ->  val menu = cafeMenu[cafe.name] ?: return@filter false
            menu.any {it in remaining}
            }
        .maxByOrNull { cafe ->
            cafeMenu[cafe.name]?.count { it in remaining } ?: 0
            } ?: break

            val  covered = cafeMenu[best.name]?.filter { it in remaining } ?: break
            if (covered.isEmpty()) break
            result.add(best)
            remaining.removeAll(covered.toSet())
        }

        return result
    }

    private fun calcFitness(route: List<Cafe>, uLat: Double, uLon: Double): Double {
        if (route.isEmpty()) return 0.0
        var totalMin = 0.0
        var prevLat = uLat
        var prevLon = uLon
        for (cafe in route) {
            totalMin += metersToMinutes (
                euclideanMeters(prevLat, prevLon, cafe.location.latitude, cafe.location.longitude)
            )
            prevLat = cafe.location.latitude
            prevLon = cafe.location.longitude
        }
        return if (totalMin > 0) 1.0 / totalMin else Double.MAX_VALUE
    }

    fun metersToMinutes(m: Double): Double = m / WALK_SPEED_M_PER_MIN

    fun euclideanMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = (lat2 - lat1) * 111320.0
        val dLon = (lon2 - lon1) * 111320.0 * cos(Math.toRadians(lat1))
        return sqrt(dLat + dLat + dLon * dLon)
    }

    private fun makeIndividual(route: List<Cafe>, uLat: Double, uLon: Double) =
        Individual(route, calcFitness(route, uLat, uLon))

    private fun tournamentSelect(pop: List<Individual>): Individual = (0..2).map { pop.random() }.maxBy { it.fitness }

    private fun orderedCrossover(
        p1: Individual, p2: Individual,
        uLat: Double, uLon: Double
    ): Individual {
        val size = p1.route.size
        if (size <= 1) return p1
        val start = Random.nextInt(size)
        val end = Random.nextInt(start, size)
        val segment = p1.route.subList(start, end + 1)
        val rest = p2.route.filter { it !in segment }
        val child = (rest.take(start) + segment + rest.drop(start)).take(size)
        return makeIndividual(child, uLat, uLon)
    }

    private fun swapMutate(ind: Individual, uLat: Double, uLon: Double): Individual {
        if (ind.route.size <= 1) return ind
        val r = ind.route.toMutableList()
        val i = Random.nextInt(r.size)
        val j = Random.nextInt(r.size)
        val t = r[i]; r[i] = r[j]; r[j] = t
        return makeIndividual(r, uLat, uLon)
    }
}