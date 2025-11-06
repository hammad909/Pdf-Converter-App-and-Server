package com.example.pdfconverter.helper

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import android.webkit.MimeTypeMap


fun createFileFromUri(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
        ?: throw IllegalArgumentException("Cannot open input stream from URI")

    val extension = getFileExtension(context, uri) ?: ".tmp"

    val tempFile = File.createTempFile("processed_", extension, context.cacheDir)
    val outputStream = FileOutputStream(tempFile)

    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

    return tempFile
}

fun getFileExtension(context: Context, uri: Uri): String? {
    val mimeType = context.contentResolver.getType(uri)
    if (mimeType != null) {
        val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        if (ext != null) return ".$ext"
    }

    val path = uri.path ?: return null
    val dotIndex = path.lastIndexOf('.')
    return if (dotIndex != -1) path.substring(dotIndex) else null
}
