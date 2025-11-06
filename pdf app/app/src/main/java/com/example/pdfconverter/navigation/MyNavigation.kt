package com.example.pdfconverter.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pdfconverter.screens.MainScreen
import com.example.pdfconverter.screens.PdfMergingScreen
import com.example.pdfconverter.screens.PdfPickerScreen
import com.example.pdfconverter.screens.PdfSplitScreen
import com.example.pdfconverter.screens.PdfViewerScreen
import com.example.pdfconverter.viewmodels.PdfConverterViewModel
import androidx.core.net.toUri
import com.example.pdfconverter.screens.DocumentScannerScreen
import com.example.pdfconverter.screens.HistoryScreen
import com.example.pdfconverter.screens.PageGroupAssignmentScreen
import com.example.pdfconverter.screens.SplashScreen
import com.example.pdfconverter.screens.webConntectionScreens.ConnectToWebScreen
import com.example.pdfconverter.screens.webConntectionScreens.WebConvertScreen
import com.example.pdfconverter.screens.webConntectionScreens.WebMergingScreen
import com.example.pdfconverter.screens.webConntectionScreens.WebPageGroupAssignmentScreen
import com.example.pdfconverter.screens.webConntectionScreens.WebPdfSplitScreen
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel
import com.example.pdfconverter.viewmodels.ScannerViewModel
import com.example.pdfconverter.viewmodels.StateViewModel
import com.example.pdfconverter.viewmodels.UploadViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost


@OptIn(ExperimentalAnimationApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MyAppNavigation( modifier : Modifier,
                     pdfConverterViewModel: PdfConverterViewModel,
                     stateViewModel: StateViewModel,
                     uploadViewModel: UploadViewModel,
                     connectToWebViewModel: ConnectToWebViewModel,
                     scannerViewModel: ScannerViewModel){

    val navController = rememberNavController()

    AnimatedNavHost(
        navController = navController,
        startDestination = "splashScreen",
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(700)) },
        exitTransition = { fadeOut(animationSpec = tween(700)) },
        popEnterTransition = { fadeIn(animationSpec = tween(700)) },
        popExitTransition = { fadeOut(animationSpec = tween(700)) }
    ) {

        composable("splashScreen") {
            SplashScreen(navController)
        }


        composable("mainScreen"){

            MainScreen(navController,connectToWebViewModel)
        }

        composable("pdfConverterScreen"){

            PdfPickerScreen(navController,stateViewModel, uploadViewModel,connectToWebViewModel)
        }

        composable("pdfMergingScreen"){

            PdfMergingScreen(navController, pdfConverterViewModel, stateViewModel,connectToWebViewModel)
        }

        composable("pdfSplitScreen"){

            PdfSplitScreen(navController, pdfConverterViewModel ,stateViewModel,connectToWebViewModel)
        }

        composable("groupAssignment") {
            PageGroupAssignmentScreen(navController, pdfConverterViewModel,connectToWebViewModel)
        }

        composable("view_pdf?uri={uri}") { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri")
            uriString?.let {
                PdfViewerScreen(it.toUri(), navController,connectToWebViewModel)
            }
        }

        //web connection
        composable("webConnect"){

            ConnectToWebScreen(
                navController,
                connectToWebViewModel
            )
        }

        composable("webConvertScreen/{fileName}") { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            WebConvertScreen(
                navController = navController,
                stateViewModel,
                uploadViewModel,
                fileName = fileName,
                connectToWebViewModel = connectToWebViewModel,

            )
        }

        composable("webPdfSplitScreen/{fileName}") { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            WebPdfSplitScreen(
                navController,
                pdfConverterViewModel,
                stateViewModel,
                connectToWebViewModel,
                fileName = fileName
            )
        }

        composable ("webPageGroupAssignmentScreen"){
            WebPageGroupAssignmentScreen(
                navController,
                pdfConverterViewModel,
                connectToWebViewModel
            )
        }

        composable("webMergingScreen/{fileName}") { backStackEntry ->
            val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
            WebMergingScreen(
                navController,
                pdfConverterViewModel,
                stateViewModel,
                connectToWebViewModel,
                fileName = fileName
            )
        }

        composable("historyScreen") {
            HistoryScreen(navController)
        }

        composable("scannerScreen") {
            DocumentScannerScreen(navController,scannerViewModel)
        }

    }



}