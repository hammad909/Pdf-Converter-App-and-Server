package com.example.pdfconverter.utills

import android.graphics.Bitmap
import android.graphics.PointF
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

fun detectDocumentEdges(bitmap: Bitmap): List<PointF>? {
    val src = Mat()
    Utils.bitmapToMat(bitmap, src)

    val maxDim = 1000
    val scale = maxDim.toDouble() / maxOf(src.width(), src.height())
    val resized = Mat()
    if (scale < 1.0) Imgproc.resize(src, resized, Size(src.width() * scale, src.height() * scale))
    else src.copyTo(resized)

    val gray = Mat()
    Imgproc.cvtColor(resized, gray, Imgproc.COLOR_BGR2GRAY)
    Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)
    Imgproc.equalizeHist(gray, gray)

    // 3️⃣ Edge map via Canny
    val edges = Mat()
    Imgproc.Canny(gray, edges, 60.0, 180.0)
    Imgproc.dilate(edges, edges, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0)))

    // 4️⃣ Find contours
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

    if (contours.isEmpty()) return null

    // 5️⃣ Sort by area (largest first)
    contours.sortByDescending { Imgproc.contourArea(it) }

    var bestQuad: MatOfPoint2f? = null
    var bestArea = 0.0

    for (c in contours.take(15)) {
        val contour2f = MatOfPoint2f(*c.toArray())
        val peri = Imgproc.arcLength(contour2f, true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(contour2f, approx, 0.02 * peri, true)

        val area = abs(Imgproc.contourArea(approx))
        if (approx.total() == 4L && area > bestArea && area > resized.width() * resized.height() * 0.15) {
            bestArea = area
            bestQuad = approx
        }
    }

    if (bestQuad == null) return null

    val ratioX = bitmap.width.toFloat() / resized.width()
    val ratioY = bitmap.height.toFloat() / resized.height()

    val scaledPoints = bestQuad!!.toArray().map {
        PointF((it.x * ratioX).toFloat(), (it.y * ratioY).toFloat())
    }

    return sortCorners(scaledPoints)
}

private fun sortCorners(points: List<PointF>): List<PointF> {
    val sortedByY = points.sortedBy { it.y }
    val top = sortedByY.take(2).sortedBy { it.x }
    val bottom = sortedByY.takeLast(2).sortedBy { it.x }
    return listOf(top[0], top[1], bottom[1], bottom[0])
}
