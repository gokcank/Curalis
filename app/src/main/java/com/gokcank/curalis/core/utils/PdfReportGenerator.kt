package com.gokcank.curalis.core.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Vital
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun generateHealthReportPdf(
        medications: List<Medication>,
        vitals: List<Vital> = emptyList(),
        takenCount: Int = 0,
        skippedCount: Int = 0,
        missedCount: Int = 0
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size at 72 dpi
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#006A6A")
            textSize = 20f
            isFakeBoldText = true
            typeface = Typeface.DEFAULT_BOLD
        }
        val subTitlePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 13f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 40f

        // Title & Header
        canvas.drawText("CURALİS SAĞLIK & İLAÇ UYUM RAPORU", 40f, y, titlePaint)
        y += 24f

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr"))
        val currentDateStr = dateFormat.format(Date())
        canvas.drawText("Rapor Oluşturma Tarihi: $currentDateStr", 40f, y, bodyPaint)
        y += 18f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Adherence Section
        val totalScheduled = takenCount + skippedCount + missedCount
        val adherenceRate = if (totalScheduled > 0) (takenCount * 100) / totalScheduled else 100

        canvas.drawText("1. İlaç Kullanım Uyum Özet İstatistiği", 40f, y, subTitlePaint)
        y += 18f
        canvas.drawText("• Toplam İlaç Vakti: $totalScheduled | Alınan: $takenCount | Atlanan: $skippedCount | Kaçırılan: $missedCount", 40f, y, bodyPaint)
        y += 16f
        canvas.drawText("• Genel İlaç Uyum Oranı: %$adherenceRate", 40f, y, bodyPaint)
        y += 20f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Medication Cabinet List
        canvas.drawText("2. Mevcut İlaç Dolabı Listesi (${medications.size} Kayıtlı İlaç)", 40f, y, subTitlePaint)
        y += 20f

        canvas.drawText("İlaç Adı", 40f, y, subTitlePaint)
        canvas.drawText("Form & Doz", 190f, y, subTitlePaint)
        canvas.drawText("Sıklık", 340f, y, subTitlePaint)
        canvas.drawText("Kalan Stok", 470f, y, subTitlePaint)
        y += 8f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 16f

        if (medications.isEmpty()) {
            canvas.drawText("Kayıtlı ilaç bulunmamaktadır.", 40f, y, bodyPaint)
            y += 20f
        } else {
            medications.forEach { med ->
                if (y > 740f) return@forEach
                canvas.drawText(med.name.take(20), 40f, y, bodyPaint)
                val dosageStr = listOfNotNull(med.dosage, med.unit).joinToString(" ")
                canvas.drawText("${med.formType.displayNameTr} $dosageStr".take(22), 190f, y, bodyPaint)
                canvas.drawText(med.frequencyType.name, 340f, y, bodyPaint)
                val stockText = if (med.isRefillReminderEnabled && med.currentStock != null) "${med.currentStock} Doz" else "-"
                canvas.drawText(stockText, 470f, y, bodyPaint)
                y += 18f
            }
        }

        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Vitals History Section
        if (vitals.isNotEmpty()) {
            canvas.drawText("3. Son Ölçümler Geçmişi (${vitals.size} Ölçüm)", 40f, y, subTitlePaint)
            y += 20f
            vitals.take(8).forEach { vital ->
                if (y > 780f) return@forEach
                val vDateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(vital.timeInMillis))
                val valStr = if (vital.value2 != null) "${vital.value1} / ${vital.value2}" else "${vital.value1}"
                canvas.drawText("• ${vital.type.name}: $valStr ${vital.unit} ($vDateStr)", 40f, y, bodyPaint)
                y += 16f
            }
            y += 10f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
        }

        // Footer
        canvas.drawText("Bu rapor Curalis Sağlık Asistanı tarafından cihaz üzerinde güvenle üretilmiştir.", 40f, 800f, bodyPaint)

        pdfDocument.finishPage(page)

        val reportFile = File(context.cacheDir, "Curalis_Saglik_Raporu.pdf")
        FileOutputStream(reportFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return reportFile
    }

    fun shareReport(context: Context, medications: List<Medication>, vitals: List<Vital> = emptyList()) {
        try {
            val pdfFile = generateHealthReportPdf(medications, vitals)
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Curalis Sağlık Raporu (PDF)").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "📄 PDF Sağlık Raporu oluşturuldu", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Hata: PDF raporu açılamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
