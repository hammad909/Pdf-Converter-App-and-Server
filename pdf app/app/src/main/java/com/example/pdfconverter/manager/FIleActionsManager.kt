package com.example.pdfconverter.manager

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.pdfconverter.dataClasses.ConvertedFiles
import java.io.File

object FileActionsManager {

    fun openFile(context: Context, file: ConvertedFiles) {
        try {
            val openFile = File(file.path)
            if (!openFile.exists()) {
                Toast.makeText(context, "File not found!", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                openFile
            )

            val mimeType = when (openFile.extension.lowercase()) {
                "pdf" -> "application/pdf"
                "doc", "docx" -> "application/msword"
                "txt" -> "text/plain"
                "ppt", "pptx" -> "application/vnd.ms-powerpoint"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "html" -> "text/html"
                else -> "*/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareFile(context: Context, file: ConvertedFiles) {
        try {
            val shareFile = File(file.path)
            if (!shareFile.exists()) {
                Toast.makeText(context, "File not found!", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                shareFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share File via"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing file", Toast.LENGTH_SHORT).show()
        }
    }
}
