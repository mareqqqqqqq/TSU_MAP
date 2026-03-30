package com.example.tsuappmap
import org.maplibre.android.geometry.LatLng

// data  - это модификатор которые добавляет полезные методы
data class Cafe(
    val name: String,
    val location: LatLng,
    val isUserAdded: Boolean = false
)


object CafeData {
    val defaultCafes = listOf(
        Cafe("Страбукс", LatLng(56.46966067095539, 84.94597563687766)),
        Cafe("Сибирские блины", LatLng(56.46944956796985, 84.94658296061961)),
        Cafe("Ростикс", LatLng(56.469188722295215, 84.95106026976076))
    )

    fun getAllCafes(): List<Cafe> = defaultCafes
}