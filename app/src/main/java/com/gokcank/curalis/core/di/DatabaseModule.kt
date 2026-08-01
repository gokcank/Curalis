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
        ).build()
    }

    @Provides
    @Singleton
    fun provideMedicationDao(db: CuralisDatabase): MedicationDao {
        return db.medicationDao
    }
}
