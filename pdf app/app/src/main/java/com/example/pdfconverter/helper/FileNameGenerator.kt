package com.example.pdfconverter.helper

import android.os.Environment
import java.io.File


fun generateFileName(): String {
    val baseName = "document_no_"
    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    if (!downloadsFolder.exists()) {
        downloadsFolder.mkdirs()
    }

    var index = 1
    var fileName: String

    do {
        fileName = "$baseName$index.pdf"
        val file = File(downloadsFolder, fileName)
        if (!file.exists()) break
        index++
    } while (true)

    return fileName
}
