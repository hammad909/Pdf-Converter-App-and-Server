package com.example.pdfconverter.screens.webConntectionScreens

import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel
import com.example.pdfconverter.viewmodels.PdfConverterViewModel
import com.example.pdfconverter.viewmodels.StateViewModel

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebPdfSplitScreen(
    navController: NavController,
    pdfConverterViewModel: PdfConverterViewModel,
    stateViewModel: StateViewModel,
    connectToWebViewModel: ConnectToWebViewModel,
    fileName : String
) {
    val context = LocalContext.current
    val selectedUri by stateViewModel.selectedPdfUri.collectAsState()
    val isLoading by pdfConverterViewModel.isSplitting.collectAsState()

    var isDownloading by remember { mutableStateOf(true) }
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

    LaunchedEffect(Unit) {
        connectToWebViewModel.webDownloadAndSaveFile(context, fileName) { fileUri ->
            if (fileUri != null) {
                isDownloading = false
                stateViewModel.setSelectedPdf(fileUri, fileName)
                Toast.makeText(context, "File downloaded: $fileUri", Toast.LENGTH_LONG).show()
            } else {
                isDownloading = false
                Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    val backgroundColor = Color(0xFFF5F5F5)
    val buttonColor = Color(0xFFD32F2F)



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Split PDF",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(end = 40.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("mainScreen")
                        stateViewModel.clearSelectedPdf()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = buttonColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select a PDF file to split into separate pages",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            fileName.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = buttonColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = it,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedUri?.let { uri ->
                                    navController.navigate("view_pdf?uri=${Uri.encode(uri.toString())}")
                                }
                            }
                    )

                    IconButton(onClick = {
                        stateViewModel.clearSelectedPdf()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove selected PDF",
                            tint = Color.Red
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        selectedUri?.let { uri ->
                            val safeName = fileName.substringBeforeLast(".")
                            pdfConverterViewModel.splitPdfIntoPages(context, uri, safeName) {
                                navController.navigate("webPageGroupAssignmentScreen")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Split PDF", color = Color.White)
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = buttonColor)
            }

        }
    }
}
