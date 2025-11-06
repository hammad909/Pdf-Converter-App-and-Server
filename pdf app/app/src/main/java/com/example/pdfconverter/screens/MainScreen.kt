package com.example.pdfconverter.screens

import android.Manifest
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pdfconverter.R
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, connectToWebViewModel: ConnectToWebViewModel) {
    val backgroundColor = Color(0xFFF5F5F5)
    val buttonColor = Color(0xFFD32F2F)
    val context = LocalContext.current
    val activity = context as? Activity


    BackHandler { activity?.finish() }

    val receivedAction by connectToWebViewModel.receivedAction.collectAsState()
    val status by connectToWebViewModel.connectionStatus.collectAsState()
    var isConnected by remember { mutableStateOf(isOnline(context)) }


    LaunchedEffect(Unit) {
        while (true) {
            isConnected = isOnline(context)
            kotlinx.coroutines.delay(2000)
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
                        text = "PDF Converter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        modifier = Modifier.padding(start = 82.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = buttonColor),
                actions = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = if(status != "Connected"){Color.White.copy(alpha = 0.15f)}
                        else{
                            Color(0xFF388E3C)
                        }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { navController.navigate("webConnect") }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Connect", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Link, contentDescription = "Web Connect", tint = Color.White)
                        }
                    }
                }
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.pdf),
                contentDescription = "PDF Illustration",
                modifier = Modifier
                    .size(260.dp)
                    .padding(bottom = 24.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item{
                    FeatureButton(
                        text = "Convert PDF",
                        color = if (isConnected) buttonColor else Color.Gray
                    ) {
                        if (isConnected) {
                            navController.navigate("pdfConverterScreen")
                        } else {
                            Toast.makeText(
                                context,
                                "No internet connection found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                item {
                    FeatureButton("Merge PDFs", buttonColor) { navController.navigate("pdfMergingScreen") }
                }
                item {
                    FeatureButton("Split PDF", buttonColor) { navController.navigate("pdfSplitScreen") }
                }
                item {
                    FeatureButton("Files History", buttonColor) { navController.navigate("historyScreen") }
                }
                item {
                    FeatureButton("Scan Document", buttonColor) { navController.navigate("scannerScreen") }
                }
            }
        }
    }
}

@Composable
fun FeatureButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
