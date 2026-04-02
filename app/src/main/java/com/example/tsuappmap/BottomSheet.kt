package com.example.tsuappmap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TabButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(65.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(red = 0, green = 114, blue = 188),
            contentColor = Color.White
        )
    ) {
        Text(label)
    }
}


@Composable
fun TabContent(selectedTab: Int) {
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
                    onClick = {},
                    modifier = Modifier.width(360.dp).height(70.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 0, green = 0, blue = 188),
                        contentColor = Color.White
                    )
                ) { Text("Кнопка 6") }

                Button(
                    onClick = {},
                    modifier = Modifier.width(360.dp).height(70.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 0, green = 0, blue = 188),
                        contentColor = Color.White
                    )
                ) { Text("Кнопка 10") }

                Button(
                    onClick = {},
                    modifier = Modifier.width(360.dp).height(70.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 0, green = 0, blue = 188),
                        contentColor = Color.White
                    )
                ) { Text("Кнопка 12") }
            }

            2 -> Box(
                modifier = Modifier.fillMaxSize().background(Color(red = 100, green = 100, blue = 100))
            ) { Text("Контент кнопки 2") }

            3 -> Box(
                modifier = Modifier.fillMaxSize().background(Color(red = 100, green = 100, blue = 100))
            ) { Text("Контент кнопки 3") }

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