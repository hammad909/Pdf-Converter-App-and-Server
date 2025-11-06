package com.example.pdfconverter

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.pdfconverter.navigation.MyAppNavigation
import com.example.pdfconverter.ui.theme.PDFConverterTheme
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel
import com.example.pdfconverter.viewmodels.PdfConverterViewModel
import com.example.pdfconverter.viewmodels.ScannerViewModel
import com.example.pdfconverter.viewmodels.StateViewModel
import com.example.pdfconverter.viewmodels.UploadViewModel


class MainActivity : ComponentActivity() {

    private val pdfConverterViewModel: PdfConverterViewModel by viewModels()
    private val uploadViewModel : UploadViewModel by viewModels()
    private val stateViewModel : StateViewModel by viewModels()
    private val  scannerViewModel : ScannerViewModel by viewModels()
    private val connectToWebViewModel : ConnectToWebViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            PDFConverterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyAppNavigation( modifier = Modifier.padding(innerPadding),
                        pdfConverterViewModel,
                        stateViewModel,
                        uploadViewModel,
                        connectToWebViewModel,
                        scannerViewModel
                    )
                }
            }
        }
    }
}
