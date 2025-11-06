package com.example.pdfconverter.dataClasses

import android.graphics.Bitmap
import android.net.Uri

data class ScannedPage(
    val uri: Uri? = null,
    val previewBitmap: Bitmap? = null,
    val fullBitmap: Bitmap? = null
)
