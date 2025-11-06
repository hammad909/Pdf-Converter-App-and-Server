package com.example.pdfconverter.repository

import com.example.pdfconverter.apiInstance.RetrofitClient
import com.example.pdfconverter.dataClasses.UploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import okhttp3.ResponseBody
import com.example.pdfconverter.helper.parseErrorMessage
import retrofit2.Response

class UploadRepository {

    suspend fun uploadPdfFile(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadPdf(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPdfForTxt(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadTxt(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun uploadPdfForPpt(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadPpt(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPdfForHtml(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadHtml(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun uploadPdfForRtf(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadRtf(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFileFromServer(serverFileName: String): Response<ResponseBody> {
        return RetrofitClient.apiService.downloadFile(serverFileName)
    }

    suspend fun uploadTxtForPdf(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("text/plain".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadTxtToPdf(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadRtfForPdf(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/rtf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadRtfToPdf(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadPptxForPdf(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody(
                "application/vnd.openxmlformats-officedocument.presentationml.presentation".toMediaTypeOrNull()
            )
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadPptxToPdf(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadHtmlForPdf(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("text/html".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadHtmlToPdf(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadDocxForPdf(file: File): Result<UploadResponse> {
        return try {
            val requestFile = file.asRequestBody("application/vnd.openxmlformats-officedocument.wordprocessingml.document".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.apiService.uploadDocxToPdf(multipartBody)

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val message = parseErrorMessage(response)
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
