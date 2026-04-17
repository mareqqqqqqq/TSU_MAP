package com.example.tsuappmap.algorithm.Genetic

import org.maplibre.android.geometry.LatLng


enum class FoodCategory { MAIN_DISH, SIDE_DISH, DRINK, DESSERT, SUPPLIES }

data class MenuItem(val name: String, val category: FoodCategory)

data class FoodEstablishment(
    val id: Int,
    val name: String,
    val location: LatLng,
    val menu: List<MenuItem>,
    val openHour: Int,
    val openMinute: Int,
    val closeHour: Int,
    val closeMinute: Int
) {
    private val opensAtMin: Int = openHour * 60 + openMinute
    private val closesAtMin: Int = closeHour * 60 + closeMinute

    fun isOpenAt(hour: Int, minute: Int): Boolean {
        val now = hour * 60 + minute
        return now >= opensAtMin && now < closesAtMin
    }

    fun minutesUntilClose(hour: Int, minute: Int): Int = closesAtMin - (hour * 60 + minute)
}


private class MenuBuilder {
    val entries = ArrayList<MenuItem>()
    fun main(n: String) {
        entries += MenuItem(n, FoodCategory.MAIN_DISH)
    }

    fun side(n: String) {
        entries += MenuItem(n, FoodCategory.SIDE_DISH)
    }

    fun drink(n: String) {
        entries += MenuItem(n, FoodCategory.DRINK)
    }

    fun dessert(n: String) {
        entries += MenuItem(n, FoodCategory.DESSERT)
    }

    fun supply(n: String) {
        entries += MenuItem(n, FoodCategory.SUPPLIES)
    }
}

private inline fun spot(
    id: Int,
    title: String,
    lat: Double,
    lon: Double,
    openH: Int,
    openM: Int,
    closeH: Int,
    closeM: Int,
    configure: MenuBuilder.() -> Unit
): FoodEstablishment {
    val mb = MenuBuilder().apply(configure)
    return FoodEstablishment(
        id = id,
        name = title,
        location = LatLng(lat, lon),
        menu = mb.entries.toList(),
        openHour = openH,
        openMinute = openM,
        closeHour = closeH,
        closeMinute = closeM
    )
}


object FoodDatabase {

    val allEstablishments: List<FoodEstablishment> = buildList {
        add(spot(0, "Старбукс", 56.46966067095539, 84.94597563687766, 8, 0, 22, 0) {
            drink("Капучино"); drink("Латте"); drink("Американо")
            dessert("Чизкейк"); dessert("Круассан")
        })

        add(spot(1, "Сибирские блины", 56.46944956796985, 84.94658296061961, 9, 0, 20, 0) {
            main("Блины с мясом"); main("Блины с творогом")
            dessert("Блины с вареньем")
            main("Окрошка"); drink("Чай")
        })

        add(spot(2, "Ростикс", 56.469188722295215, 84.95106026976076, 10, 0, 22, 0) {
            main("Куриные крылья"); main("Бургер")
            side("Картофель фри")
            drink("Кола"); dessert("Мороженое")
        })

        add(spot(3, "Столовая ТГУ", 56.469518721621064, 84.94650550369671, 8, 30, 17, 0) {
            main("Борщ"); main("Пюре с котлетой")
            side("Салат Цезарь")
            drink("Компот"); dessert("Булочка")
        })

        add(spot(4, "Кафе Минутка", 56.46961030124462, 84.94649465755533, 8, 0, 19, 0) {
            main("Пицца"); main("Сосиска в тесте")
            drink("Сок"); dessert("Пирожок")
        })

        add(spot(5, "Ресто Место", 56.47148319104433, 84.95223847096904, 11, 0, 23, 0) {
            main("Стейк"); main("Паста Карбонара")
            side("Греческий салат")
            drink("Лимонад"); dessert("Тирамису")
        })

        add(spot(6, "Сыр Бор", 56.47083084697907, 84.94606609977744, 9, 0, 21, 0) {
            main("Сырники"); main("Вареники"); main("Пельмени")
            drink("Морс")
        })

        add(spot(7, "Батина Шаурма", 56.4711382127202, 84.9415137996349, 10, 0, 23, 0) {
            main("Шаурма классическая"); main("Шаурма острая"); main("Фалафель")
            drink("Айран")
        })

        add(spot(8, "Ярче", 56.473648323118326, 84.94522123015783, 7, 0, 23, 0) {
            main("Сэндвич")
            side("Чипсы")
            drink("Вода")
            dessert("Шоколадка")
            supply("Одноразовая посуда")
        })

        add(spot(9, "Белка Кофе", 56.4714747651078, 84.95016141305494, 8, 0, 21, 0) {
            drink("Эспрессо"); drink("Раф"); drink("Какао")
            dessert("Маффин")
            main("Сэндвич с курицей")
        })

        add(spot(10, "Петрушка", 56.47547096567096, 84.95069066515241, 9, 0, 18, 0) {
            main("Суп дня"); main("Гречка с мясом")
            side("Винегрет")
            drink("Кисель")
        })

        add(spot(11, "NOVA", 56.4755139253109, 84.95034197799288, 11, 0, 22, 0) {
            main("Роллы"); main("Рамен")
            side("Эдамаме")
            drink("Зелёный чай")
            dessert("Моти")
        })

        add(spot(12, "Подкова", 56.47050412464726, 84.93922238973506, 10, 0, 21, 0) {
            main("Плов"); main("Лагман"); main("Самса")
            drink("Чай с молоком")
        })

        add(spot(13, "Пятёрочка", 56.46378649053012, 84.95143071841592, 7, 0, 23, 0) {
            main("Готовая еда (курица)")
            side("Хлеб")
            drink("Молоко")
            dessert("Печенье")
            supply("Одноразовая посуда"); supply("Салфетки")
        })

        add(spot(14, "Бристоль", 56.47370248187285, 84.94513445588653, 8, 0, 22, 0) {
            side("Снеки"); side("Сухарики")
            drink("Энергетик"); drink("Вода газированная")
            supply("Салфетки")
        })
    }

    fun findEstablishmentsForItem(itemName: String): List<FoodEstablishment> {
        val matches = ArrayList<FoodEstablishment>()
        for (est in allEstablishments) {
            if (est.menu.any { it.name == itemName }) matches += est
        }
        return matches
    }
}
