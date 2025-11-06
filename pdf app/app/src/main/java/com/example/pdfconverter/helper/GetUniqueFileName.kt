package com.example.pdfconverter.helper

import java.io.File

fun getUniqueFileName(directory: File, baseName: String, extension: String = "pdf"): File {
    var file = File(directory, "$baseName.$extension")
    var index = 1
    while (file.exists()) {
        file = File(directory, "$baseName($index).$extension")
        index++
    }
    return file
}
