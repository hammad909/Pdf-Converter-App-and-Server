package com.example.pdfconverter.dataClasses

import androidx.compose.ui.graphics.Color

data class TempGroup(
    val name: String,
    val color: Color = Color.LightGray,
    val selectedPages: MutableSet<Int> = mutableSetOf(),
    var range: String = ""
)