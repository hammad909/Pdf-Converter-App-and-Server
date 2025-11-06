package com.example.pdfconverter.helper

import android.content.Context
import android.os.Environment
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import java.io.File

fun saveFileToDownloads(context: Context, desiredName: String, body: ResponseBody): File? {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, desiredName)
        val sink = file.sink().buffer()
        sink.writeAll(body.source())
        sink.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
