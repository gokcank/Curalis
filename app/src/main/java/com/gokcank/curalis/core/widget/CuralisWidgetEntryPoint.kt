package com.gokcank.curalis.core.widget

import com.gokcank.curalis.domain.usecase.AcknowledgeReminderUseCase
import com.gokcank.curalis.domain.usecase.GetMedicationsUseCase
import com.gokcank.curalis.domain.usecase.GetRemindersBetweenDatesUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * GlanceAppWidget, standart bir Android bileşeni (Activity/Fragment/@AndroidEntryPoint
 * Receiver) olmadığı için alan enjeksiyonu yerine EntryPointAccessors kullanılır —
 * PdfReportGeneratorEntryPoint ile aynı desen (bkz. PdfReportGenerator.kt).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface CuralisWidgetEntryPoint {
    fun getRemindersBetweenDatesUseCase(): GetRemindersBetweenDatesUseCase
    fun getMedicationsUseCase(): GetMedicationsUseCase
    fun acknowledgeReminderUseCase(): AcknowledgeReminderUseCase
}
