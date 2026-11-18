package com.example.data.storage

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.example.data.model.AcademicConstants
import com.example.data.model.NoteFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class LocalStorageService(private val context: Context) {

    private val uploadsDir: File by lazy {
        File(context.filesDir, "uploads").apply {
            if (!exists()) mkdirs()
        }
    }

    private val allowedExtensions = setOf("pdf", "png", "jpg", "jpeg", "doc", "docx", "txt", "md")

    suspend fun storeUri(uri: Uri): NoteFileItem = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var originalName = "document_${System.currentTimeMillis()}"
        var size = -1L

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) {
                    cursor.getString(nameIdx)?.let { if (it.isNotBlank()) originalName = it }
                }
                if (sizeIdx >= 0) {
                    size = cursor.getLong(sizeIdx)
                }
            }
        }

        if (size > AcademicConstants.MAX_FILE_SIZE_BYTES) {
            throw IllegalArgumentException("File '$originalName' exceeds the 10MB size limit.")
        }

        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        val ext = originalName.substringAfterLast('.', "").lowercase().ifBlank {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.lowercase() ?: "bin"
        }

        if (ext !in allowedExtensions) {
            throw IllegalArgumentException("Unsupported file format '.$ext'. Allowed: PDF, PNG, JPG, DOC, DOCX, TXT, MD.")
        }

        val fileId = UUID.randomUUID().toString()
        val storedFileName = "$fileId.$ext"
        val destFile = File(uploadsDir, storedFileName)

        resolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                val buffer = ByteArray(8192)
                var totalRead = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    totalRead += read
                    if (totalRead > AcademicConstants.MAX_FILE_SIZE_BYTES) {
                        destFile.delete()
                        throw IllegalArgumentException("File '$originalName' exceeds the 10MB size limit.")
                    }
                    output.write(buffer, 0, read)
                }
            }
        } ?: throw IllegalStateException("Unable to read selected file '$originalName'.")

        val finalSize = destFile.length()
        val extractedText = if (ext == "txt" || ext == "md") {
            runCatching { destFile.readText(Charsets.UTF_8).take(12_000) }.getOrDefault("")
        } else {
            ""
        }

        NoteFileItem(
            fileId = fileId,
            originalFileName = originalName,
            storedFileName = storedFileName,
            contentType = mimeType,
            size = finalSize,
            localPath = destFile.absolutePath,
            extractedText = extractedText
        )
    }

    suspend fun createSamplePdf(
        originalFileName: String,
        headerTitle: String,
        subtitle: String,
        sections: List<Pair<String, List<String>>>
    ): NoteFileItem = withContext(Dispatchers.IO) {
        val fileId = UUID.randomUUID().toString()
        val storedFileName = "$fileId.pdf"
        val destFile = File(uploadsDir, storedFileName)

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 44f
        val contentWidth = pageWidth - margin * 2

        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2563EB")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 13.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 52f

        // Header
        canvas.drawText(headerTitle, margin, y, headerPaint)
        y += 20f
        canvas.drawText(subtitle, margin, y, subPaint)
        y += 14f
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 24f

        val textBuilder = StringBuilder()
        textBuilder.appendLine(headerTitle)
        textBuilder.appendLine(subtitle)
        textBuilder.appendLine()

        for ((heading, bullets) in sections) {
            if (y > pageHeight - 90f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 52f
            }

            canvas.drawText(heading, margin, y, sectionPaint)
            y += 18f
            textBuilder.appendLine(heading)

            for (bullet in bullets) {
                val wrapped = wrapText("• $bullet", bodyPaint, contentWidth)
                for (line in wrapped) {
                    if (y > pageHeight - 60f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 52f
                    }
                    canvas.drawText(line, margin + 6f, y, bodyPaint)
                    y += 16f
                }
                y += 4f
                textBuilder.appendLine(bullet)
            }
            y += 12f
            textBuilder.appendLine()
        }

        pdfDocument.finishPage(page)
        FileOutputStream(destFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        NoteFileItem(
            fileId = fileId,
            originalFileName = originalFileName,
            storedFileName = storedFileName,
            contentType = "application/pdf",
            size = destFile.length(),
            localPath = destFile.absolutePath,
            extractedText = textBuilder.toString()
        )
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val test = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(test) <= maxWidth) {
                currentLine = StringBuilder(test)
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    fun deleteFile(localPath: String) {
        runCatching {
            val f = File(localPath)
            if (f.exists()) f.delete()
        }
    }

    suspend fun saveToDownloads(item: NoteFileItem): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val source = File(item.localPath)
            if (!source.exists()) throw IllegalStateException("Source file not found on device.")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, item.originalFileName)
                    put(MediaStore.Downloads.MIME_TYPE, item.contentType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/CloudNotesHub")
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("Could not create file in Downloads.")
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    source.inputStream().use { input -> input.copyTo(out) }
                } ?: throw IllegalStateException("Failed to write to Downloads.")
                "Saved '${item.originalFileName}' to Downloads/CloudNotesHub"
            } else {
                "File ready: ${item.originalFileName}"
            }
        }
    }

    fun buildShareIntent(item: NoteFileItem): Intent? {
        val file = File(item.localPath)
        if (!file.exists()) return null
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = item.contentType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, item.originalFileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    fun buildOpenExternalIntent(item: NoteFileItem): Intent? {
        val file = File(item.localPath)
        if (!file.exists()) return null
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, item.contentType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
