package com.sathsara.ecbtracker.data.service

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.sathsara.ecbtracker.data.model.Entry
import com.sathsara.ecbtracker.data.model.Payment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun exportExcel(entries: List<Entry>, payments: List<Payment>, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val workbook = XSSFWorkbook()
            
            // --- Sheet 1: Readings ---
            val readingsSheet = workbook.createSheet("Readings")
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.AQUA.index // Closest to Cyan
                fillPattern = FillPatternType.SOLID_FOREGROUND
                val font = workbook.createFont().apply {
                    color = IndexedColors.BLACK.index
                    bold = true
                }
                setFont(font)
            }

            // Headers
            var rowNum = 0
            val row = readingsSheet.createRow(rowNum++)
            val headers = listOf("Date", "Time", "Meter Reading", "Units Used", "Appliances", "Note", "Photo URL")
            headers.forEachIndexed { i, title ->
                val cell = row.createCell(i)
                cell.setCellValue(title)
                cell.cellStyle = headerStyle
            }

            // Data
            entries.forEach { entry ->
                val dataRow = readingsSheet.createRow(rowNum++)
                dataRow.createCell(0).setCellValue(entry.date)
                dataRow.createCell(1).setCellValue(entry.time)
                dataRow.createCell(2).setCellValue(entry.unit)
                dataRow.createCell(3).setCellValue(entry.used)
                dataRow.createCell(4).setCellValue(entry.appliances?.joinToString(" | ") ?: "")
                dataRow.createCell(5).setCellValue(entry.note ?: "")
                dataRow.createCell(6).setCellValue(entry.imgUrl ?: "")
            }

            // Auto-size columns
            headers.indices.forEach { readingsSheet.autoSizeColumn(it) }

            // --- Sheet 2: Payments ---
            val paymentsSheet = workbook.createSheet("Payments")
            var pRowNum = 0
            val pRow = paymentsSheet.createRow(pRowNum++)
            val pHeaders = listOf("Month", "Bill Amount", "Paid Amount", "Status", "Bank", "Note")
            pHeaders.forEachIndexed { i, title ->
                val cell = pRow.createCell(i)
                cell.setCellValue(title)
                cell.cellStyle = headerStyle
            }

            payments.forEach { payment ->
                val dataRow = paymentsSheet.createRow(pRowNum++)
                dataRow.createCell(0).setCellValue(payment.month)
                dataRow.createCell(1).setCellValue(payment.billAmount)
                dataRow.createCell(2).setCellValue(payment.paidAmount)
                dataRow.createCell(3).setCellValue(if (payment.paid) "Paid" else "Pending")
                dataRow.createCell(4).setCellValue(payment.bank ?: "")
                dataRow.createCell(5).setCellValue(payment.note ?: "")
            }
            pHeaders.indices.forEach { paymentsSheet.autoSizeColumn(it) }

            saveToDownloads("$fileName.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") { outputStream ->
                workbook.write(outputStream)
                workbook.close()
            }
        }
    }

    suspend fun exportCsv(entries: List<Entry>, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        Result.runCatching {
            saveToDownloads("$fileName.csv", "text/csv") { outputStream ->
                val writer = OutputStreamWriter(outputStream)
                writer.append("Date,Time,Meter Reading,Units Used,Appliances,Note\n")
                
                entries.forEach { entry ->
                    val appliancesStr = entry.appliances?.joinToString("|") ?: ""
                    val safeNote = entry.note?.replace(",", ";") ?: ""
                    writer.append("${entry.date},${entry.time},${entry.unit},${entry.used},${appliancesStr},${safeNote}\n")
                }
                writer.flush()
            }
        }
    }

    suspend fun exportPdf(entries: List<Entry>, fileName: String, dateRange: String): Result<String> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val textPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            // Draw Header
            canvas.drawText("ECB Tracker Report", 50f, 60f, titlePaint)
            canvas.drawText(dateRange, 50f, 90f, textPaint)
            canvas.drawLine(50f, 100f, 545f, 100f, linePaint)

            // Draw Table Header
            var yPos = 130f
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Date", 50f, yPos, textPaint)
            canvas.drawText("Reading", 180f, yPos, textPaint)
            canvas.drawText("Used kWh", 280f, yPos, textPaint)
            canvas.drawText("Appliances", 380f, yPos, textPaint)
            canvas.drawLine(50f, yPos + 10f, 545f, yPos + 10f, linePaint)

            // Draw Rows
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            yPos += 30f

            entries.take(25).forEach { entry -> // Simplified for brevity in one page
                canvas.drawText(entry.date, 50f, yPos, textPaint)
                canvas.drawText(entry.unit.toString(), 180f, yPos, textPaint)
                canvas.drawText(entry.used.toString(), 280f, yPos, textPaint)
                val apps = entry.appliances?.joinToString(", ")?.take(20) ?: "-"
                canvas.drawText(apps, 380f, yPos, textPaint)
                
                yPos += 25f
            }
            
            if (entries.size > 25) {
                canvas.drawText("... and ${entries.size - 25} more records.", 50f, yPos + 20f, textPaint)
            }

            pdfDocument.finishPage(page)

            saveToDownloads("$fileName.pdf", "application/pdf") { outputStream ->
                pdfDocument.writeTo(outputStream)
                pdfDocument.close()
            }
        }
    }

    private fun saveToDownloads(fileName: String, mimeType: String, writer: (OutputStream) -> Unit): String {
        val resolver = context.contentResolver
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { writer(it) }
                return "Saved to Downloads folder"
            }
        } else {
            // Legacy for API 28 and below
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            file.outputStream().use { writer(it) }
            return "Saved to Downloads folder"
        }
        throw Exception("Failed to save file")
    }
}
