package com.example.pdfconverter.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfconverter.dataClasses.ConvertedFiles
import kotlinx.coroutines.launch
import com.example.pdfconverter.dataClasses.UploadResponse
import com.example.pdfconverter.helper.saveFileToDownloads
import com.example.pdfconverter.helper.uriToTempFile
import com.example.pdfconverter.manager.FileHistoryManager
import com.example.pdfconverter.repository.UploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class UploadViewModel : ViewModel() {

    private val repository = UploadRepository()

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _uploadResponse = MutableStateFlow<UploadResponse?>(null)
    val uploadResponse: StateFlow<UploadResponse?> = _uploadResponse

    private val _downloadStatus = MutableStateFlow<String?>(null)
    val downloadStatus: StateFlow<String?> = _downloadStatus.asStateFlow()

    private val _exceptionMessage = MutableStateFlow<String?>(null)
    val exceptionMessage : StateFlow<String?> = _exceptionMessage.asStateFlow()

    fun uploadPdf(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadPdfFile(file)

                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {
                        _uploadResponse.value = null
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadPdfForPpt(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file_ppt"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadPdfForPpt(file)

                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                        _uploadResponse.value = null
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadPdfForTxt(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file_txt"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadPdfForTxt(file)
                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadPdfForHtml(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file_html"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadPdfForHtml(file)
                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadPdfForRtf(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file_rtf"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadPdfForRtf(file)
                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    suspend fun downloadAndSaveFile(
        context: Context,
        serverFileName: String,
        desiredFileName: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = repository.downloadFileFromServer(serverFileName)
                if (response.isSuccessful && response.body() != null) {
                    val savedFile = saveFileToDownloads(context, desiredFileName, response.body()!!)

                    if (savedFile != null) {
                        FileHistoryManager.saveFile(
                            context,
                            ConvertedFiles(name = desiredFileName, path = savedFile.absolutePath)
                        )

                        _downloadStatus.value = "File saved to Downloads as $desiredFileName"
                        true
                    } else {
                        _downloadStatus.value = "Failed to save file"
                        false
                    }
                } else {
                    _downloadStatus.value = "Download failed: ${response.code()}"
                    false
                }
            } catch (e: Exception) {
                _downloadStatus.value = "Error: ${e.localizedMessage}"
                false
            }
        }
    }

    fun uploadDocxForPdf(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file_docx"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadDocxForPdf(file)

                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadPptxForPdf(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file_pptx"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadPptxForPdf(file)

                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadHtmlForPdf(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null
            try {
                val fileName = "upload_file_html"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadHtmlForPdf(file)

                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadRtfForPdf(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null
            try {
                val fileName = "upload_file_rtf"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadRtfForPdf(file)

                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure {e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun uploadTxtForPdf(context: Context, uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            _isConverting.value = true
            _exceptionMessage.value = null

            try {
                val fileName = "upload_file_txt"
                val file = uriToTempFile(context, uri, fileName)

                if (file != null) {
                    val result = repository.uploadTxtForPdf(file)

                    result.onSuccess { response ->
                        _uploadResponse.value = response
                    }.onFailure { e->
                        _uploadResponse.value = null
                        _exceptionMessage.value = e.message ?: "unknown Error occurred"
                    }
                } else {
                    _uploadResponse.value = null
                }
            } catch (e: Exception) {
                _uploadResponse.value = null
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun resetDownloadStatus() {
        _downloadStatus.value = null
    }

    fun resetUploadResponse() {
        _uploadResponse.value = null
    }
}
