package com.example.pdfconverter.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.launch
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import kotlin.math.hypot


@SuppressLint("UseKtx")
@Composable
fun CropScreen(
    bitmap: Bitmap,
    initialCorners: List<PointF>? = null,
    onCancel: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    val scope = rememberCoroutineScope()
    var imageRect by remember { mutableStateOf(android.graphics.RectF()) }

    var topLeft by remember { mutableStateOf(Offset.Zero) }
    var topRight by remember { mutableStateOf(Offset.Zero) }
    var bottomLeft by remember { mutableStateOf(Offset.Zero) }
    var bottomRight by remember { mutableStateOf(Offset.Zero) }

    val cornerRadius = 20f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .onGloballyPositioned { cords ->
                val w = cords.size.width.toFloat()
                val h = cords.size.height.toFloat()
                val bw = bitmap.width.toFloat()
                val bh = bitmap.height.toFloat()

                val scale = minOf(w / bw, h / bh)
                val displayW = bw * scale
                val displayH = bh * scale
                val left = (w - displayW) / 2f
                val top = (h - displayH) / 2f
                imageRect = android.graphics.RectF(left, top, left + displayW, top + displayH)

                if (topLeft == Offset.Zero) {
                    initialCorners?.takeIf { it.size == 4 }?.let {
                        val scaleX = displayW / bw
                        val scaleY = displayH / bh
                        topLeft = Offset(left + it[0].x * scaleX, top + it[0].y * scaleY)
                        topRight = Offset(left + it[1].x * scaleX, top + it[1].y * scaleY)
                        bottomRight = Offset(left + it[2].x * scaleX, top + it[2].y * scaleY)
                        bottomLeft = Offset(left + it[3].x * scaleX, top + it[3].y * scaleY)
                    } ?: run {
                        val margin = displayW * 0.1f
                        topLeft = Offset(left + margin, top + margin)
                        topRight = Offset(left + displayW - margin, top + margin)
                        bottomLeft = Offset(left + margin, top + displayH - margin)
                        bottomRight = Offset(left + displayW - margin, top + displayH - margin)
                    }
                }
            }
    ) {
        // Display image
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        val pos = change.position

                        val cornerThreshold = 80f
                        val edgeThreshold = 45f

                        val corners = listOf(topLeft, topRight, bottomRight, bottomLeft)
                        val cornerDistances = corners.map { hypot(it.x - pos.x, it.y - pos.y) }

                        val nearestCornerIndex = cornerDistances.indexOf(cornerDistances.minOrNull()!!)
                        val nearestCornerDistance = cornerDistances[nearestCornerIndex]

                        val midTop = (topLeft + topRight) / 2f
                        val midRight = (topRight + bottomRight) / 2f
                        val midBottom = (bottomLeft + bottomRight) / 2f
                        val midLeft = (topLeft + bottomLeft) / 2f

                        val edges = listOf(midTop, midRight, midBottom, midLeft)
                        val edgeDistances = edges.map { hypot(it.x - pos.x, it.y - pos.y) }
                        val nearestEdgeIndex = edgeDistances.indexOf(edgeDistances.minOrNull()!!)
                        val nearestEdgeDistance = edgeDistances[nearestEdgeIndex]

                        when {
                            // 🔹 Corner Drag — smoother and more responsive
                            nearestCornerDistance < cornerThreshold && nearestCornerDistance < nearestEdgeDistance * 0.8f -> {
                                val cornerDrag = dragAmount * 0.9f // apply slight damping for precision
                                val newCorner = corners[nearestCornerIndex] + cornerDrag
                                val clampedX = newCorner.x.coerceIn(imageRect.left, imageRect.right)
                                val clampedY = newCorner.y.coerceIn(imageRect.top, imageRect.bottom)

                                when (nearestCornerIndex) {
                                    0 -> topLeft = Offset(clampedX, clampedY)
                                    1 -> topRight = Offset(clampedX, clampedY)
                                    2 -> bottomRight = Offset(clampedX, clampedY)
                                    3 -> bottomLeft = Offset(clampedX, clampedY)
                                }
                            }

                            nearestEdgeDistance < edgeThreshold -> {
                                val edgeDrag = dragAmount * 0.85f
                                when (nearestEdgeIndex) {
                                    0 -> {
                                        topLeft += edgeDrag
                                        topRight += edgeDrag
                                    }
                                    1 -> {
                                        topRight += edgeDrag
                                        bottomRight += edgeDrag
                                    }
                                    2 -> {
                                        bottomLeft += edgeDrag
                                        bottomRight += edgeDrag
                                    }
                                    3 -> {
                                        topLeft += edgeDrag
                                        bottomLeft += edgeDrag
                                    }
                                }

                                fun clampCorner(o: Offset) = Offset(
                                    o.x.coerceIn(imageRect.left, imageRect.right),
                                    o.y.coerceIn(imageRect.top, imageRect.bottom)
                                )

                                topLeft = clampCorner(topLeft)
                                topRight = clampCorner(topRight)
                                bottomLeft = clampCorner(bottomLeft)
                                bottomRight = clampCorner(bottomRight)
                            }
                        }
                    }
                }


        ) {
            val polygonPath = Path().apply {
                moveTo(topLeft.x, topLeft.y)
                lineTo(topRight.x, topRight.y)
                lineTo(bottomRight.x, bottomRight.y)
                lineTo(bottomLeft.x, bottomLeft.y)
                close()
            }

            val outerPath = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
            }

            drawPath(
                path = Path().apply { op(outerPath, polygonPath, PathOperation.Difference) },
                color = Color(0x99000000)
            )

            drawPath(polygonPath, Color.White, style = Stroke(width = 4f))
            listOf(topLeft, topRight, bottomLeft, bottomRight).forEach { corner ->
                drawCircle(Color.Cyan, radius = cornerRadius, center = corner)
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(onClick = {
                scope.launch {

                    val matsToRelease = mutableListOf<Mat>()
                    var resultBitmap: Bitmap? = null

                    try {
                        val srcMat = Mat()
                        matsToRelease.add(srcMat)
                        Utils.bitmapToMat(bitmap, srcMat)

                        val bw = bitmap.width.toFloat()
                        val bh = bitmap.height.toFloat()
                        val scaleX = bw / imageRect.width()
                        val scaleY = bh / imageRect.height()

                        val srcPoints = MatOfPoint2f(
                            Point(
                                ((topLeft.x - imageRect.left) * scaleX).toDouble(),
                                ((topLeft.y - imageRect.top) * scaleY).toDouble()
                            ),
                            Point(
                                ((topRight.x - imageRect.left) * scaleX).toDouble(),
                                ((topRight.y - imageRect.top) * scaleY).toDouble()
                            ),
                            Point(
                                ((bottomRight.x - imageRect.left) * scaleX).toDouble(),
                                ((bottomRight.y - imageRect.top) * scaleY).toDouble()
                            ),
                            Point(
                                ((bottomLeft.x - imageRect.left) * scaleX).toDouble(),
                                ((bottomLeft.y - imageRect.top) * scaleY).toDouble()
                            )
                        )
                        matsToRelease.add(srcPoints)

                        val width = hypot(
                            (topRight.x - topLeft.x).toDouble(),
                            (topRight.y - topLeft.y).toDouble()
                        ) * scaleX

                        val height = hypot(
                            (bottomLeft.x - topLeft.x).toDouble(),
                            (bottomLeft.y - topLeft.y).toDouble()
                        ) * scaleY

                        val dstPoints = MatOfPoint2f(
                            Point(0.0, 0.0),
                            Point(width, 0.0),
                            Point(width, height),
                            Point(0.0, height)
                        )
                        matsToRelease.add(dstPoints)

                        val transform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)
                        matsToRelease.add(transform)

                        val dstMat = Mat(Size(width, height), srcMat.type())
                        matsToRelease.add(dstMat)

                        Imgproc.warpPerspective(
                            srcMat, dstMat, transform,
                            dstMat.size(), Imgproc.INTER_LANCZOS4
                        )

                        resultBitmap = createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888)
                        Utils.matToBitmap(dstMat, resultBitmap)

                        onConfirm(resultBitmap)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        matsToRelease.forEach { it.release() }
                    }
                }
            }) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Confirm Crop",
                    tint = Color.Green,
                    modifier = Modifier.size(48.dp)
                )
            }

        }
    }
}
