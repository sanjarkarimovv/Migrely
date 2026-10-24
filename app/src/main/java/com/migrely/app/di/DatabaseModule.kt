package com.migrely.app.di

import android.content.Context
import androidx.room.Room
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.attack.datasource.AttackDataSourceImpl
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSourceImpl
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.medication.dao.MedicationDao
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.datasource.medication.datasource.MedicationDataSourceImpl
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.tag.dao.TagDao
import com.migrely.app.datasource.tag.datasource.TagDataSource
import com.migrely.app.datasource.tag.datasource.TagDataSourceImpl
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    // No fallbackToDestructiveMigration: an app update must never delete logged attacks (BRD EC-STO-01).
    @Provides
    @Singleton
    fun providesMigrelyDatabase(@ApplicationContext context: Context): MigrelyDatabase =
        Room.databaseBuilder(context, MigrelyDatabase::class.java, MigrelyDatabase.NAME).build()

    @Provides
    fun providesAttackDao(database: MigrelyDatabase): AttackDao = database.attackDao()

    @Provides
    fun providesAttackDataSource(attackDao: AttackDao): AttackDataSource =
        AttackDataSourceImpl(attackDao, AttackLocalMapper())

    @Provides
    fun providesTagDao(database: MigrelyDatabase): TagDao = database.tagDao()

    @Provides
    fun providesTagDataSource(tagDao: TagDao): TagDataSource = TagDataSourceImpl(
        tagDao,
        CustomTagLocalMapper()
    )

    @Provides
    fun providesMedicationDao(database: MigrelyDatabase): MedicationDao = database.medicationDao()

    @Provides
    fun providesMedicationDataSource(medicationDao: MedicationDao): MedicationDataSource =
        MedicationDataSourceImpl(medicationDao, MedicationLocalMapper())

    @Provides
    fun providesClockDataSource(): ClockDataSource = ClockDataSourceImpl()
}
