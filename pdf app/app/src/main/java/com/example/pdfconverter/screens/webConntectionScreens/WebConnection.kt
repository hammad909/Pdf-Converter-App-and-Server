package com.example.pdfconverter.screens.webConntectionScreens

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pdfconverter.screens.PortraitCaptureActivity
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectToWebScreen(
    navController: NavController,
    connectToWebViewModel: ConnectToWebViewModel
) {
    var code by remember { mutableStateOf("") }
    val status by connectToWebViewModel.connectionStatus.collectAsState()
    val receivedAction by connectToWebViewModel.receivedAction.collectAsState()

    var shouldNavigateOn by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current


    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        if (result.contents != null) {
            val scannedCode = result.contents
            Log.d("ConnectToWebScreen", "Scanned QR Code: $scannedCode")
            code = scannedCode
            connectToWebViewModel.connect(scannedCode)
            shouldNavigateOn = true
        }else{
            Toast.makeText(context,"Scan Error('failed')", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(status,shouldNavigateOn) {
        if(shouldNavigateOn == true && status == "Connected"){
            navController.navigate("mainScreen"){
                coroutineScope.launch {
                    delay(1500)
                    popUpTo("webConnect"){
                        inclusive = true
                    }
                }
            }
        }
    }

    LaunchedEffect(receivedAction) {
        receivedAction?.let { (action, fileName) ->
            when (action) {
                "convert" -> navController.navigate("webConvertScreen/$fileName")
                "merge" -> navController.navigate("webMergingScreen/$fileName")
                "split" -> navController.navigate("webPdfSplitScreen/$fileName")
            }
            connectToWebViewModel.clearReceivedAction()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Connect to Web",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 60.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD32F2F))
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Enter the 6-digit connection code or scan QR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F)
            )

            OutlinedTextField(
                value = code,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        code = it
                    }
                },
                label = { Text("Enter Code", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black,
                    cursorColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray
                )
            )


            Button(
                onClick = {
                    if (status == "Connected") {
                        connectToWebViewModel.disconnect()
                        shouldNavigateOn = true
                    } else {
                        connectToWebViewModel.connect(code)
                        shouldNavigateOn = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (status == "Connected") Color(0xFF388E3C) else Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    if (status == "Connected") "Disconnect" else "Connect",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    val options = ScanOptions().apply {
                        setPrompt("Scan QR code from web")
                        setBeepEnabled(true)
                        captureActivity = PortraitCaptureActivity::class.java
                    }
                    barcodeLauncher.launch(options)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Scan QR Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Column {
                Text(
                    text = status,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (status) {
                        "Connected" -> Color(0xFF388E3C)
                        "Wrong Code", "Invalid Code" -> Color.Red
                        else -> Color.Gray
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Note: ")
                        }
                        append("Closing the app will disconnect the session. Tapping Disconnect will also remove the connection.")
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}




