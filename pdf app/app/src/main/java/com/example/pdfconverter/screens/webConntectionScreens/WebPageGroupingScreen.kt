package com.example.pdfconverter.screens.webConntectionScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pdfconverter.dataClasses.PageGroup
import com.example.pdfconverter.dataClasses.TempGroup
import com.example.pdfconverter.viewmodels.PdfConverterViewModel
import com.example.pdfconverter.screens.EditSingleGroupDialog
import com.example.pdfconverter.screens.GroupEditorDialog
import com.example.pdfconverter.screens.PdfPagePreview
import com.example.pdfconverter.screens.groupPagesByRanges
import com.example.pdfconverter.viewmodels.ConnectToWebViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebPageGroupAssignmentScreen(
    navController: NavController,
    pdfConverterViewModel: PdfConverterViewModel,
    connectToWebViewModel : ConnectToWebViewModel
) {

    var showFileHistoryDialog by remember { mutableStateOf(false) }
    val splitPages by pdfConverterViewModel.splitResultFiles.collectAsState()
    val tempGroups = pdfConverterViewModel.tempGroups
    var pagesPerGroupText by remember { mutableStateOf("") }
    var groupBeingEdited by remember { mutableStateOf<TempGroup?>(null) }

    var showGroupEditor by remember { mutableStateOf(false) }
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

    val code by connectToWebViewModel.currentCode.collectAsState()

    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF2F2F2),
        topBar = {
            TopAppBar(
                title = { Text("Assign Pages to Groups", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack()
                        pdfConverterViewModel.clearSplitResult()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD32F2F)
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(text = "Total Pages: ${splitPages.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black)

            OutlinedTextField(
                value = pagesPerGroupText,
                onValueChange = { pagesPerGroupText = it.filter { c -> c.isDigit() || c == ',' || c == '-' } },
                label = { Text("Page Ranges (e.g. 1-2,3-5,6-9)", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD32F2F),
                    unfocusedBorderColor = Color.DarkGray,
                    focusedLabelColor = Color(0xFFD32F2F),
                    cursorColor = Color.Red,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (pagesPerGroupText.isNotBlank()) {
                        groupPagesByRanges(
                            pagesPerGroupText,
                            splitPages,
                            pdfConverterViewModel
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Auto Group", color = Color.White)
            }

            if (showFileHistoryDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showFileHistoryDialog = false },
                    confirmButton = {},
                    title = {
                        Text(
                            text = "File History",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    },

                    text = {
                        Column {
                            Text(
                                text = "View all your previously converted, split, or uploaded PDF files.",
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showFileHistoryDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                        ) {
                            Text("Close", color = Color.Black)
                        }
                    },
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    shape = RoundedCornerShape(16.dp)
                )
            }


            if (showGroupEditor) {
                GroupEditorDialog(
                    initialGroups = tempGroups,
                    onDismiss = { showGroupEditor = false },
                    onSave = { updatedGroups ->
                        val oldGroupsByName = pdfConverterViewModel.tempGroups.associateBy { it.name }

                        val deletedGroups = oldGroupsByName.keys - updatedGroups.map { it.name }.toSet()

                        pdfConverterViewModel.pageAssignments.entries.removeIf { (_, assignedGroup) ->
                            assignedGroup.name in deletedGroups
                        }

                        updatedGroups.forEach { updatedGroup ->
                            val oldGroup = oldGroupsByName[updatedGroup.name]
                            if (oldGroup != null && oldGroup.color != updatedGroup.color) {
                                val updatedAssignments =
                                    pdfConverterViewModel.pageAssignments.mapValues { (_, assignedGroup) ->
                                        if (assignedGroup.name == updatedGroup.name) {
                                            PageGroup(
                                                name = updatedGroup.name,
                                                color = updatedGroup.color
                                            )
                                        } else assignedGroup
                                    }
                                pdfConverterViewModel.pageAssignments.clear()
                                pdfConverterViewModel.pageAssignments.putAll(updatedAssignments)
                            }
                        }

                        pdfConverterViewModel.tempGroups.forEachIndexed { index, oldGroup ->
                            if (index < updatedGroups.size) {
                                val newGroup = updatedGroups[index]
                                if (oldGroup.name != newGroup.name) {
                                    pdfConverterViewModel.updateGroupNameInAssignments(
                                        oldGroup.name,
                                        newGroup.name
                                    )
                                }
                            }
                        }

                        pdfConverterViewModel.tempGroups.clear()
                        pdfConverterViewModel.tempGroups.addAll(updatedGroups)
                        showGroupEditor = false
                    }
                    ,
                    pdfConverterViewModel = pdfConverterViewModel
                )
            }


            Button(
                onClick = { showGroupEditor = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Edit Groups", color = Color.White)
            }
            Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = {
                        pdfConverterViewModel.createAndUploadGroupedPdfsZip(code, context)
                        showFileHistoryDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    enabled = tempGroups.isNotEmpty() && pdfConverterViewModel.pageAssignments.isNotEmpty()
                ) {
                    Text("Save Grouped PDFs", color = Color.White)
                }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Groups", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)

            tempGroups.forEach { group ->

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.elevatedCardElevation(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (group.range.isNotBlank()) {
                                Text(
                                    text = "Pages: ${group.range}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(group.color)
                                .border(1.dp, Color.LightGray, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { groupBeingEdited = group.copy() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit single Group",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (groupBeingEdited != null) {
                EditSingleGroupDialog(
                    group = groupBeingEdited!!,
                    onDismiss = { groupBeingEdited = null },
                    onSave = { updatedGroup ->
                        val index =
                            pdfConverterViewModel.tempGroups.indexOfFirst { it.name == groupBeingEdited!!.name }
                        if (index != -1) {
                            val oldName = pdfConverterViewModel.tempGroups[index].name
                            pdfConverterViewModel.tempGroups[index] = updatedGroup

                            if (oldName != updatedGroup.name) {
                                pdfConverterViewModel.updateGroupNameInAssignments(
                                    oldName,
                                    updatedGroup.name
                                )
                            }
                        }
                        groupBeingEdited = null
                    },
                    pdfConverterViewModel = pdfConverterViewModel
                )
            }

            if (splitPages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    "Split Pages",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))

                splitPages.forEach { page ->
                    val assignedGroup = pdfConverterViewModel.pageAssignments[page.pageNumber]
                    val updatedGroup = assignedGroup?.let { assigned ->
                        tempGroups.find { it.name == assigned.name }
                    }
                    val updatedGroupName = updatedGroup?.name ?: assignedGroup?.name
                    val updatedGroupColor =
                        updatedGroup?.color ?: assignedGroup?.color ?: Color.LightGray

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Page ${page.pageNumber}", color = Color.Black)

                                Box {
                                    var expanded by remember { mutableStateOf(false) }

                                    Button(
                                        onClick = { expanded = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = updatedGroupColor
                                        )
                                    ) {
                                        Text(updatedGroupName ?: "Assign", color = Color.White)
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Unassigned") },
                                            onClick = {
                                                pdfConverterViewModel.pageAssignments.remove(page.pageNumber)
                                                expanded = false
                                            }
                                        )
                                        tempGroups.forEach { group ->
                                            DropdownMenuItem(
                                                text = { Text(group.name) },
                                                onClick = {
                                                    pdfConverterViewModel.pageAssignments[page.pageNumber] = PageGroup(
                                                        name = group.name,
                                                        color = group.color
                                                    )
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }

                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            PdfPagePreview(pageData = page)
                        }
                    }
                }
            }
        }
    }
}






