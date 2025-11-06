package com.example.pdfconverter.dataClasses

data class UploadResponse(
    val message: String,
    val file: String,
    val download_url: String
)
