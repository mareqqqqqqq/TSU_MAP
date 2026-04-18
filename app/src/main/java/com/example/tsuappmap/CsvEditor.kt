package com.example.tsuappmap

//import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CsvEditorContent(
    currentCsv: String, onSave: (String) -> Unit, onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(currentCsv) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Редактирование обучающей выборки",
            color = TsuDark,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            "Первая строка — заголовок. Последний столбец — целевой признак.",
            color = Color.Gray,
            fontSize = 12.sp
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it; errorMsg = null },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 11.sp, fontFamily = FontFamily.Monospace
            ),
            placeholder = { Text("Введите CSV...", fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TsuBlue, unfocusedBorderColor = TsuBlue.copy(alpha = 0.4f)
            )
        )

        if (errorMsg != null) {
            Text(errorMsg!!, color = TsuWarn, fontSize = 12.sp)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { text = DEFAULT_CSV; errorMsg = null },
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
            ) {
                Text("Сбросить", color = Color.Gray, fontSize = 13.sp)
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TsuBlue)
            ) {
                Text("Отмена", color = TsuBlue, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    try {
                        parseCSV(text)
                        onSave(text)
                    } catch (e: Exception) {
                        errorMsg = "Ошибка: ${e.message}"
                    }
                },
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TsuBlue)
            ) {
                Text(
                    "Сохранить", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
            }
        }
    }
}