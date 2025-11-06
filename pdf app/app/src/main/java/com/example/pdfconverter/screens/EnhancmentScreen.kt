package com.example.pdfconverter.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pdfconverter.dataClasses.EnhancementType
import com.example.pdfconverter.viewmodels.ScannerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EnhancementScreen(
    bitmap: Bitmap,
    viewModel: ScannerViewModel,
    onCancel: () -> Unit,
    onDone: (Bitmap) -> Unit
) {

    var isProcessing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    var previewBitmap by remember { mutableStateOf(bitmap) }
    val selectedEnhancement by viewModel.selectedEnhancement.collectAsState()

    val enhancementIntensity = remember { mutableStateMapOf<EnhancementType, Float>() }

    var sliderValue by remember { mutableFloatStateOf(enhancementIntensity[selectedEnhancement] ?: 0.5f) }

    LaunchedEffect(selectedEnhancement) {
        sliderValue = enhancementIntensity[selectedEnhancement] ?: 0.5f
    }

    LaunchedEffect(sliderValue, selectedEnhancement) {
        enhancementIntensity[selectedEnhancement] = sliderValue
        kotlinx.coroutines.delay(150)
        if (!isProcessing) {
            isProcessing = true
            val result = viewModel.enhanceBitmapForPreview(
                bitmap,
                selectedEnhancement,
                sliderValue
            )
            previewBitmap = result
            isProcessing = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        color = Color(0xFF101010)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = "Enhance Document",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aspectRatio(3f / 4f)
                    .background(Color.DarkGray, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = previewBitmap,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "Image Crossfade"
                ) { image ->
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Enhanced Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                if (isProcessing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Choose Enhancement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EnhancementType.entries.forEach { type ->
                        ElevatedButton(
                            onClick = {
                                if (!isProcessing) {
                                    coroutineScope.launch {
                                        isProcessing = true
                                        viewModel.setEnhancement(type)
                                        val result = viewModel.enhanceBitmapForPreview(
                                            bitmap,
                                            type,
                                            sliderValue
                                        )
                                        previewBitmap = result
                                        isProcessing = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (type == selectedEnhancement)
                                    MaterialTheme.colorScheme.primary
                                else
                                    Color(0xFF2C2C2C)
                            ),
                            enabled = !isProcessing
                        ) {
                            Text(
                                text = type.name,
                                color = if (type == selectedEnhancement) Color.White else Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }


            if (selectedEnhancement != EnhancementType.NONE) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enhancement Strength: ${(sliderValue * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Slider(
                        value = sliderValue,
                        onValueChange = { newValue ->
                            sliderValue = newValue
                        },
                        valueRange = 0f..1f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel, enabled = !isProcessing) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Button(
                    onClick = { if (!isProcessing) onDone(previewBitmap) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = !isProcessing
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Apply",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
