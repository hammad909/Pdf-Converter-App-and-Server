package com.example.pdfconverter.dataClasses

import android.net.Uri


data class SplitPageData(
    val pageNumber: Int,
    val fileName: String,
    val pdfBytes: ByteArray,
    val uri: Uri
)
 {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SplitPageData

        if (pageNumber != other.pageNumber) return false
        if (fileName != other.fileName) return false
        if (!pdfBytes.contentEquals(other.pdfBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pageNumber
        result = 31 * result + fileName.hashCode()
        result = 31 * result + pdfBytes.contentHashCode()
        return result
    }
}
