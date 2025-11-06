package com.example.pdfconverter.helper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoHelperDialog(showInfo: MutableState<Boolean>,title: String,text : String) {
    if (showInfo.value) {
        AlertDialog(
            onDismissRequest = { showInfo.value = false },
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            },
            text = {
                Column {
                   Text(text = buildAnnotatedString {
                   withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,
                       color = Color.Black)){
                       append("Info : ")
                   }
                   append(text)})
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo.value = false }) {
                    Text("Got it", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

