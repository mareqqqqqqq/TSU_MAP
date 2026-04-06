package com.example.tsuappmap
import kotlin.math.log2


// features - признаки location, budget, timop
// label -
// sample - пример
data class Sample(
    val features: Map<String, String>, // словарь
    val labels: String // Yarche
)

sealed class TreeNode {
    data class Decision(
        val feature: String,
        val branches: Map<String, TreeNode>
    ) : TreeNode()

    data class Leaf(
        val label: String,
    ) : TreeNode()
}

fun parseCSV(csv: String) : Pair<List<Sample>, List<String>> {
    val lines = csv.trim().lines().filter { it.isNotBlank() } // разбивает на массив строк
    require(lines.size >= 2) { "CSV должен содежать заголовок и хотя бы одну строку" }

    val headers = lines[0].split(",").map { it.trim() }
    val featureNames = headers.dropLast(1) // удалит последний, там у нас ответ, wether location итд
    val targetName = headers.last()

    val samples = lines.drop(1).mapIndexed { idx, line ->
        val values = line.split(",").map { it.trim() }
        require(values.size == headers.size) {
            "Что то не то"
        }

        val features = featureNames.zip(values.dropLast(1)).toMap()
        Sample(features, values.last())
    }

    return Pair(samples, featureNames)
}

fun entropy(samples: List<Sample>): Double {
    if (samples.isEmpty()) return 0.0
    val total = samples.size.toDouble() // сохраним количество примеров как double

    return samples
        .groupBy { it.labels }
        .values
        .sumOf {
            group ->
            val p = group.size / total
            -p * log2(p)
        }
}











fun main() {
    val csv = """
        location,budget,time_available,food_type,queue_tolerance,weather,recommended_place
        main_building,low,medium,full_meal,medium,good,Main_Cafeteria
        main_building,low,short,snack,low,good,Yarche
        main_building,medium,short,coffee,low,good,Bus_Stop_Coffee
        main_building,high,medium,coffee,medium,good,Starbooks
        second_building,low,very_short,snack,low,good,Vending_Machine
        second_building,medium,short,coffee,medium,good,Second_Building_Cafe
        second_building,medium,medium,full_meal,medium,good,Main_Cafeteria
        second_building,low,short,snack,low,bad,Vending_Machine
        campus_center,medium,short,pancakes,medium,good,Siberian_Pancakes
    """.trimIndent() // убирает лишние отступы



}

