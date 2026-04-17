package com.example.tsuappmap.algorithm.NeuralNetwork

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DigitDrawContent() {
    val context = LocalContext.current
    val gridSize = 50
    val brushRadius = 1

    val pixels = remember { Array(gridSize) { BooleanArray(gridSize) } }

    var redrawTrigger by remember { mutableStateOf(0) }

    var resultText by remember { mutableStateOf("Оцени заведение ") }

    val classifier = remember { NnClassifier(context) }

    val weightsLoaded = remember { classifier.loadWeights() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = resultText,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color(red = 0, green = 114, blue = 188))
                .pointerInput(Unit)
                {
                    detectTapGestures { offset ->
                        val cellW = size.width.toFloat() / gridSize
                        val cellH = size.height.toFloat() / gridSize
                        val col = (offset.x / cellW).toInt().coerceIn(0, gridSize - 1)
                        val row = (offset.y / cellH).toInt().coerceIn(0, gridSize - 1)

                        for (dr in -brushRadius..brushRadius) {
                            for (dc in -brushRadius..brushRadius) {
                                val r = row + dr
                                val c = col + dc
                                if (r in 0 until gridSize && c in 0 until gridSize) {
                                    pixels[r][c] = true
                                }
                            }
                        }
                        redrawTrigger++
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val cellW = size.width.toFloat() / gridSize
                        val cellH = size.height.toFloat() / gridSize
                        val col = (change.position.x / cellW).toInt().coerceIn(0, gridSize - 1)
                        val row = (change.position.y / cellH).toInt().coerceIn(0, gridSize - 1)
                        var changed = false
                        for (dr in -brushRadius..brushRadius) {
                            for (dc in -brushRadius..brushRadius) {
                                val r = row + dr
                                val c = col + dc
                                if (r in 0 until gridSize && c in 0 until gridSize) {
                                    if (!pixels[r][c]) {
                                        pixels[r][c] = true
                                        changed = true
                                    }
                                }
                            }
                        }
                        if (changed) redrawTrigger++
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                redrawTrigger.let { _ ->
                    val cellW = size.width / gridSize
                    val cellH = size.height / gridSize

                    for (row in 0 until gridSize) {
                        for (col in 0 until gridSize) {
                            if (pixels[row][col]) {
                                drawRect(
                                    color = Color.White,
                                    topLeft = Offset(col * cellW, row * cellH),
                                    size = Size(cellW, cellH)
                                )
                            }
                        }
                    }

                    val gridColor = Color(0xFF333333)
                    for (i in 0..gridSize) {
                        val strokeW = if (i % 5 == 0) 1.5f else 0.5f
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, i * cellH),
                            end = Offset(size.width, i * cellH),
                            strokeWidth = strokeW
                        )
                    }
                    for (i in 0..gridSize) {
                        val strokeW = if (i % 5 == 0) 1.5f else 0.5f
                        drawLine(
                            color = gridColor,
                            start = Offset(i * cellW, 0f),
                            end = Offset(i * cellW, size.height),
                            strokeWidth = strokeW
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val input = FloatArray(gridSize * gridSize)
                    for (row in 0 until gridSize) {
                        for (col in 0 until gridSize) {
                            input[row * gridSize + col] = if (pixels[row][col]) 1.0f else 0.0f
                        }
                    }

                    val (digit, confidence) = classifier.classifyWithConfidence(input)
                    val percent = (confidence * 100).toInt()
                    resultText = if (digit >= 0) {
                        "Цифра: $digit  (уверенность: $percent%)"
                    }
                    else {
                        "Не удалось распознать"
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(red = 0, green = 114, blue = 188),
                    contentColor = Color.White
                )
            ) { Text("Распознать") }

            Button(
                onClick = {
                    for (row in 0 until gridSize) {
                        for (col in 0 until gridSize) {
                            pixels[row][col] = false
                        }
                    }
                    redrawTrigger++
                    resultText = "Нарисуй цифру"
                },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(red = 120, green = 120, blue = 120),
                    contentColor = Color.White
                )
            ) { Text("Очистить") }
        }
    }
}

@Composable
fun DigitDrawFullScreen(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = onClose,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 120, green = 120, blue = 120),
                        contentColor = Color.White
                    )
                ) {
                    Text("Назад к карте")
                }
            }
            DigitDrawContent()
        }
    }
}
