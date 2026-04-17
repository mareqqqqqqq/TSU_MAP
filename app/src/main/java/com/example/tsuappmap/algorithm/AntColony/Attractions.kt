package com.example.tsuappmap.algorithm.AntColony

data class PointOfAttractions(
    val name: String,
    val lat: Double,
    val lon: Double
)

object Attractions
{
    val allPoint = listOf(
        PointOfAttractions("Памятник Флоринскому и Менделееву", 56.46923558144582, 84.9487508449349),
        PointOfAttractions("Памятник Г.Н. Потанину", 56.468822591801, 84.94973303250951),
        PointOfAttractions("Камень — геофизический центр Азии", 56.46948136876713, 84.9499735383008),
        PointOfAttractions("Главный корпус ТГУ", 56.469495396469895, 84.948090391001),
        PointOfAttractions("Памятник павшим за Родину", 56.46869469776115, 84.94930878003805),
        PointOfAttractions("Озеро ТГУ", 56.469297294130385, 84.94280281527605),
        PointOfAttractions("Университетская Роща", 56.469758219197345, 84.94893434560394),
        PointOfAttractions("Сибирский Ботанический Сад", 56.466526611751426, 84.94667467592014),
        PointOfAttractions("Каменные бабы", 56.469160, 84.948218),
        PointOfAttractions("Профессор Белкин (белка)", 56.469837, 84.948352),
        PointOfAttractions("Каменные изваяния", 56.469865, 84.948183),
        PointOfAttractions("Мост через реку Медичку", 56.471216, 84.948979),
        PointOfAttractions("Шахматы и Библиотека ТГУ", 56.467967, 84.949470)
    )
}