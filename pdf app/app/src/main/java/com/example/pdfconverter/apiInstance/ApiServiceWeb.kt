package com.example.pdfconverter.apiInstance

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiServiceWeb {

        @GET("download/{code}/{fileName}")
        suspend fun downloadFile(
            @Path("code") code: String,
            @Path("fileName") fileName: String
        ): Response<ResponseBody>

        @Multipart
        @POST("upload_processed/{code}")
        suspend fun uploadProcessedFile(
            @Path("code") code: String?,
            @Part file: MultipartBody.Part
        ): Response<Unit>

}