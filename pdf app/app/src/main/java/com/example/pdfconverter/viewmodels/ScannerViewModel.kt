package com.example.pdfconverter.viewmodels

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfconverter.dataClasses.ConvertedFiles
import com.example.pdfconverter.dataClasses.EnhancementType
import com.example.pdfconverter.dataClasses.ScannedPage
import com.example.pdfconverter.manager.FileHistoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class ScannerViewModel : ViewModel() {

    private val _scannedPages = MutableStateFlow<List<ScannedPage>>(emptyList())
    val scannedPages = _scannedPages.asStateFlow()

    private val _selectedEnhancement = MutableStateFlow(EnhancementType.NONE)
    val selectedEnhancement = _selectedEnhancement.asStateFlow()

    fun setEnhancement(type: EnhancementType) {
        _selectedEnhancement.value = type
    }


    fun addPage(bitmap: Bitmap) {
        viewModelScope.launch {
            val enhanced = enhanceBitmapForPdf(bitmap, _selectedEnhancement.value)
            val page = ScannedPage(uri = null, previewBitmap = bitmap, fullBitmap = enhanced)
            _scannedPages.value = _scannedPages.value + page
        }
    }

    fun removePage(page: ScannedPage) {
        _scannedPages.value = _scannedPages.value - page
    }

    fun clearAll() {
        _scannedPages.value = emptyList()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    suspend fun savePdf(context: Context, fileName: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val pdfDocument = android.graphics.pdf.PdfDocument()

                scannedPages.value.forEachIndexed { index, page ->
                    val originalBitmap = page.fullBitmap ?: page.previewBitmap ?: return@forEachIndexed

                    val bitmap = enhanceBitmapForPdf(originalBitmap, _selectedEnhancement.value)


                    val pageWidth = 2480
                    val pageHeight = 3508

                    val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(
                        pageWidth, pageHeight, index + 1
                    ).create()

                    val pdfPage = pdfDocument.startPage(pageInfo)
                    val canvas = pdfPage.canvas
                    canvas.drawColor(Color.WHITE)

                    val scale = minOf(
                        pageWidth.toFloat() / bitmap.width,
                        pageHeight.toFloat() / bitmap.height
                    )
                    val scaledWidth = (bitmap.width * scale).toInt()
                    val scaledHeight = (bitmap.height * scale).toInt()
                    val left = (pageWidth - scaledWidth) / 2f
                    val top = (pageHeight - scaledHeight) / 2f
                    val rect = RectF(left, top, left + scaledWidth, top + scaledHeight)

                    val paint = Paint().apply {
                        isAntiAlias = true
                        isFilterBitmap = true
                        isDither = true
                    }

                    canvas.drawBitmap(bitmap, null, rect, paint)
                    pdfDocument.finishPage(pdfPage)
                }

                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { output ->
                        pdfDocument.writeTo(output)
                    }
                }

                val fileNameOnly = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"
                val outFile = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileNameOnly
                )

                FileHistoryManager.saveFile(
                    context,
                    ConvertedFiles(
                        name = fileNameOnly,
                        path = outFile.absolutePath
                    )
                )
                pdfDocument.close()
                uri
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
                }
                null
            }
        }
    }

    suspend fun enhanceBitmapForPdf(
        bitmap: Bitmap,
        type: EnhancementType = EnhancementType.NONE,
        intensity: Float = 0.5f
    ): Bitmap = withContext(Dispatchers.Default) {
        // If no filter or zero intensity — return original immediately
        if (type == EnhancementType.NONE || intensity <= 0f) return@withContext bitmap

        val enhanced = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val matsToRelease = mutableListOf<Mat>()
        val src = Mat()
        Utils.bitmapToMat(bitmap, src)
        matsToRelease.add(src)

        try {
            when (type) {
                EnhancementType.BRIGHTNESS -> {
                    val result = Mat()
                    matsToRelease.add(result)

                    // Now neutral at 0f
                    val contrast = 1.0 + 0.8 * intensity
                    val brightness = 0.0 + 40.0 * intensity

                    src.convertTo(result, -1, contrast, brightness)
                    Utils.matToBitmap(result, enhanced)
                }

                EnhancementType.HIGH_CONTRAST -> {
                    val gray = Mat()
                    val blurred = Mat()
                    val division = Mat()
                    val claheResult = Mat()
                    val result = Mat()

                    try {
                        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
                        Imgproc.GaussianBlur(gray, blurred, Size(81.0, 81.0), 0.0)
                        Core.divide(gray, blurred, division, 255.0)
                        Core.normalize(division, division, 0.0, 255.0, Core.NORM_MINMAX)
                        division.convertTo(division, CvType.CV_8UC1)

                        val clahe = Imgproc.createCLAHE(1.0 + 4.0 * intensity, Size(8.0, 8.0))
                        clahe.apply(division, claheResult)

                        val contrast = 1.0 + 0.8 * intensity
                        val brightness = 0.0 + 20.0 * intensity

                        claheResult.convertTo(result, -1, contrast, brightness)
                        Imgproc.cvtColor(result, result, Imgproc.COLOR_GRAY2BGR)
                        Utils.matToBitmap(result, enhanced)
                    } finally {
                        listOf(gray, blurred, division, claheResult, result).forEach { it.release() }
                    }
                }

                EnhancementType.BLACK_WHITE -> {
                    val gray = Mat()
                    val bw = Mat()
                    matsToRelease.add(gray)
                    matsToRelease.add(bw)

                    Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)
                    Imgproc.GaussianBlur(gray, gray, Size(3.0, 3.0), 0.0)

                    val blockSize = 15
                    val cValue = 10.0 * (1 - intensity) + 2.0

                    Imgproc.adaptiveThreshold(
                        gray, bw, 255.0,
                        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                        Imgproc.THRESH_BINARY,
                        blockSize,
                        cValue
                    )

                    Utils.matToBitmap(bw, enhanced)
                }

                EnhancementType.GRAYSCALE -> {
                    val gray = Mat()
                    matsToRelease.add(gray)

                    Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY)

                    val contrast = 1.0 + 0.8 * intensity
                    val brightness = 0.0 + 20.0 * intensity

                    gray.convertTo(gray, -1, contrast, brightness)
                    Utils.matToBitmap(gray, enhanced)
                }

                EnhancementType.NONE -> { /* already handled above */ }
            }

            // Optional sharpening (mild)
            val matToSharpen = Mat()
            val resultSharpened = Mat()
            val kernel = Mat(3, 3, CvType.CV_32F)
            matsToRelease.add(matToSharpen)
            matsToRelease.add(resultSharpened)
            matsToRelease.add(kernel)

            Utils.bitmapToMat(enhanced, matToSharpen)
            kernel.put(
                0, 0, floatArrayOf(
                    0f, -0.05f * intensity, 0f,
                    -0.05f * intensity, 1.1f + 0.3f * intensity, -0.05f * intensity,
                    0f, -0.05f * intensity, 0f
                )
            )

            Imgproc.filter2D(matToSharpen, resultSharpened, -1, kernel)
            Utils.matToBitmap(resultSharpened, enhanced)

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            matsToRelease.forEach { it.release() }
        }

        enhanced
    }


    suspend fun enhanceBitmapForPreview(
        bitmap: Bitmap,
        type: EnhancementType,
        intensity: Float = 0.5f
    ): Bitmap = withContext(Dispatchers.Default) {
        enhanceBitmapForPdf(bitmap, type, intensity)
    }



}
