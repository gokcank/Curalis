package com.gokcank.curalis.core.di

import com.gokcank.curalis.data.repository.MedicationRepositoryImpl
import com.gokcank.curalis.domain.repository.MedicationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        medicationRepositoryImpl: MedicationRepositoryImpl
    ): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        reminderRepositoryImpl: com.gokcank.curalis.data.repository.ReminderRepositoryImpl
    ): com.gokcank.curalis.domain.repository.ReminderRepository

    @Binds
    @Singleton
    abstract fun bindDoctorRepository(
        doctorRepositoryImpl: com.gokcank.curalis.data.repository.DoctorRepositoryImpl
    ): com.gokcank.curalis.domain.repository.DoctorRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        appointmentRepositoryImpl: com.gokcank.curalis.data.repository.AppointmentRepositoryImpl
    ): com.gokcank.curalis.domain.repository.AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyContactRepository(
        emergencyContactRepositoryImpl: com.gokcank.curalis.data.repository.EmergencyContactRepositoryImpl
    ): com.gokcank.curalis.domain.repository.EmergencyContactRepository

    @Binds
    @Singleton
    abstract fun bindVitalRepository(
        vitalRepositoryImpl: com.gokcank.curalis.data.repository.VitalRepositoryImpl
    ): com.gokcank.curalis.domain.repository.VitalRepository

    @Binds
    @Singleton
    abstract fun bindStockHistoryRepository(
        stockHistoryRepositoryImpl: com.gokcank.curalis.data.repository.StockHistoryRepositoryImpl
    ): com.gokcank.curalis.domain.repository.StockHistoryRepository

    @Binds
    @Singleton
    abstract fun bindBackupManager(
        localBackupManager: com.gokcank.curalis.data.backup.LocalBackupManager
    ): com.gokcank.curalis.domain.repository.BackupManager
}
