package com.example.pdfconverter.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.NavController
import com.example.pdfconverter.viewmodels.StateViewModel
import com.example.pdfconverter.viewmodels.UploadViewModel
import kotlinx.coroutines.delay
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.pdfconverter.helper.NavigateToHistoryHelper
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPickerScreen(
    navController: NavController,
    stateViewModel: StateViewModel,
    uploadViewModel: UploadViewModel,
    connectToWebViewModel: ConnectToWebViewModel
) {

    val backgroundColor = Color(0xFFF5F5F5)
    val buttonColor = Color(0xFFD32F2F)

    val context = LocalContext.current
    val formats by stateViewModel.availableFormats.collectAsState()
    var conversionState by remember { mutableStateOf("idle") }
    var conversionSuccess by remember { mutableStateOf<Boolean?>(null) }
    val serverFileName by uploadViewModel.uploadResponse.collectAsState()
    val pdfUri by stateViewModel.selectedPdfUri.collectAsState()
    val fileName by stateViewModel.selectedPdfFileName.collectAsState()
    val scope = rememberCoroutineScope()
    val fileExtension = remember(serverFileName?.file) {
        serverFileName?.file?.substringAfterLast('.', "")?.let { ".$it" } ?: ""
    }
    var downloadFileName by remember { mutableStateOf("") }
    val downloadStatus by uploadViewModel.downloadStatus.collectAsState()
    var selectedFormat by remember { mutableStateOf(formats.firstOrNull() ?: "") }
    val exceptionMessage by uploadViewModel.exceptionMessage.collectAsState()

    LaunchedEffect(formats) {
        if (formats.isNotEmpty()) {
            selectedFormat = formats.first()
        } else {
            selectedFormat = ""
        }
    }


    var expanded by remember { mutableStateOf(false) }
    var convertedMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    val isConverting by uploadViewModel.isConverting.collectAsState()


    LaunchedEffect(navController.currentDestination?.route) {
        conversionState = "idle"
        convertedMessage = null
        conversionSuccess = null
        downloadFileName = ""
        showSuccessMessage = false
    }


    BackHandler {
        stateViewModel.clearSelectedPdf()
        uploadViewModel.resetDownloadStatus()
        uploadViewModel.resetUploadResponse()
        convertedMessage = null
        conversionState = "idle"
        downloadFileName = ""
        navController.navigate("mainScreen")
    }



    // React to conversion updates
    LaunchedEffect(serverFileName, isConverting,exceptionMessage) {
        if (isConverting) {
            conversionState = "converting"
            convertedMessage = null
        } else if (serverFileName?.file?.isNotBlank() == true) {
            conversionState = "success"
            convertedMessage = "✅ Converted to $selectedFormat"
        } else if (exceptionMessage!=null) {
            conversionState = "error"
            convertedMessage = exceptionMessage
        }
    }

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

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                DocumentFile.fromSingleUri(context, it)?.name?.let { fileName ->
                    stateViewModel.setSelectedPdf(it, fileName)

                    val extension = fileName.substringAfterLast('.', "").lowercase()

                    when (extension) {
                        "pdf" -> stateViewModel.setAvailableFormats(
                            listOf(
                                "Text (.txt)",
                                "Word (.docx)",
                                "PowerPoint (.pptx)",
                                "RTF (.rtf)",
                                "HTML (.html)"
                            )
                        )
                        "txt" -> stateViewModel.setAvailableFormats(listOf("PDF (.pdf)"))
                        "docx", "doc" -> stateViewModel.setAvailableFormats(listOf("PDF (.pdf)"))
                        "pptx", "ppt" -> stateViewModel.setAvailableFormats(listOf("PDF (.pdf)"))
                        "rtf" -> stateViewModel.setAvailableFormats(listOf("PDF (.pdf)"))
                        "html" -> stateViewModel.setAvailableFormats(listOf("PDF (.pdf)"))
                        else -> stateViewModel.setAvailableFormats(emptyList())
                    }
                }
            }
        }
    )




    if (showSuccessMessage) {
        LaunchedEffect(true) {
            delay(4000)
            showSuccessMessage = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Select & Convert PDF",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(end = 40.dp)
                        )

                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        convertedMessage = null
                        showSuccessMessage = false
                        conversionSuccess = false
                        conversionState = "idle"
                        downloadFileName = ""
                        uploadViewModel.resetUploadResponse()
                        stateViewModel.clearSelectedPdf()
                        uploadViewModel.resetDownloadStatus()

                        navController.navigate("mainScreen")
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
            Button(
                onClick = {
                    launcher.launch(
                        arrayOf(
                            "application/pdf",         // PDF
                            "application/msword",      // .doc
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                            "application/vnd.ms-powerpoint", // .ppt
                            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
                            "text/plain",              // .txt
                            "text/html",               // .html
                            "application/rtf"          // .rtf
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose Pdf", color = Color.White)
            }


            Spacer(modifier = Modifier.height(24.dp))

            fileName?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable {
                            pdfUri?.let { uri ->
                                val encoded = Uri.encode(uri.toString())
                                navController.navigate("view_pdf?uri=$encoded")
                            }
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "PDF File",
                        tint = buttonColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Selected File:", color = Color.Black, fontWeight = FontWeight.SemiBold)
                        Text(it, color = Color.Black)
                    }
                    IconButton(onClick = {
                        convertedMessage = null
                        showSuccessMessage = false
                        conversionSuccess = false
                        conversionState = "idle"
                        downloadFileName = ""
                        stateViewModel.clearSelectedPdf()
                        uploadViewModel.resetDownloadStatus()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove PDF",
                            tint = Color.Red
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                TextField(
                    value = selectedFormat,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Convert to", color = Color.Black) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    formats.forEach { format ->
                        DropdownMenuItem(
                            text = { Text(format, color = Color.White) },
                            onClick = {
                                selectedFormat = format
                                expanded = false
                            }
                        )
                    }
                }
            }




            Spacer(modifier = Modifier.height(32.dp))

            if (isConverting) {
                CircularProgressIndicator(color = buttonColor)
            } else {
                Button(
                    onClick = {
                        fileName?.substringBeforeLast('.') ?: "output"
                        if (pdfUri != null) {
                            when {
                                fileName?.endsWith(".pdf", true) == true -> {
                                    when (selectedFormat) {
                                        "Text (.txt)" -> uploadViewModel.uploadPdfForTxt(context, pdfUri)
                                        "Word (.docx)" -> uploadViewModel.uploadPdf(context, pdfUri)
                                        "PowerPoint (.pptx)" -> uploadViewModel.uploadPdfForPpt(context, pdfUri)
                                        "RTF (.rtf)" -> uploadViewModel.uploadPdfForRtf(context, pdfUri)
                                        "HTML (.html)" -> uploadViewModel.uploadPdfForHtml(context, pdfUri)
                                    }
                                }
                                fileName?.endsWith(".txt", true) == true -> uploadViewModel.uploadTxtForPdf(context, pdfUri)
                                fileName?.endsWith(".docx", true) == true -> uploadViewModel.uploadDocxForPdf(context, pdfUri)
                                fileName?.endsWith(".pptx", true) == true -> uploadViewModel.uploadPptxForPdf(context, pdfUri)
                                fileName?.endsWith(".rtf", true) == true -> uploadViewModel.uploadRtfForPdf(context, pdfUri)
                                fileName?.endsWith(".html", true) == true -> uploadViewModel.uploadHtmlForPdf(context, pdfUri)
                            }

                        }
                    },
                    enabled = pdfUri != null,
                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Convert", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            convertedMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {

                    Column(modifier = Modifier.padding(16.dp)) {
                        if(conversionState == "success" ){
                        Text(
                            text = "✅ Conversion Successful!",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                            Text(text = message,
                                color = Color.Black
                            )
                        }
                        else if (conversionState == "error" && exceptionMessage != null) {
                            Text(
                                text = "❌ Conversion Failed!",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = exceptionMessage!!,
                                color = Color.Black
                            )
                        }


                    }
                }
            }



            if (conversionState == "success") {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val primaryColor = Color.Red
                    val borderDefault = Color(0xFFB0B0B0)

                    OutlinedTextField(
                        value = downloadFileName,
                        onValueChange = { downloadFileName = it },
                        label = {
                            Text("Enter file name to save", color = Color.Black)
                        },
                        placeholder = {
                            Text("Enter file name", color = Color(0xFFB0B0B0))
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.Black),
                        trailingIcon = {
                            Text(
                                text = fileExtension, // example: ".txt"
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = Color.Black,
                            focusedIndicatorColor = borderDefault,
                            unfocusedIndicatorColor = borderDefault,
                            cursorColor = primaryColor
                        )
                    )

                }

                Spacer(modifier = Modifier.height(8.dp))

                val fullFileName = "$downloadFileName$fileExtension"

                Button(
                    onClick = {
                       scope.launch {
                           uploadViewModel.downloadAndSaveFile(
                               context = context,
                               serverFileName = serverFileName?.file ?: "",
                               desiredFileName = fullFileName
                           )
                       }
                    },
                    enabled = downloadFileName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save to Downloads", color = Color.White)
                }
            }


            downloadStatus?.let { status ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = status,
                    color = if (status.contains("saved", true)) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                NavigateToHistoryHelper(navController)
            }

        }
    }
}
