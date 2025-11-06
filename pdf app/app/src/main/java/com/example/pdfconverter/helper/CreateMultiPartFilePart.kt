package com.example.pdfconverter.helper

import com.example.pdfconverter.apiInstance.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

fun createMultipartFilePart(pdfBytes: ByteArray, fileName: String): MultipartBody.Part {
    val requestBody = RequestBody.create("application/pdf".toMediaTypeOrNull(), pdfBytes)
    return MultipartBody.Part.createFormData("file", fileName, requestBody)
}

suspend fun uploadMergedPdfFiles(
    code: String?,
    pdfBytes: ByteArray,
    fileName: String = "merged.pdf"
): Boolean {
    return try {
        val filePart = createMultipartFilePart(pdfBytes, fileName)
        val response = RetrofitClient.apiServiceWeb.uploadProcessedFile(code, filePart)
        response.isSuccessful
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}