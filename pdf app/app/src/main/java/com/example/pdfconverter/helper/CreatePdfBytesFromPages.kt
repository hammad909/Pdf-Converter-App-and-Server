package com.example.pdfconverter.helper

import com.example.pdfconverter.dataClasses.SplitPageData
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun createPdfBytesFromPages(pages: List<SplitPageData>): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val writer = PdfWriter(byteArrayOutputStream)
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
    return byteArrayOutputStream.toByteArray()
}
