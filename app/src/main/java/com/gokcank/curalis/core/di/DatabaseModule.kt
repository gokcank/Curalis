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
        // DİKKAT: Burada kasıtlı olarak fallbackToDestructiveMigration() kullanılmıyor.
        // Bu, kullanıcı verisi (ilaç/randevu/ölçüm geçmişi) barındıran canlı veritabanı.
        // Sürüm numarası bir sonraki güncellemede artarsa ve karşılığında bir Migration
        // tanımlanmamışsa, Room burada veriyi sessizce silmek yerine çökerek geliştiriciyi
        // gerçek bir migration yazmaya zorlar (bkz. schemas/ klasörü, room.schemaLocation).
        return Room.databaseBuilder(
            app,
            CuralisDatabase::class.java,
            CuralisDatabase.DATABASE_NAME
        )
        .addMigrations(*com.gokcank.curalis.data.local.CURALIS_MIGRATIONS)
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
    fun provideStockHistoryDao(db: CuralisDatabase): com.gokcank.curalis.data.local.dao.StockHistoryDao {
        return db.stockHistoryDao
    }

    @Provides
    @Singleton
    fun provideMedicationDictionaryDatabase(app: Application): com.gokcank.curalis.data.local.MedicationDictionaryDatabase {
        // Bu veritabanı kullanıcı verisi içermiyor; her güncellemede assets'ten yeniden
        // kopyalanan salt okunur bir ilaç sözlüğü. Bu yüzden yıkıcı migration burada güvenli.
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
