package com.migrely.app.di

import android.content.Context
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.backup.data.mapper.AttacksCsvMapper
import com.migrely.app.backup.data.mapper.BackupSummaryDomainMapper
import com.migrely.app.backup.data.repository.BackupRepository
import com.migrely.app.backup.domain.usecase.ExportBackupUseCase
import com.migrely.app.backup.domain.usecase.ExportCsvUseCase
import com.migrely.app.backup.domain.usecase.GetAutomaticCopyUseCase
import com.migrely.app.backup.domain.usecase.ImportBackupUseCase
import com.migrely.app.backup.domain.usecase.ReadBackupUseCase
import com.migrely.app.backup.domain.usecase.RestoreAutomaticCopyUseCase
import com.migrely.app.backup.presentation.mapper.AutomaticCopyPresentationMapper
import com.migrely.app.backup.presentation.mapper.BackupSummaryPresentationMapper
import com.migrely.app.backup.presentation.mapper.ImportModeDomainMapper
import com.migrely.app.backup.presentation.viewmodel.YourDataViewModel
import com.migrely.app.backup.ui.di.YourDataDependencies
import com.migrely.app.backup.ui.mapper.AutomaticCopyUiMapper
import com.migrely.app.backup.ui.mapper.BackupSummaryUiMapper
import com.migrely.app.backup.ui.mapper.YourDataEventPresenter
import com.migrely.app.backup.ui.mapper.YourDataMessageBehaviourUiMapper
import com.migrely.app.backup.ui.mapper.YourDataNotificationUiMapper
import com.migrely.app.backup.ui.mapper.YourDataViewStateUiMapper
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backup.datasource.BackupDataSourceImpl
import com.migrely.app.datasource.backup.mapper.BackupLocalMapper
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSource
import com.migrely.app.datasource.backupfile.datasource.BackupFileDataSourceImpl
import com.migrely.app.datasource.backupfile.mapper.BackupFileMapper
import com.migrely.app.datasource.backupfile.validator.BackupRecordsValidator
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.daymark.mapper.DayMarkLocalMapper
import com.migrely.app.datasource.document.datasource.DocumentDataSource
import com.migrely.app.datasource.document.datasource.DocumentDataSourceImpl
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import com.migrely.app.navigation.mapper.YourDataNavigationEventDestinationMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Backups, exports and imports (change add-data-export-import, BRD D70–D74). One repository serves every
 * single-function repository interface, so the use cases take it directly.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object BackupModule {
    @Provides
    fun providesBackupDataSource(database: MigrelyDatabase): BackupDataSource =
        BackupDataSourceImpl(
            database.backupDao(),
            BackupLocalMapper(
                AttackTagLocalMapper(),
                CustomTagLocalMapper(),
                MedicationLocalMapper(),
                DayMarkLocalMapper()
            ),
            database::deleteEverything
        )

    @Provides
    fun providesBackupFileDataSource(@ApplicationContext context: Context): BackupFileDataSource =
        BackupFileDataSourceImpl(
            context.filesDir,
            BackupFileMapper(
                AttackTagLocalMapper(),
                CustomTagLocalMapper(),
                MedicationLocalMapper()
            ),
            BackupRecordsValidator()
        )

    @Provides
    fun providesDocumentDataSource(@ApplicationContext context: Context): DocumentDataSource =
        DocumentDataSourceImpl(context.contentResolver)

    @Provides
    fun providesBackupRepository(
        backupDataSource: BackupDataSource,
        backupFileDataSource: BackupFileDataSource,
        documentDataSource: DocumentDataSource,
        clockDataSource: ClockDataSource
    ) = BackupRepository(
        backupDataSource,
        backupFileDataSource,
        documentDataSource,
        clockDataSource,
        AttacksCsvMapper(),
        BackupSummaryDomainMapper()
    )

    /** One for the app, so a file being written or read survives a rotation (ARCHITECTURE section 13). */
    @Provides
    @Singleton
    fun providesYourDataViewModel(
        repository: BackupRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = YourDataViewModel(
        ExportBackupUseCase(repository, coroutines),
        ExportCsvUseCase(repository, coroutines),
        ReadBackupUseCase(repository, repository, coroutines),
        ImportBackupUseCase(repository, repository, repository, coroutines),
        GetAutomaticCopyUseCase(repository, coroutines),
        RestoreAutomaticCopyUseCase(repository, repository, coroutines),
        BackupSummaryPresentationMapper(),
        AutomaticCopyPresentationMapper(BackupSummaryPresentationMapper()),
        ImportModeDomainMapper(),
        useCaseExecutor
    )

    @Provides
    @Singleton
    fun providesYourDataDependencies(yourDataViewModel: YourDataViewModel): YourDataDependencies {
        val presenter = YourDataEventPresenter()
        return YourDataDependencies(
            yourDataViewModel = yourDataViewModel,
            yourDataViewStateUiMapper = YourDataViewStateUiMapper(
                AutomaticCopyUiMapper(BackupSummaryUiMapper())
            ),
            yourDataEventPresenter = presenter,
            yourDataMessageBehaviourUiMapper = YourDataMessageBehaviourUiMapper(),
            navigationMapper = YourDataNavigationEventDestinationMapper(),
            notificationMapper = YourDataNotificationUiMapper(presenter, BackupSummaryUiMapper())
        )
    }
}
