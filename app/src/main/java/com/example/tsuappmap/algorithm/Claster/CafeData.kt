package com.example.tsuappmap.algorithm.Claster

import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.MarkerOptions
import android.graphics.Color
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory


data class Cafe(
    val name: String,
    val location: LatLng
)


object CafeData {
    val defaultCafes = listOf(
        Cafe("Страбукс", LatLng(56.46966067095539, 84.94597563687766)),
        Cafe("Сибирские блины", LatLng(56.46944956796985, 84.94658296061961)),
        Cafe("Ростикс", LatLng(56.469188722295215, 84.95106026976076)),
        Cafe("Столовая тгу", LatLng(56.469518721621064, 84.94650550369671)),
        Cafe("кафе минутка", LatLng(56.46961030124462, 84.94649465755533)),
        Cafe("ресто место", LatLng(56.47148319104433, 84.95223847096904)),
        Cafe("сыр бор", LatLng(56.47083084697907, 84.94606609977744)),
        Cafe("батина шаурма", LatLng(56.4711382127202, 84.9415137996349)),
        Cafe("ярче", LatLng(56.473648323118326, 84.94522123015783)),
        Cafe("белка кофе", LatLng(56.4714747651078, 84.95016141305494)),


        Cafe("петрушка", LatLng(56.47547096567096, 84.95069066515241)),
        Cafe("NOVA", LatLng(56.4755139253109, 84.95034197799288)),
        Cafe("Mindaйк кофе", LatLng(56.47548281661097, 84.950537779244)),
        Cafe("Harat's Pub", LatLng(56.47354888429167, 84.94617443409479)),
        Cafe("Ярче!", LatLng(56.47414449570845, 84.94449896564056)),
        Cafe("Бристоль", LatLng(56.47370248187285, 84.94513445588653)),
        Cafe("Подкова", LatLng(56.47050412464726, 84.93922238973506)),
        Cafe("Шашлычный дом", LatLng(56.46417336821341, 84.94013731376342)),
        Cafe("Пятерочка", LatLng(56.46378649053012, 84.95143071841592)),


        Cafe("Сыр бор ", LatLng(56.47084181557067, 84.9461442043919)),
        Cafe("NOVA", LatLng(56.4755139253109, 84.95034197799288)),
        Cafe("Экспресс", LatLng(56.470170451498646, 84.94261018799517)),
        Cafe("Мини-Микс", LatLng(56.46466909509066, 84.94878320889117)),
        Cafe("Научка", LatLng(56.46771578238479, 84.94990984578615)),
    )

    fun getAllCafes(): List<Cafe> = defaultCafes

    fun showAllCafesOnMap(map: MapLibreMap) {
        defaultCafes.forEach { cafe ->
            map.addMarker(
                MarkerOptions()
                    .position(cafe.location)
                    .title(cafe.name)
                    .setSnippet("Заведение общепита")
            )
        }
        Log.d("CafeData", "Отображено ${defaultCafes.size} кафе")
    }

    private val CLUSTERS_COLORS = listOf(
        Color.argb(220, 255, 80, 80),
        Color.argb(220, 80, 160, 255),
        Color.argb(220, 80, 220, 100),
        Color.argb(220, 255, 200, 0),
        Color.argb(220, 220, 80, 255)
    )

    fun showClusterOnMap(map: MapLibreMap, k: Int = 3, context: Context) {
        val cafes = defaultCafes
        val results = Kmeans().cluster(cafes, k)

        results.forEach { result ->
            val color = CLUSTERS_COLORS[result.clusterId % CLUSTERS_COLORS.size]
            val icon = createColoredIcon(context, color)

            map.addMarker(
                MarkerOptions()
                    .position(result.cafe.location)
                    .title(result.cafe.name)
                    .setSnippet("Класстер ${result.clusterId + 1}")
                    .icon(icon)
            )
        }
    }

    fun showClustersManhattanOnMap(map: MapLibreMap, k: Int = 3, context: Context) {
        val results = KMeansManhattan().cluster(defaultCafes, k)
        results.forEach { result ->
            val color = CLUSTERS_COLORS[result.clusterId % CLUSTERS_COLORS.size]
            val icon = createColoredIcon(context, color)
            map.addMarker(
                MarkerOptions()
                    .position(result.cafe.location)
                    .title(result.cafe.name)
                    .setSnippet("Кластер (manhattan) ${result.clusterId + 1}")
                    .icon(icon)
            )
        }
    }

    private fun createColoredIcon(context: Context, color: Int): Icon {
        val size = 64
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = Color.argb(60, 0, 0, 0)
        canvas.drawCircle(size / 2f + 2f, size / 2f + 2f, size / 2f - 4f, paint)

        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    fun getAllLocations(): List<LatLng> = defaultCafes.map { it.location }
}