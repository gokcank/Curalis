package com.gokcank.curalis.core.di

import android.app.Application
import androidx.room.Room
import com.gokcank.curalis.data.local.CuralisDatabase
import com.gokcank.curalis.data.local.dao.MedicationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCuralisDatabase(app: Application): CuralisDatabase {
        return Room.databaseBuilder(
            app,
            CuralisDatabase::class.java,
            CuralisDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideMedicationDao(db: CuralisDatabase): MedicationDao {
        return db.medicationDao
    }

    @Provides
    @Singleton
    fun provideReminderDao(db: CuralisDatabase): com.gokcank.curalis.data.local.dao.ReminderDao {
        return db.reminderDao
    }

    @Provides
    @Singleton
    fun provideDoctorDao(db: CuralisDatabase): com.gokcank.curalis.data.local.dao.DoctorDao {
        return db.doctorDao
    }

    @Provides
    @Singleton
    fun provideAppointmentDao(db: CuralisDatabase): com.gokcank.curalis.data.local.dao.AppointmentDao {
        return db.appointmentDao
    }

    @Provides
    @Singleton
    fun provideVitalDao(db: CuralisDatabase): com.gokcank.curalis.data.local.dao.VitalDao {
        return db.vitalDao
    }

    @Provides
    @Singleton
    fun provideMedicationDictionaryDatabase(app: Application): com.gokcank.curalis.data.local.MedicationDictionaryDatabase {
        return Room.databaseBuilder(
            app,
            com.gokcank.curalis.data.local.MedicationDictionaryDatabase::class.java,
            com.gokcank.curalis.data.local.MedicationDictionaryDatabase.DATABASE_NAME
        )
        .createFromAsset("database/medications.db")
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideDrugDao(db: com.gokcank.curalis.data.local.MedicationDictionaryDatabase): com.gokcank.curalis.data.local.dao.DrugDao {
        return db.drugDao
    }
}
