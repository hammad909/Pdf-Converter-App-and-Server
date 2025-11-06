package com.example.pdfconverter.utills

import androidx.annotation.RequiresApi
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.pdfconverter.dataClasses.SplitPageData
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PdfMerger
import java.io.ByteArrayOutputStream



object PdfUtil {

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun mergePdfsWithIText(context: Context, uris: List<Uri>, outputFileName: String): Uri? {
        return withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val downloadsUri = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val outputUri = resolver.insert(downloadsUri, values)

            outputUri?.let { uri ->
                resolver.openOutputStream(uri)?.use { outputStream ->
                    val pdfWriter = PdfWriter(outputStream)
                    val mergedPdfDoc = PdfDocument(pdfWriter)
                    val merger = PdfMerger(mergedPdfDoc)

                    for (pdfUri in uris) {
                        resolver.openInputStream(pdfUri)?.use { inputStream ->
                            val reader = PdfReader(inputStream)
                            val pdfDoc = PdfDocument(reader)
                            merger.merge(pdfDoc, 1, pdfDoc.numberOfPages)
                            pdfDoc.close()
                        }
                    }

                    mergedPdfDoc.close()
                }

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                uri
            }
        }
    }

    suspend fun splitPdfPagesInMemory(
        context: Context,
        uri: Uri,
        baseFileName: String
    ): List<SplitPageData> = withContext(Dispatchers.IO) {
        val resultPages = mutableListOf<SplitPageData>()
        val resolver = context.contentResolver

        try {
            resolver.openInputStream(uri)?.use { inputStream ->
                val reader = PdfReader(inputStream)
                val sourcePdf = PdfDocument(reader)

                val totalPages = sourcePdf.numberOfPages
                if (totalPages == 0) {
                    Log.e("PDF_SPLIT", "PDF has no pages.")
                    return@withContext emptyList()
                }

                for (i in 1..totalPages) {
                    try {
                        val outputStream = ByteArrayOutputStream()
                        val writer = PdfWriter(outputStream)
                        val singlePagePdf = PdfDocument(writer)

                        sourcePdf.copyPagesTo(i, i, singlePagePdf)
                        singlePagePdf.close()

                        val splitBytes = outputStream.toByteArray()
                        if (splitBytes.isNotEmpty()) {
                            resultPages.add(
                                SplitPageData(
                                    pageNumber = i,
                                    fileName = "${baseFileName}_page_$i.pdf",
                                    pdfBytes = splitBytes,
                                    uri = uri
                                )
                            )
                        } else {
                            Log.w("PDF_SPLIT", "Skipped empty page $i")
                        }
                    } catch (pageError: Exception) {
                        Log.e("PDF_SPLIT", "❌ Failed to split page $i", pageError)
                    }
                }

                sourcePdf.close()
            } ?: Log.e("PDF_SPLIT", "Input stream was null.")
        } catch (e: Exception) {
            Log.e("PDF_SPLIT", "❌ Error splitting PDF", e)
        }

        return@withContext resultPages
    }


    suspend fun mergePdfsInMemory(context: Context, uris: List<Uri>): ByteArray? {
        return withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val outputStream = ByteArrayOutputStream()

            try {
                val pdfWriter = PdfWriter(outputStream)
                val mergedPdfDoc = PdfDocument(pdfWriter)
                val merger = PdfMerger(mergedPdfDoc)

                for (pdfUri in uris) {
                    resolver.openInputStream(pdfUri)?.use { inputStream ->
                        val reader = PdfReader(inputStream)
                        val pdfDoc = PdfDocument(reader)
                        merger.merge(pdfDoc, 1, pdfDoc.numberOfPages)
                        pdfDoc.close()
                    }
                }

                mergedPdfDoc.close()
                outputStream.toByteArray()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                outputStream.close()
            }
        }
    }

}




