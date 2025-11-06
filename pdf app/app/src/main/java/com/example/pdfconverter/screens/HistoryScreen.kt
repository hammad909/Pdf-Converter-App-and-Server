package com.example.pdfconverter.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pdfconverter.dataClasses.ConvertedFiles
import com.example.pdfconverter.helper.InfoHelperDialog
import com.example.pdfconverter.manager.FileActionsManager
import com.example.pdfconverter.manager.FileHistoryManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val (files, setFiles) = remember {
        mutableStateOf(FileHistoryManager.getFiles(context))
    }

    var showInfo = remember { mutableStateOf<Boolean>(false) }

    val title = "Instructions"
    val text = "These are the successfully converted files." +
            " Tap on the file to view the file. " +
            "Tap the three dots to access further options. Options including view the file," +
            "Share on other platforms and delete from the device"


    InfoHelperDialog(showInfo,title,text)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 36.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "History",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },navigationIcon = {
                    IconButton(onClick = { navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },actions = {
                    IconButton(onClick = {

                        showInfo.value = true

                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Instructions",
                            tint = Color.White
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFD32F2F))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No files found",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(files) { file ->
                    FileCard(
                        context = context,
                        file = file,
                        onDelete = {
                            setFiles(FileHistoryManager.getFiles(context))
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun FileCard(context: Context, file: ConvertedFiles, onDelete: () -> Unit) {
    val extension = file.name.substringAfterLast('.', "").lowercase()
    val iconColor = when (extension) {
        "pdf" -> Color(0xFFD32F2F)
        "doc", "docx" -> Color(0xFF1976D2)
        "txt" -> Color(0xFF388E3C)
        "jpg", "jpeg", "png" -> Color(0xFFF57C00)
        else -> Color.Gray
    }

    val expanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { FileActionsManager.openFile(context, file) },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "File",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = file.name,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Text(
                        text = file.path,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Box {
                    IconButton(onClick = { expanded.value = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = Color.Black
                        )
                    }
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open") },
                            onClick = {
                                expanded.value = false
                                FileActionsManager.openFile(context, file)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share") },
                            onClick = {
                                expanded.value = false
                                FileActionsManager.shareFile(context, file)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded.value = false
                                FileHistoryManager.removeFile(context, file)
                                onDelete()
                            }
                        )
                    }
                }
            }

        }
    }
}
