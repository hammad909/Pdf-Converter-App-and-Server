package com.example.pdfconverter.viewmodels

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfconverter.dataClasses.PageGroup
import com.example.pdfconverter.dataClasses.SplitPageData
import com.example.pdfconverter.utills.PdfUtil
import com.example.pdfconverter.utills.PdfUtil.splitPdfPagesInMemory
import com.itextpdf.kernel.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import java.io.FileOutputStream
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.documentfile.provider.DocumentFile
import com.example.pdfconverter.apiInstance.RetrofitClient
import com.example.pdfconverter.dataClasses.ConvertedFiles
import com.example.pdfconverter.dataClasses.TempGroup
import com.example.pdfconverter.helper.createPdfBytesFromPages
import com.example.pdfconverter.helper.getUniqueFileName
import com.example.pdfconverter.helper.uploadMergedPdfFiles
import com.example.pdfconverter.manager.FileHistoryManager
import com.example.pdfconverter.utills.PdfUtil.mergePdfsInMemory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PdfConverterViewModel : ViewModel() {

    private val _splitResultFiles = MutableStateFlow<List<SplitPageData>>(emptyList())
    val splitResultFiles: StateFlow<List<SplitPageData>> get() = _splitResultFiles

    private val _isSplitting = MutableStateFlow(false)
    val isSplitting: StateFlow<Boolean> get() = _isSplitting

    private val _isMerging = MutableStateFlow(false)
    val isMerging: StateFlow<Boolean> get() = _isMerging

    private val _mergedPdfUri = MutableStateFlow<Uri?>(null)
    val mergedPdfUri: StateFlow<Uri?> get() = _mergedPdfUri


    val pageAssignments = mutableStateMapOf<Int, PageGroup>()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun mergeSelectedPdfs(context: Context, uris: List<Uri>, fileName: String) {
        viewModelScope.launch {
            _isMerging.value = true
            val mergedUri = withContext(Dispatchers.IO) {
                PdfUtil.mergePdfsWithIText(context, uris, fileName)
            }

            mergedUri?.let { contentUri ->
                // Determine final file name
                val finalName = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"
                val outFile = File(context.getExternalFilesDir(null), finalName)

                // Copy content from mergedUri to actual file
                context.contentResolver.openInputStream(contentUri)?.use { input ->
                    outFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Save real path to history
                FileHistoryManager.saveFile(
                    context,
                    ConvertedFiles(name = finalName, path = outFile.absolutePath)
                )

                // Update LiveData/StateFlow for UI
                _mergedPdfUri.value = Uri.fromFile(outFile)
            }

            _isMerging.value = false
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun splitPdfIntoPages(
        context: Context,
        uri: Uri,
        baseFileName: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isSplitting.value = true

            try {
                val pages = splitPdfPagesInMemory(context, uri, baseFileName)
                _splitResultFiles.value = pages
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
                _splitResultFiles.value = emptyList()
            } finally {
                _isSplitting.value = false
            }
        }
    }


    fun saveGroupedPdfs(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val groupedPages = pageAssignments.entries.groupBy(
                keySelector = { it.value.name },
                valueTransform = { entry ->
                    splitResultFiles.value.firstOrNull { it.pageNumber == entry.key }
                }
            ).mapValues { entry -> entry.value.filterNotNull() }

            Log.d("GroupedPDF", "Page Assignments: ${pageAssignments.entries}")

            groupedPages.forEach { (groupName, pages) ->
                try {
                    if (pages.isNotEmpty()) {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                        // Use helper to get a unique file name
                        val mergedDocFile = getUniqueFileName(downloadsDir, groupName)

                        val writer = PdfWriter(FileOutputStream(mergedDocFile))
                        val mergedDoc = PdfDocument(writer)

                        for (page in pages.sortedBy { it.pageNumber }) {
                            val srcDoc = PdfDocument(PdfReader(ByteArrayInputStream(page.pdfBytes)))

                            for (i in 1..srcDoc.numberOfPages) {
                                val pageToCopy = srcDoc.getPage(i).copyTo(mergedDoc)
                                mergedDoc.addPage(pageToCopy)
                            }

                            srcDoc.close()
                        }

                        mergedDoc.close()
                        FileHistoryManager.saveFile(
                            context,
                            ConvertedFiles(
                                name = mergedDocFile.name,
                                path = mergedDocFile.absolutePath
                            )
                        )

                        Log.d("GroupedPDF", "Saved to: ${mergedDocFile.absolutePath}")

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Saved: ${mergedDocFile.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("GroupedPDF", "Error saving group $groupName", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to save $groupName.pdf", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun assignPageToGroup(pageNumber: Int, group: PageGroup) {
        pageAssignments[pageNumber] = group
    }


    fun clearSplitResult() {
        _splitResultFiles.value = emptyList()
        _isSplitting.value = false
    }



    // Holds all the temporary groups user defines (name + color + selected pages)
    var tempGroups = mutableStateListOf<TempGroup>()
        private set

    // Function to add a new group
    fun addNewGroup() {
        tempGroups.add(TempGroup(name = "New Group", selectedPages = mutableSetOf()))
    }

    // Function to remove a group by index
    fun removeGroup(index: Int) {
        if (index in tempGroups.indices) {
            tempGroups.removeAt(index)
        }
    }

    fun updateGroupNameInAssignments(oldName: String, newName: String) {
        val updatedAssignments = pageAssignments.mapValues { (pageNumber, group) ->
            if (group.name == oldName) group.copy(name = newName) else group
        }

        pageAssignments.clear()
        pageAssignments.putAll(updatedAssignments)
    }


    //webs split pdfs upload

    fun createGroupedPdfsZip(): ByteArray? {
        val groupedPages = pageAssignments.entries.groupBy(
            keySelector = { it.value.name },
            valueTransform = { entry -> splitResultFiles.value.firstOrNull { it.pageNumber == entry.key } }
        ).mapValues { entry -> entry.value.filterNotNull() }

        if (groupedPages.isEmpty()) return null

        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            ZipOutputStream(byteArrayOutputStream).use { zipOut ->
                for ((groupName, pages) in groupedPages) {
                    if (pages.isNotEmpty()) {
                        val pdfByteArray = createPdfBytesFromPages(pages)
                        val entry = ZipEntry("$groupName.pdf")
                        zipOut.putNextEntry(entry)
                        zipOut.write(pdfByteArray)
                        zipOut.closeEntry()
                    }
                }
            }
            byteArrayOutputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }



    fun uploadGroupedPdfsZip(code: String?, zipBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val requestBody = zipBytes.toRequestBody("application/zip".toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    "grouped_pdfs.zip",
                    requestBody
                )

                val response = RetrofitClient.apiServiceWeb.uploadProcessedFile(code, multipartBody)
                if (response.isSuccessful) {
                    Log.d("Upload", "Uploaded grouped_pdfs.zip successfully")
                } else {
                    Log.e("Upload", "Failed to upload grouped_pdfs.zip: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Upload", "Exception uploading grouped_pdfs.zip")
            }
        }
    }

    fun createAndUploadGroupedPdfsZip(code: String?, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val zipBytes = createGroupedPdfsZip()
            if (zipBytes != null) {
                uploadGroupedPdfsZip(code, zipBytes)
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "No grouped PDFs to upload", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun webMergeAndUpload(context: Context, uris: List<Uri>, code: String?) {
        viewModelScope.launch {
            val mergedBytes = mergePdfsInMemory(context, uris)
            if (mergedBytes != null) {
                val success = try {
                    uploadMergedPdfFiles(code, mergedBytes)
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
                if (!success) {
                    println("Upload failed")
                }
            } else {
                println("Merging PDFs failed")
            }
        }
    }

    fun clearMergedPdf() {
        _mergedPdfUri.value = null
    }



    fun parsePageRange(rangeString: String): Set<Int> {
        val pageNumbers = mutableSetOf<Int>()
        val ranges = rangeString.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        for (range in ranges) {
            val parts = range.split("-").mapNotNull { it.toIntOrNull() }
            when (parts.size) {
                2 -> {
                    val start = parts[0]
                    val end = parts[1]
                    if (start <= end) {
                        for (i in start..end) {
                            pageNumbers.add(i)
                        }
                    }
                }
                1 -> {
                    pageNumbers.add(parts[0])
                }
            }
        }
        return pageNumbers
    }

    fun updateGroupAssignmentsFromRange(group: TempGroup, allSplitPages: List<SplitPageData>) {

        val pagesToRemove = pageAssignments.filter { it.value.name == group.name }.keys.toSet()
        pagesToRemove.forEach { pageAssignments.remove(it) }

        val pagesInNewRange = parsePageRange(group.range)
        val finalizedGroup = PageGroup(name = group.name, color = group.color)

        pagesInNewRange.forEach { pageNumber ->
            if (allSplitPages.any { it.pageNumber == pageNumber }) {
                pageAssignments[pageNumber] = finalizedGroup
            }
        }
    }

}
