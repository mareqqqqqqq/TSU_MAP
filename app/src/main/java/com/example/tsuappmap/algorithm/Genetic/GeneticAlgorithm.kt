package com.example.tsuappmap.algorithm.Genetic

import androidx.compose.material3.Card
import com.example.tsuappmap.Cafe
import com.example.tsuappmap.CafeData
import java.util.Calendar
import kotlin.math.*
import kotlin.random.Random

object GeneticAlgorithm {
    private const val WALK_SPEED_M_PER_MIN = 83.3

    val cafeInfo: Map<String, CafeInfo> = mapOf(
        "Старбукс" to CafeInfo(listOf("Кофе, Пиво"), 8, 20),
        "Сибирские блины" to CafeInfo(listOf("Кофе, Булочки"), 8, 20),
        "Ростикс" to CafeInfo(listOf("Кофе, Молоко"), 8, 20),
        "Столовая тгу" to CafeInfo(listOf("Кофе"), 8, 20),
        "кафе минутка" to CafeInfo(listOf("Кофе"), 8, 20),
        "ресто место" to CafeInfo(listOf("Кофе"), 8, 20),
        "сыр бор" to CafeInfo(listOf("Кофе"), 8, 20),
        "батина шаурма" to CafeInfo(listOf("Кофе"), 8, 20),
        "ярче" to CafeInfo(listOf("Кофе"), 8, 20),
        "белка кофе" to CafeInfo(listOf("Кофе"), 8, 20),
        "петрушка" to CafeInfo(listOf("Кофе"), 8, 20),
        "NOVA" to CafeInfo(listOf("Кофе"), 8, 20),
        "Mindaйк кофе" to CafeInfo(listOf("Кофе"), 8, 20),
        "Harat's Pub" to CafeInfo(listOf("Кофе"), 8, 20),
        "Бристоль" to CafeInfo(listOf("Кофе"), 8, 20),
        "Подкова" to CafeInfo(listOf("Кофе"), 8, 20),
        "Шашлычный дом" to CafeInfo(listOf("Кофе"), 8, 20),
        "Пятерочка" to CafeInfo(listOf("Кофе"), 8, 20),
        "Экспресс" to CafeInfo(listOf("Кофе"), 8, 20),
        "Мини-Микс" to CafeInfo(listOf("Кофе"), 8, 20),
        "Научка" to CafeInfo(listOf("Кофе"), 8, 20),
    )

    data class CafeInfo(
        val menu: List<String>,
        val openHour: Int,
        val closeHour: Int
    )

    val cafeMenu: Map<String, List<String>> get() = cafeInfo.mapValues { it.value.menu}
    val allMenuItems: List<String> = cafeInfo.values.flatMap{ it.menu }.distinct().sorted()

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
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        while (remaining.isNotEmpty()) {
            val best = allCafes.filter { cafe ->  val info = cafeInfo[cafe.name] ?: return@filter false
                val isOpen = currentHour >= info.openHour && currentHour < info.closeHour
                isOpen && info.menu.any { it in remaining }
            }
        .maxByOrNull { cafe ->
            val info = cafeInfo[cafe.name] ?: return@maxByOrNull 0
            val count = info.menu.count{ it in remaining }
            val closingSoon = (info.closeHour - currentHour) <= 2
            if (closingSoon) count + 100 else count
            } ?: break

            val info = cafeInfo[best.name] ?: break
            val  covered = info.menu.filter { it in remaining } ?: break
            if (covered.isEmpty()) break
            result.add(best)
            remaining.removeAll(covered.toSet())
        }
        return result
    }

    private fun calcFitness(route: List<Cafe>, uLat: Double, uLon: Double): Double {
        if (route.isEmpty()) return 0.0
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        var totalMin = 0.0
        var prevLat = uLat
        var prevLon = uLon

        for (cafe in route) {
            totalMin += metersToMinutes (
                euclideanMeters(prevLat, prevLon, cafe.location.latitude, cafe.location.longitude)
            )

            val info = cafeInfo[cafe.name]
            if (info != null) {
                val minutesToClose = (info.closeHour - currentHour) * 60.0
                if (minutesToClose in 0.0..60.0) {
                    totalMin += (60.0 - minutesToClose) * 3
                }
            }
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