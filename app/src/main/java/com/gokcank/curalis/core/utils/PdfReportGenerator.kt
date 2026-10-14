package com.gokcank.curalis.core.utils

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.gokcank.curalis.core.notification.NotificationPreferences
import com.gokcank.curalis.R
import com.gokcank.curalis.domain.model.FrequencyType
import com.gokcank.curalis.domain.model.Medication
import com.gokcank.curalis.domain.model.Vital
import com.gokcank.curalis.domain.repository.MedicationRepository
import com.gokcank.curalis.domain.repository.VitalRepository
import com.gokcank.curalis.domain.usecase.DoseAdherence
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import com.gokcank.curalis.presentation.vital.displayNameRes
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

/** PDF raporunun dili; rapor oluşturulurken seçilir, varsayılanı uygulamanın dili. */
enum class ReportLanguage(val locale: Locale) {
    TURKISH(Locale.forLanguageTag("tr")),
    ENGLISH(Locale.ENGLISH);

    companion object {
        fun forLocale(locale: Locale): ReportLanguage = if (locale.language == "tr") TURKISH else ENGLISH
    }
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
        includeVitals: Boolean = true,
        language: ReportLanguage = ReportLanguage.forLocale(Locale.getDefault())
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
            includeMedicationList = includeMedicationList,
            language = language
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
            val chooser = Intent.createChooser(shareIntent, activityContext.getString(R.string.pdf_share_chooser_title)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityContext.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activityContext, activityContext.getString(R.string.pdf_share_error), Toast.LENGTH_SHORT).show()
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
        includeMedicationList: Boolean = true,
        language: ReportLanguage = ReportLanguage.TURKISH
    ): File {
        val pdfDocument = PdfDocument()
        // Rapor metinleri uygulamanın değil, seçilen rapor dilinin kaynaklarından okunur.
        val res = context.createConfigurationContext(
            Configuration(context.resources.configuration).apply { setLocale(language.locale) }
        ).resources
        val locale = language.locale
        fun percent(value: Int?): String = value?.let { res.getString(R.string.pdf_percent, it) } ?: "—"
        fun delay(minutes: Int?): String = when {
            minutes == null -> "—"
            minutes < 60 -> res.getString(R.string.pdf_duration_minutes, minutes)
            minutes % 60 == 0 -> res.getString(R.string.pdf_duration_hours, minutes / 60)
            else -> res.getString(R.string.pdf_duration_hours_minutes, minutes / 60, minutes % 60)
        }

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
                res.getString(R.string.pdf_footer, pageNumber),
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
        canvas.drawText(res.getString(R.string.pdf_title), 40f, y, titlePaint)
        y += 24f

        val dateFormat = SimpleDateFormat(res.getString(R.string.pdf_datetime_pattern), locale)
        val currentDateStr = dateFormat.format(Date())
        canvas.drawText(res.getString(R.string.pdf_generated_at, currentDateStr), 40f, y, bodyPaint)
        y += 18f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 20f

        // Adherence Section
        if (includeAdherenceSummary && overall != null) {
            val takenText = if (overall.late > 0) {
                res.getString(R.string.pdf_taken_with_late, overall.taken, overall.late)
            } else {
                "${overall.taken}"
            }

            val rangeDateFormat = SimpleDateFormat(res.getString(R.string.pdf_date_pattern), locale)
            canvas.drawText(res.getString(R.string.pdf_section_adherence, sectionNumber++), 40f, y, subTitlePaint)
            y += 18f
            canvas.drawText(
                res.getString(
                    R.string.pdf_range,
                    rangeDateFormat.format(Date(rangeStartMillis)),
                    rangeDateFormat.format(Date(rangeEndMillis))
                ),
                40f, y, bodyPaint
            )
            y += 16f
            canvas.drawText(
                res.getString(R.string.pdf_totals, overall.total, takenText, overall.skipped, overall.missed),
                40f, y, bodyPaint
            )
            y += 16f
            // Geç alınan doz yarım sayılır (bkz. DoseAdherence); değerlendirilecek doz yoksa oran yazılmaz.
            canvas.drawText(res.getString(R.string.pdf_overall_adherence, percent(overall.percentage)), 40f, y, bodyPaint)
            y += 20f

            if (medicationSummaries.isNotEmpty()) {
                fun drawSummaryHeader() {
                    canvas.drawText(res.getString(R.string.pdf_col_medication), 40f, y, tableHeaderPaint)
                    canvas.drawText(res.getString(R.string.pdf_col_taken), COL_TAKEN, y, tableHeaderPaint)
                    canvas.drawText(res.getString(R.string.pdf_col_late), COL_LATE, y, tableHeaderPaint)
                    canvas.drawText(res.getString(R.string.pdf_col_skipped), COL_SKIPPED, y, tableHeaderPaint)
                    canvas.drawText(res.getString(R.string.pdf_col_missed), COL_MISSED, y, tableHeaderPaint)
                    canvas.drawText(res.getString(R.string.pdf_col_adherence), COL_ADHERENCE, y, tableHeaderPaint)
                    canvas.drawText(res.getString(R.string.pdf_col_avg_delay), COL_DELAY, y, tableHeaderPaint)
                    y += 6f
                    canvas.drawLine(40f, y, 555f, y, linePaint)
                    y += 14f
                }

                ensureSpace(60f)
                canvas.drawText(res.getString(R.string.pdf_per_medication_title), 40f, y, tableHeaderPaint)
                y += 16f
                drawSummaryHeader()
                medicationSummaries.forEach { (name, summary) ->
                    if (y + 16f > CONTENT_BOTTOM) {
                        finishPage()
                        startPage()
                        drawSummaryHeader()
                    }
                    canvas.drawText(name.take(32), 40f, y, bodyPaint)
                    canvas.drawText("${summary.taken}", COL_TAKEN, y, bodyPaint)
                    canvas.drawText("${summary.late}", COL_LATE, y, bodyPaint)
                    canvas.drawText("${summary.skipped}", COL_SKIPPED, y, bodyPaint)
                    canvas.drawText("${summary.missed}", COL_MISSED, y, bodyPaint)
                    canvas.drawText(percent(summary.percentage), COL_ADHERENCE, y, bodyPaint)
                    canvas.drawText(delay(summary.averageDelayMinutes), COL_DELAY, y, bodyPaint)
                    y += 16f
                }
                ensureSpace(28f)
                y += 2f
                canvas.drawText(
                    res.getString(
                        R.string.pdf_late_footnote,
                        if (lateThresholdMinutes % 60 == 0) {
                            res.getString(R.string.pdf_threshold_hours, lateThresholdMinutes / 60)
                        } else {
                            res.getString(R.string.pdf_threshold_minutes, lateThresholdMinutes)
                        }
                    ),
                    40f, y, notePaint
                )
                y += 12f
                canvas.drawText(
                    res.getString(R.string.pdf_delay_footnote),
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
            canvas.drawText(res.getString(R.string.pdf_section_cabinet, sectionNumber++, medications.size), 40f, y, subTitlePaint)
            y += 20f

            canvas.drawText(res.getString(R.string.pdf_col_name), 40f, y, subTitlePaint)
            canvas.drawText(res.getString(R.string.pdf_col_form_dose), 190f, y, subTitlePaint)
            canvas.drawText(res.getString(R.string.pdf_col_frequency), 340f, y, subTitlePaint)
            canvas.drawText(res.getString(R.string.pdf_col_stock), 470f, y, subTitlePaint)
            y += 8f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 16f

            if (medications.isEmpty()) {
                canvas.drawText(res.getString(R.string.pdf_no_medications), 40f, y, bodyPaint)
                y += 20f
            } else {
                medications.forEach { med ->
                    ensureSpace(18f)
                    canvas.drawText(med.name.take(20), 40f, y, bodyPaint)
                    val dosageStr = listOfNotNull(med.dosage, med.unit).joinToString(" ")
                    val formName = if (language == ReportLanguage.TURKISH) med.formType.displayNameTr else med.formType.displayNameEn
                    canvas.drawText("$formName $dosageStr".take(22), 190f, y, bodyPaint)
                    val frequency = when (med.frequencyType) {
                        FrequencyType.DAILY -> res.getString(R.string.freq_every_day_chip)
                        FrequencyType.SPECIFIC_DAYS -> res.getString(R.string.freq_specific_days_chip)
                        FrequencyType.INTERVAL -> res.getString(R.string.freq_every_x_days_chip, med.intervalDays ?: 2)
                        FrequencyType.CYCLIC -> res.getString(R.string.freq_cyclic_chip, med.activeDays ?: 21, med.restDays ?: 7)
                        FrequencyType.AS_NEEDED -> res.getString(R.string.freq_as_needed_chip)
                    }
                    canvas.drawText(frequency.take(22), 340f, y, bodyPaint)
                    val stockText = if (med.isRefillReminderEnabled && med.currentStock != null) {
                        res.getQuantityString(R.plurals.pdf_stock_doses, med.currentStock, med.currentStock)
                    } else {
                        "-"
                    }
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
            canvas.drawText(res.getString(R.string.pdf_section_vitals, sectionNumber++, vitals.size), 40f, y, subTitlePaint)
            y += 20f
            vitals.take(8).forEach { vital ->
                ensureSpace(16f)
                val vDateStr = SimpleDateFormat(res.getString(R.string.pdf_date_time_short_pattern), locale).format(Date(vital.timeInMillis))
                val valStr = if (vital.value2 != null) "${vital.value1} / ${vital.value2}" else "${vital.value1}"
                canvas.drawText("• ${res.getString(vital.type.displayNameRes())}: $valStr ${vital.unit} ($vDateStr)", 40f, y, bodyPaint)
                y += 16f
            }
            y += 10f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
        }

        finishPage()

        val reportFile = File(context.cacheDir, res.getString(R.string.pdf_file_name))
        FileOutputStream(reportFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return reportFile
    }

    private companion object {
        // A4, 72 dpi
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val FOOTER_Y = 800f
        /** Bu çizginin altına içerik yazılmaz; altbilgiye yer kalır. */
        const val CONTENT_BOTTOM = 780f

        // İlaç başına özet tablosunun sütunları; İngilizce başlıklar daha uzun olduğu için
        // uyum ve gecikme sütunları sağa alındı.
        const val COL_TAKEN = 230f
        const val COL_LATE = 280f
        const val COL_SKIPPED = 315f
        const val COL_MISSED = 370f
        const val COL_ADHERENCE = 425f
        const val COL_DELAY = 490f
    }
}
