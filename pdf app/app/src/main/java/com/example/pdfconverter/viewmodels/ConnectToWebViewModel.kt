package com.example.pdfconverter.viewmodels

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pdfconverter.apiInstance.RetrofitClient
import com.example.pdfconverter.helper.createFileFromUri
import com.example.pdfconverter.manager.SocketManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ConnectToWebViewModel : ViewModel() {

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _buttonColor = MutableStateFlow("red")
    val buttonColor = _buttonColor.asStateFlow()


    private val _currentCode = MutableStateFlow<String?>(null)
    val currentCode = _currentCode.asStateFlow()

    fun connect(code: String) {
        if (code.isBlank()) {
            _connectionStatus.value = "Invalid Code"
            _buttonColor.value = "red"
            return
        }

        _currentCode.value = code

        SocketManager.init("http://192.168.100.121:9000")
        SocketManager.connect()

        SocketManager.onConnected {
            SocketManager.send("join_code", code)
            setupActionListener()
        }

        SocketManager.onEvent("paired") { _ ->
            viewModelScope.launch {
                _connectionStatus.value = "Connected"
                _buttonColor.value = "green"
            }
        }

        SocketManager.onEvent("code_expired") { _ ->
            viewModelScope.launch {
                _connectionStatus.value = "Code expired or disconnected"
                _buttonColor.value = "red"
                SocketManager.disconnect()
            }
        }


        SocketManager.onDisconnected {
            viewModelScope.launch {
                _connectionStatus.value = "Disconnected"
                _buttonColor.value = "red"
            }
        }
    }


    fun disconnect() {
        SocketManager.disconnect()
        _connectionStatus.value = "Disconnected"
        _buttonColor.value = "red"
    }

    private val _receivedAction = MutableStateFlow<Pair<String, String>?>(null)
    val receivedAction: StateFlow<Pair<String, String>?> = _receivedAction.asStateFlow()

    fun setupActionListener() {
        SocketManager.onEvent("action") { args ->
            Log.d("ConnectToViewModel", "onEvent action args: ${args.contentToString()}")

            if (args.isNotEmpty()) {
                val jsonObject = args[0]
                if (jsonObject is org.json.JSONObject) {
                    val actionType = jsonObject.optString("action", null)
                    val fileName = jsonObject.optString("fileName", null)

                    Log.d("ConnectToViewModel", "actionType: $actionType, fileName: $fileName")

                    if (actionType != null && fileName != null) {
                        viewModelScope.launch {
                            _receivedAction.value = Pair(actionType, fileName)
                            Log.d("ConnectToViewModel", "Updated _receivedAction with $actionType, $fileName")
                        }
                    }
                } else {
                    Log.d("ConnectToViewModel", "Unexpected data type for args[0]: ${jsonObject::class.java}")
                }
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.FROYO)
    fun webDownloadAndSaveFile(
        context: Context,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        val code = _currentCode.value
        if (code.isNullOrEmpty()) {
            Log.e("DownloadDebug", "Code is empty, cannot download")
            onResult(null)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiServiceWeb.downloadFile(code, fileName)

                if (!response.isSuccessful) {
                    Log.e("DownloadDebug", "HTTP error: ${response.code()} ${response.message()}")
                    withContext(Dispatchers.Main) { onResult(null) }
                    return@launch
                }

                val body = response.body()
                if (body == null) {
                    Log.e("DownloadDebug", "Response body is null")
                    withContext(Dispatchers.Main) { onResult(null) }
                    return@launch
                }

                val fileUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ → Use MediaStore (Scoped Storage)
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            body.byteStream().copyTo(output)
                        }
                    }
                    uri
                } else {
                    // Android 9 and below → Legacy storage
                    val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsFolder.exists()) downloadsFolder.mkdirs()
                    val file = File(downloadsFolder, fileName)
                    body.byteStream().buffered().use { input ->
                        file.outputStream().buffered().use { output ->
                            input.copyTo(output)
                        }
                    }
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                }

                withContext(Dispatchers.Main) {
                    onResult(fileUri)
                }

            } catch (e: Exception) {
                Log.e("DownloadDebug", "Exception occurred: ${e.message}", e)
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }

    fun uploadProcessedFile(
        context: Context,
        fileUri: Uri,
        fileName: String,
        onResult: (Boolean) -> Unit
    ) {
        val code = _currentCode.value
        if (code.isNullOrEmpty()) {
            onResult(false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = createFileFromUri(context, fileUri)

                val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"

                val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    fileName,   // use user-entered fileName here
                    requestBody
                )

                val response = RetrofitClient.apiServiceWeb.uploadProcessedFile(code, multipartBody)

                withContext(Dispatchers.Main) {
                    onResult(response.isSuccessful)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }



    fun clearReceivedAction() {
        _receivedAction.value = null
    }


}