package com.example.pdfconverter.apiInstance

import com.example.pdfconverter.dataClasses.UploadResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface ApiService {

    @Multipart
    @POST("/upload/")
    suspend fun uploadPdf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @Multipart
    @POST("/pdf/upload/ppt/")
    suspend fun uploadPpt(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @Multipart
    @POST("/pdf/upload/txt/")
    suspend fun uploadTxt(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @Multipart
    @POST("/pdf/upload/html/")
    suspend fun uploadHtml(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @Multipart
    @POST("/pdf/upload/rtf/")
    suspend fun uploadRtf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>


    @Multipart
    @POST("/docx/upload/docx_to_pdf/")
    suspend fun uploadDocxToPdf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @Multipart
    @POST("/html/upload/html_to_pdf/")
    suspend fun uploadHtmlToPdf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @Multipart
    @POST("/pptx/upload/ppt_to_pdf/")
    suspend fun uploadPptxToPdf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>


    @Multipart
    @POST("/rtf/upload/rtf_to_pdf/")
    suspend fun uploadRtfToPdf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>


    @Multipart
    @POST("/txt/upload/txt_to_pdf/")
    suspend fun uploadTxtToPdf(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>



    @GET("/download/{file_name}")
    @Streaming
    suspend fun downloadFile(@Path("file_name") fileName: String): Response<ResponseBody>
}
