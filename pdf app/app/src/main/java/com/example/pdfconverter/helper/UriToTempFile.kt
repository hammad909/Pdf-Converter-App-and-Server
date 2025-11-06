package com.example.pdfconverter.helper

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun uriToTempFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalExtension = DocumentFile.fromSingleUri(context, uri)?.name
            ?.substringAfterLast('.', "") ?: "tmp"
        val tempFile = File.createTempFile(fileName, ".$originalExtension", context.cacheDir)

        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

