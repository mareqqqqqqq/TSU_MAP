package com.example.tsuappmap
import kotlin.math.log2



data class Sample(
    val features: Map<String, String>,
    val label: String
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
    val lines = csv.trim().lines().filter { it.isNotBlank() }
    require(lines.size >= 2) { "CSV должен содежать заголовок и хотя бы одну строку" }

    val headers = lines[0].split(",").map { it.trim() }
    val featureNames = headers.dropLast(1)
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

fun predict(tree: TreeNode, ans: Map<String, String>): String {
    var node: TreeNode = tree
    while (node is TreeNode.Decision) {
        var answer = ans[node.feature] ?:return "Нет даных"
        node = node.branches[answer] ?: return "Нет данных"
    }

    return (node as TreeNode.Leaf).label
}

fun getFeatureValues(samples: List<Sample>, feature: String): List<String> =
    samples.mapNotNull {it.features[feature] }.distinct().sorted()


fun entropy(samples: List<Sample>): Double {
    if (samples.isEmpty()) return 0.0
    val total = samples.size.toDouble()

    return samples
        .groupBy { it.label }
        .values
        .sumOf {
            group ->
            val p = group.size / total
            -p * log2(p)
        }
}

fun informationGain(samples: List<Sample>, feature: String) : Double {
    val total = samples.size.toDouble()
    val parentEntropy = entropy(samples)

    val weightedChildEntropy = samples
        .groupBy { it.features[feature] ?: "?" }
        .values
        .sumOf { subset -> (subset.size / total) * entropy(subset) }


    return parentEntropy - weightedChildEntropy
}


private fun majorityLabel(samples: List<Sample>) : String =
    samples.groupBy { it.label }
        .maxByOrNull { it.value.size }!!
        .key

fun buildTree(samples: List<Sample>, features: List<String>) : TreeNode {
    val uniqueLabels = samples.map { it.label }.distinct()
    if (uniqueLabels.size == 1) {
        return TreeNode.Leaf(uniqueLabels[0])
    }

    if (features.isEmpty()) {
        return TreeNode.Leaf(majorityLabel(samples))
    }

    val bestFeature = features.maxByOrNull {
        informationGain(samples, it)
    } !!

    if (informationGain(samples, bestFeature) <= 0.0) {
        return TreeNode.Leaf(majorityLabel(samples))
    }

    val remainingFeatures= features - bestFeature

    val branches = samples
        .groupBy { it.features[bestFeature] ?: "?"}
        .mapValues { (_, subset) -> buildTree(subset, remainingFeatures)}

    return TreeNode.Decision(bestFeature, branches)
}

val DEFAULT_CSV = """
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
""".trimIndent()

fun main() {
    val (samples, features) = parseCSV(DEFAULT_CSV)
    val tree = buildTree(samples, features)

    val answers = mapOf(
        "location" to "main_building",
        "budget" to "low",
        "time_available" to "short",
        "food_type" to "snack",
        "queue_tolerance" to "low",
        "weather" to "good"
    )

    val result = predict(tree, answers)
}

