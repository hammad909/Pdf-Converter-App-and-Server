package com.example.pdfconverter.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pdfconverter.dataClasses.ScannedPage
import com.example.pdfconverter.helper.generateFileName
import com.example.pdfconverter.utills.detectDocumentEdges
import com.example.pdfconverter.viewmodels.ScannerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    navController: NavController,
    viewModel: ScannerViewModel = viewModel()
) {
    val pages by viewModel.scannedPages.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()



    val tempFile = File(context.cacheDir, "page_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)

    var cropCorners by remember { mutableStateOf<List<PointF>?>(null) }

    var enhancingBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var croppingBitmap by remember { mutableStateOf<Bitmap?>(null) }


    var fileName by remember { mutableStateOf(generateFileName()) }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val scannedBitmap = savedStateHandle?.get<Bitmap>("scannedBitmap")



    LaunchedEffect(scannedBitmap) {
        scannedBitmap?.let {
            enhancingBitmap = it
            savedStateHandle.remove<Bitmap>("scannedBitmap")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            scope.launch {
                val bmp = decodeBitmap(uri, context)
                bmp?.let {
                    croppingBitmap = it
                    cropCorners = detectDocumentEdges(it) // 🔹 Auto detect edges
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { pickedUri ->
        pickedUri?.let {
            scope.launch {
                val bmp = decodeBitmap(it, context)
                bmp?.let {
                    croppingBitmap = it
                    cropCorners = detectDocumentEdges(it)
                }
            }
        }
    }

    var showNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (croppingBitmap == null && enhancingBitmap == null) {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Document Scanner", color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD32F2F)),
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigate("mainScreen")
                            viewModel.clearAll()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (croppingBitmap == null && enhancingBitmap == null) {
                BottomAppBar(
                    containerColor = Color(0xFFD32F2F),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                } else {
                                    cameraLauncher.launch(uri)
                                }
                            },
                            icon = { Icon(Icons.Filled.Camera, contentDescription = null) },
                            text = { Text("Manual Scan") },
                            containerColor = Color.White,
                            contentColor = Color(0xFFD32F2F)
                        )


                        ExtendedFloatingActionButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            icon = { Icon(Icons.Filled.Photo, contentDescription = null) },
                            text = { Text("Gallery") },
                            containerColor = Color.White,
                            contentColor = Color(0xFFD32F2F)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                enhancingBitmap != null -> {
                    EnhancementScreen(
                        bitmap = enhancingBitmap!!,
                        viewModel = viewModel,
                        onDone = { enhanced ->
                            viewModel.addPage(enhanced)
                            enhancingBitmap = null
                        },
                        onCancel = {
                            croppingBitmap = enhancingBitmap
                            enhancingBitmap = null
                        }
                    )

                }

                croppingBitmap != null -> {
                    CropScreen(
                        bitmap = croppingBitmap!!,
                        initialCorners = cropCorners,
                        onCancel = { croppingBitmap = null },
                        onConfirm = { cropped ->
                            enhancingBitmap = cropped
                            croppingBitmap = null
                            cropCorners = null
                        }
                    )
                }

                else -> {
                    if (pages.isEmpty()) {
                        EmptyState(padding)
                    } else {
                        PagesList(
                            pages = pages,
                            onRemovePage = { page -> viewModel.removePage(page) },
                            padding = padding
                        )
                    }
                }
            }

            if (croppingBitmap == null && enhancingBitmap == null) {
                Button(
                    onClick = {
                        if (pages.isEmpty()) {
                            Toast.makeText(context, "No pages to save!", Toast.LENGTH_SHORT).show()
                        } else showNameDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 90.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save Button", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Save PDF", color = Color.White)
                }
            }
        }
    }

    if (showNameDialog) {
        SavePdfDialog(
            fileName = fileName,
            onFileNameChange = { fileName = it },
            onConfirm = {
                scope.launch {
                    val uri = viewModel.savePdf(context, fileName)
                    if (uri != null) {
                        Toast.makeText(context, "PDF saved: $fileName", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Error saving PDF", Toast.LENGTH_SHORT).show()
                    }
                }
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }
}

suspend fun decodeBitmap(uri: Uri, context: Context): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                    decoder.setTargetSize(
                        info.size.width,
                        info.size.height
                    )
                }.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    .copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}




@Composable
private fun EmptyState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Text("No scanned pages yet", color = Color.Gray)
    }
}

@Composable
private fun PagesList(
    pages: List<ScannedPage>,
    onRemovePage: (ScannedPage) -> Unit,
    padding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pages) { page ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    page.previewBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Scanned Page",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color.LightGray)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    IconButton(
                        onClick = { onRemovePage(page) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Page",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavePdfDialog(
    fileName: String,
    onFileNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save PDF As") },
        text = {
            OutlinedTextField(
                value = fileName,
                onValueChange = onFileNameChange,
                singleLine = true,
                label = { Text("File name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
