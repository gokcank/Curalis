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
import com.gokcank.curalis.core.notification.NotificationPreferences
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.VitalRepository
import com.gokcank.curalis.domain.usecase.DoseAdherence
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface PdfReportGeneratorEntryPoint {
    fun pdfReportGenerator(): PdfReportGenerator
}

/** Rapor önizlemesinde gösterilen özet rozeti için hesaplanan uyum istatistikleri. */
data class ReportSummary(
    val adherencePercentage: Int?,
    val totalCount: Int,
    val takenCount: Int,
    val skippedCount: Int,
    val missedCount: Int
)

data class ReportResult(
    val file: File,
    val summary: ReportSummary
)

@Singleton
class PdfReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val medicationRepository: MedicationRepository,
    private val vitalRepository: VitalRepository,
    private val getRemindersBetweenDatesUseCase: GetRemindersBetweenDatesUseCase,
    private val notificationPreferences: NotificationPreferences
) {

    /**
     * Raporu üretir ve dosyayı döndürür; paylaşmaz. Önizleme için kullanılır.
     * @param startMillis Uyum istatistiklerinin hesaplanacağı aralığın başlangıcı; verilmezse
     * son 30 gün kullanılır.
     * @param medicationIds Yalnızca bu ilaçların dahil edilmesi için filtre; null veya boş ise
     * tüm ilaçlar dahil edilir.
     * @param includeAdherenceSummary/includeMedicationList/includeVitals Paylaşılan PDF'in
     * hangi bölümleri içereceğini seçtirir.
     */
    suspend fun generateReport(
        startMillis: Long = System.currentTimeMillis() - (30L * 24 * 3600 * 1000),
        endMillis: Long = System.currentTimeMillis(),
        medicationIds: Set<String>? = null,
        includeAdherenceSummary: Boolean = true,
        includeMedicationList: Boolean = true,
        includeVitals: Boolean = true
    ): ReportResult = withContext(Dispatchers.IO) {
        val medicationsDeferred = async { medicationRepository.getAllMedications().firstOrNull() ?: emptyList() }
        val vitalsDeferred = async { vitalRepository.getAllVitals().firstOrNull() ?: emptyList() }

        val rangeRemindersDeferred = async {
            getRemindersBetweenDatesUseCase(startMillis, endMillis).firstOrNull() ?: emptyList()
        }

        val allMedications = medicationsDeferred.await()
        val medications = if (medicationIds.isNullOrEmpty()) allMedications else allMedications.filter { it.id in medicationIds }
        val vitals = vitalsDeferred.await()
        // Plasebo dozları (CYCLIC dinlenme günleri) uyum istatistiklerine dahil edilmez —
        // bkz. Reminder.isPlacebo.
        val allRangeReminders = rangeRemindersDeferred.await().filterNot { it.isPlacebo }
        val rangeReminders = if (medicationIds.isNullOrEmpty()) allRangeReminders else allRangeReminders.filter { it.medicationId in medicationIds }

        val lateThreshold = notificationPreferences.lateDoseThresholdMinutes
        val overall = DoseAdherence.summarize(rangeReminders, lateThreshold)
        // İlaç başına özet; aralıkta sonuçlanmış dozu olmayan ilaçlar tabloya girmez.
        val remindersByMedication = rangeReminders.groupBy { it.medicationId }
        val medicationSummaries = medications.mapNotNull { med ->
            val summary = DoseAdherence.summarize(remindersByMedication[med.id].orEmpty(), lateThreshold)
            if (summary.total > 0) med.name to summary else null
        }

        val file = generateHealthReportPdf(
            medications = medications,
            vitals = if (includeVitals) vitals else emptyList(),
            overall = overall,
            medicationSummaries = medicationSummaries,
            lateThresholdMinutes = lateThreshold,
            rangeStartMillis = startMillis,
            rangeEndMillis = endMillis,
            includeAdherenceSummary = includeAdherenceSummary,
            includeMedicationList = includeMedicationList
        )

        ReportResult(
            file = file,
            summary = ReportSummary(overall.percentage, overall.total, overall.taken, overall.skipped, overall.missed)
        )
    }

    /** Kullanıcı önizlemeyi gördükten sonra dosyayı paylaşım seçicisiyle gönderir. */
    fun shareReport(activityContext: Context, pdfFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                activityContext,
                "${activityContext.packageName}.fileprovider",
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
            activityContext.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activityContext, "Hata: PDF raporu paylaşılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    fun generateHealthReportPdf(
        medications: List<Medication>,
        vitals: List<Vital> = emptyList(),
        overall: DoseAdherence.Summary? = null,
        /** İlaç adı ve o ilacın rapor aralığındaki doz özeti. */
        medicationSummaries: List<Pair<String, DoseAdherence.Summary>> = emptyList(),
        lateThresholdMinutes: Int = NotificationPreferences.DEFAULT_LATE_DOSE_THRESHOLD_MINUTES,
        rangeStartMillis: Long = System.currentTimeMillis() - (30L * 24 * 3600 * 1000),
        rangeEndMillis: Long = System.currentTimeMillis(),
        includeAdherenceSummary: Boolean = true,
        includeMedicationList: Boolean = true
    ): File {
        val pdfDocument = PdfDocument()

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
        val tableHeaderPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val notePaint = Paint().apply {
            color = Color.GRAY
            textSize = 8.5f
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // İçerik sayfaya sığmayınca yeni sayfaya geçilir; önizleme bütün sayfaları gösterir.
        var pageNumber = 0
        lateinit var page: PdfDocument.Page
        lateinit var canvas: Canvas
        var y = 0f

        fun startPage() {
            pageNumber++
            page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            canvas = page.canvas
            y = 40f
        }

        fun finishPage() {
            canvas.drawText(
                "Bu rapor Curalis Sağlık Asistanı tarafından cihaz üzerinde güvenle üretilmiştir. — Sayfa $pageNumber",
                40f, FOOTER_Y, bodyPaint
            )
            pdfDocument.finishPage(page)
        }

        /** Sonraki [height] birimlik içerik bu sayfaya sığmıyorsa yeni sayfaya geçer. */
        fun ensureSpace(height: Float) {
            if (y + height > CONTENT_BOTTOM) {
                finishPage()
                startPage()
            }
        }

        startPage()
        var sectionNumber = 1

        // Title & Header
        canvas.drawText("CURALİS SAĞLIK & İLAÇ UYUM RAPORU", 40f, y, titlePaint)
        y += 24f

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.forLanguageTag("tr"))
        val currentDateStr = dateFormat.format(Date())
        canvas.drawText("Rapor Oluşturma Tarihi: $currentDateStr", 40f, y, bodyPaint)
        y += 18f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Adherence Section
        if (includeAdherenceSummary && overall != null) {
            val takenText = if (overall.late > 0) "${overall.taken} (${overall.late} geç)" else "${overall.taken}"

            val rangeDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.forLanguageTag("tr"))
            canvas.drawText("${sectionNumber++}. İlaç Kullanım Uyum Özet İstatistiği", 40f, y, subTitlePaint)
            y += 18f
            canvas.drawText(
                "• Rapor Aralığı: ${rangeDateFormat.format(Date(rangeStartMillis))} – ${rangeDateFormat.format(Date(rangeEndMillis))}",
                40f, y, bodyPaint
            )
            y += 16f
            canvas.drawText("• Toplam İlaç Vakti: ${overall.total} | Alınan: $takenText | Atlanan: ${overall.skipped} | Kaçırılan: ${overall.missed}", 40f, y, bodyPaint)
            y += 16f
            // Geç alınan doz yarım sayılır (bkz. DoseAdherence); değerlendirilecek doz yoksa oran yazılmaz.
            canvas.drawText("• Genel İlaç Uyum Oranı: ${overall.percentage?.let { "%$it" } ?: "—"}", 40f, y, bodyPaint)
            y += 20f

            if (medicationSummaries.isNotEmpty()) {
                fun drawSummaryHeader() {
                    canvas.drawText("İlaç", 40f, y, tableHeaderPaint)
                    canvas.drawText("Alınan", 230f, y, tableHeaderPaint)
                    canvas.drawText("Geç", 280f, y, tableHeaderPaint)
                    canvas.drawText("Atlanan", 315f, y, tableHeaderPaint)
                    canvas.drawText("Kaçırılan", 370f, y, tableHeaderPaint)
                    canvas.drawText("Uyum", 435f, y, tableHeaderPaint)
                    canvas.drawText("Ort. Gecikme", 480f, y, tableHeaderPaint)
                    y += 6f
                    canvas.drawLine(40f, y, 555f, y, linePaint)
                    y += 14f
                }

                ensureSpace(60f)
                canvas.drawText("İlaç Bazında Doz Özeti", 40f, y, tableHeaderPaint)
                y += 16f
                drawSummaryHeader()
                medicationSummaries.forEach { (name, summary) ->
                    if (y + 16f > CONTENT_BOTTOM) {
                        finishPage()
                        startPage()
                        drawSummaryHeader()
                    }
                    canvas.drawText(name.take(32), 40f, y, bodyPaint)
                    canvas.drawText("${summary.taken}", 230f, y, bodyPaint)
                    canvas.drawText("${summary.late}", 280f, y, bodyPaint)
                    canvas.drawText("${summary.skipped}", 315f, y, bodyPaint)
                    canvas.drawText("${summary.missed}", 370f, y, bodyPaint)
                    canvas.drawText(summary.percentage?.let { "%$it" } ?: "—", 435f, y, bodyPaint)
                    canvas.drawText(formatDelay(summary.averageDelayMinutes), 480f, y, bodyPaint)
                    y += 16f
                }
                ensureSpace(28f)
                y += 2f
                canvas.drawText(
                    "Geç: planlanan saatinden ${formatThresholdAblative(lateThresholdMinutes)} fazla sonra alınan dozlar; uyum oranına yarım doz olarak girer.",
                    40f, y, notePaint
                )
                y += 12f
                canvas.drawText(
                    "Ort. Gecikme: alınma saati kayıtlı dozların planlanan saatten ortalama ne kadar sonra alındığı (erken alınanlar 0 sayılır).",
                    40f, y, notePaint
                )
                y += 8f
            }

            y += 12f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
        }

        // Medication Cabinet List
        if (includeMedicationList) {
            ensureSpace(60f)
            canvas.drawText("${sectionNumber++}. Mevcut İlaç Dolabı Listesi (${medications.size} Kayıtlı İlaç)", 40f, y, subTitlePaint)
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
                    ensureSpace(18f)
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
        }

        // Vitals History Section
        if (vitals.isNotEmpty()) {
            ensureSpace(60f)
            canvas.drawText("${sectionNumber++}. Son Ölçümler Geçmişi (${vitals.size} Ölçüm)", 40f, y, subTitlePaint)
            y += 20f
            vitals.take(8).forEach { vital ->
                ensureSpace(16f)
                val vDateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(vital.timeInMillis))
                val valStr = if (vital.value2 != null) "${vital.value1} / ${vital.value2}" else "${vital.value1}"
                canvas.drawText("• ${vital.type.name}: $valStr ${vital.unit} ($vDateStr)", 40f, y, bodyPaint)
                y += 16f
            }
            y += 10f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
        }

        finishPage()

        val reportFile = File(context.cacheDir, "Curalis_Saglik_Raporu.pdf")
        FileOutputStream(reportFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return reportFile
    }

    /** "25 dk", "1 sa", "2 sa 10 dk"; değer yoksa "—". */
    private fun formatDelay(minutes: Int?): String = when {
        minutes == null -> "—"
        minutes < 60 -> "$minutes dk"
        minutes % 60 == 0 -> "${minutes / 60} sa"
        else -> "${minutes / 60} sa ${minutes % 60} dk"
    }

    /** Dipnot için: "30 dakikadan", "1 saatten", "2 saatten". */
    private fun formatThresholdAblative(minutes: Int): String =
        if (minutes % 60 == 0) "${minutes / 60} saatten" else "$minutes dakikadan"

    private companion object {
        // A4, 72 dpi
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val FOOTER_Y = 800f
        /** Bu çizginin altına içerik yazılmaz; altbilgiye yer kalır. */
        const val CONTENT_BOTTOM = 780f
    }
}
