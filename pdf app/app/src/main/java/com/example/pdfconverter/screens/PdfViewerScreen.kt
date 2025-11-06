package com.example.pdfconverter.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfUri: Uri,
    navController: NavController,
    connectToWebViewModel: ConnectToWebViewModel
) {
    val context = LocalContext.current

    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val scrollState = rememberScrollState()
    val pagePositions = remember { mutableStateListOf<Int>() }

    val receivedAction by connectToWebViewModel.receivedAction.collectAsState()

    LaunchedEffect(receivedAction) {
        Log.d("ConnectToWebScreen", "receivedAction changed: $receivedAction")
        receivedAction?.let { (action, fileName) ->
            when (action) {
                "convert" -> navController.navigate("webConvertScreen/$fileName")
                "merge" -> navController.navigate("webMergingScreen/$fileName")
                "split" -> navController.navigate("webPdfSplitScreen/$fileName")
            }
            connectToWebViewModel.clearReceivedAction()
        }
    }

    // Load PDF pages into bitmaps
    LaunchedEffect(pdfUri) {
        val pdfPages = mutableListOf<Bitmap>()
        withContext(Dispatchers.IO) {
            context.contentResolver.openFileDescriptor(pdfUri, "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val bmp = createBitmap(page.width, page.height)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        pdfPages.add(bmp)
                    }
                }
            }
        }
        bitmaps = pdfPages
        pagePositions.clear()
        repeat(pdfPages.size) { pagePositions.add(0) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF Viewer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        if (bitmaps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    bitmaps.forEachIndexed { index, bitmap ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .onGloballyPositioned { coords ->
                                    val posY = coords.positionInParent().y.toInt()
                                    if (index < pagePositions.size) {
                                        pagePositions[index] = posY
                                    }
                                }
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "PDF page ${index + 1}",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
