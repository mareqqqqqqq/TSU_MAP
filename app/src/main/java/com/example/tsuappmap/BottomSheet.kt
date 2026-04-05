package com.example.tsuappmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsuappmap.algorithm.Genetic.GeneticScreen

@Composable
fun TabButton(label: String, onClick: () -> Unit, fontSize: Int) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(65.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(red = 0, green = 114, blue = 188),
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp)

    ) {
        Text(label,
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}


@Composable
fun TabContent(selectedTab: Int,
               isObstacleMode: Boolean,
               onPlaceStart: () -> Unit,
               onPlaceEnd: () -> Unit,
               onToggleObstacle: () -> Unit,
               onReset: () -> Unit
               ) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
    ) {
        when (selectedTab) {
            0, 1 -> Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                Button(
                    onClick = onPlaceStart,
                    modifier = Modifier.width(360.dp).height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 0, green = 0, blue = 188),
                        contentColor = Color.White
                    )
                ) { Text("Стартовая точка") }

                Button(
                    onClick = onPlaceEnd,
                    modifier = Modifier.width(360.dp).height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 0, green = 0, blue = 188),
                        contentColor = Color.White
                    )
                ) { Text("Конечная точка") }

                Button(
                    onClick = onToggleObstacle,
                    modifier = Modifier.width(360.dp).height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 0, green = 0, blue = 188),
                        contentColor = Color.White
                    )
                ) { Text(if (isObstacleMode) "Барьеры: ВКЛ" else "Барьеры: ВЫКЛ") }

                Button(
                    onClick = onReset, modifier = Modifier.width(360.dp).height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor =
                        Color(red = 0, green = 0, blue = 188), contentColor = Color.White)
                ) { Text("Сбросить всё") }
            }
            2 -> Column(
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.width(360.dp).height(65.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 186, green = 184, blue = 97),
                        contentColor = Color.White
                    )
                ) { Text("Поставить или поменять начальную точку") }

                Button(
                    onClick = {},
                    modifier = Modifier.width(360.dp).height(65.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 186, green = 97, blue = 97),
                        contentColor = Color.White
                    )
                ) { Text("Поставить или поменять конечную точку") }
            }

            3 -> GeneticScreen()

            4 -> Box(
                modifier = Modifier.fillMaxSize().background(Color(red = 100, green = 100, blue = 100))
            ) { Text("Контент кнопки 4") }

            5 -> Box(
                modifier = Modifier.fillMaxSize().background(Color(red = 100, green = 100, blue = 100))
            ) { Text("Контент кнопки 5") }

            6 -> Box(
                modifier = Modifier.fillMaxSize().background(Color(red = 100, green = 100, blue = 100))
            ) { Text("Контент кнопки 6") }
        }
    }
}