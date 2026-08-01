package com.gokcank.curalis.core.di

import com.gokcank.curalis.data.repository_impl.MedicationRepositoryImpl
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
        reminderRepositoryImpl: com.gokcank.curalis.data.repository_impl.ReminderRepositoryImpl
    ): com.gokcank.curalis.domain.repository.ReminderRepository
}
