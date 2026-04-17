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
        Cafe("Starbooks", LatLng(56.46961703158033, 84.94639134100332)),
        Cafe("Сибирские блины", LatLng(56.46390145061441, 84.95145963538971)),
        Cafe("Ростикс", LatLng(56.469188722295215, 84.95106026976076)),
        Cafe("Столовая ТГУ", LatLng(56.46689696900109, 84.95294573365794)),
        Cafe("Минутка", LatLng(56.46962173535437, 84.94674047576264)),
        Cafe("Ресто место", LatLng(56.47148319104433, 84.95223847096904)),
        Cafe("Сыр-Бор", LatLng(56.47084181557067, 84.9461442043919)),
        Cafe("Батин шаурма", LatLng(56.4711382127202, 84.9415137996349)),
        Cafe("Ярче", LatLng(56.473648323118326, 84.94522123015783)),
        Cafe("Белка кофе", LatLng(56.4714747651078, 84.95016141305494)),
        Cafe("Петрушка", LatLng(56.47547096567096, 84.95069066515241)),
        Cafe("Nova", LatLng(56.4755139253109, 84.95034197799288)),
        Cafe("Сладкоежка", LatLng(56.47548281661097, 84.950537779244)),
        Cafe("Mindайк Coffee", LatLng(56.47550681300926, 84.95126870760889)),
        Cafe("Harat's Pub", LatLng(56.47354888429167, 84.94617443409479)),
        Cafe("Olivka Lounge", LatLng(56.473441614938864, 84.94572584751025)),
        Cafe("Укромное местечко", LatLng(56.47238458626514, 84.94856320172501)),
        Cafe("Бристоль", LatLng(56.47370248187285, 84.94513445588653)),
        Cafe("Кафе-пекарня У мамы", LatLng(56.47337291507802, 84.9436358644174)),
        Cafe("Рюмки на стол", LatLng(56.4733441956898, 84.94457019940316)),
        Cafe("Магнолия", LatLng(56.47313382910305, 84.9445286251649)),
        Cafe("Mir Piva", LatLng(56.473977392888806, 84.94413826647619)),
        Cafe("Абрикос", LatLng(56.47145127675623, 84.94098864083306)),
        Cafe("Подкова", LatLng(56.47050412464726, 84.93922238973506)),
        Cafe("MushRooms", LatLng(56.46890443266459, 84.94071099324478)),
        Cafe("Шашлычный дом", LatLng(56.46417336821341, 84.94013731376342)),
        Cafe("Мария Ра", LatLng(56.463881479255676, 84.94052509718186)),
        Cafe("Krüger Haus", LatLng(56.462904368548664, 84.94039753563118)),
        Cafe("КОФЕ ТАЙМ", LatLng(56.46428940891372, 84.93572167328668)),
        Cafe("FixPrice", LatLng(56.46057785538854, 84.93858538318678)),
        Cafe("Дубрава", LatLng(56.459179258102225, 84.94599938008342)),
        Cafe("Магнит", LatLng(56.458733030333796, 84.94693609721611)),
        Cafe("Мир напитков", LatLng(56.45865225446246, 84.94731839272028)),
        Cafe("Пенный", LatLng(56.45917085851975, 84.94975092485159)),
        Cafe("Калина-Малина", LatLng(56.4595968131771, 84.95070993133234)),
        Cafe("Корона", LatLng(56.459201205251105, 84.95316900102277)),
        Cafe("Мир Суши", LatLng(56.4592811912635, 84.95230893738288)),
        Cafe("Додо Пицца", LatLng(56.461865909969184, 84.95069191640079)),
        Cafe("Чайная Обитель", LatLng(56.46083923122651, 84.95066913138646)),
        Cafe("Шавуху Хочу", LatLng(56.460892384212876, 84.95008684776344)),
        Cafe("Ош", LatLng(56.46049950338836, 84.94611081186356)),
        Cafe("Soupculture", LatLng(56.46394316858799, 84.95304540510185)),
        Cafe("Цветная Шаурма", LatLng(56.463796248564435, 84.95299944076237)),
        Cafe("TopDogger", LatLng(56.46388875383072, 84.95196852629113)),
        Cafe("Вечер", LatLng(56.46482734153298, 84.95344174109655)),
        Cafe("Кудесы", LatLng(56.46479021777663, 84.95405838231304)),
        Cafe("Научка", LatLng(56.46771578238479, 84.94990984578615)),
        Cafe("Экспресс Кафе №2", LatLng(56.46879856601472, 84.94539502958081)),
        Cafe("Экспресс", LatLng(56.470170451498646, 84.94261018799517)),
        Cafe("Мини-Микс", LatLng(56.46466909509066, 84.94878320889117))
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
        Log.d("CafeData","Отображено ${defaultCafes.size} кафе")
    }

    private val CLUSTERS_COLORS = listOf(
        Color.argb(220, 255, 80, 80),
        Color.argb(220, 80, 160, 255),
        Color.argb(220, 80, 220, 100),
        Color.argb(220, 255, 200, 0),
        Color.argb(220, 220, 80, 255),
        Color.argb(220, 255, 128, 0),
        Color.argb(220, 255, 20, 147),
        Color.argb(220, 0, 255, 255),
        Color.argb(220, 255, 105, 180),
        Color.argb(220, 128, 0, 128),
        Color.argb(220, 0, 255, 127),
        Color.argb(220, 255, 69, 0),
        Color.argb(220, 75, 0, 130),
        Color.argb(220, 255, 215, 0),
        Color.argb(220, 0, 191, 255)
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
    //dfdfdfdf

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
        canvas.drawCircle(size / 2f, size/ 2f, size / 2f - 4f, paint)

        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, paint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    fun getAllLocations(): List<LatLng> = defaultCafes.map { it.location }
}