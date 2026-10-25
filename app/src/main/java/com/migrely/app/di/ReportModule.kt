package com.migrely.app.di

import android.content.Context
import android.text.format.DateFormat
import androidx.core.content.FileProvider
import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSource
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSourceImpl
import com.migrely.app.locale.FormattingLocales
import com.migrely.app.navigation.mapper.ReportNavigationEventDestinationMapper
import com.migrely.app.report.data.mapper.ReportDateTimeFormatter
import com.migrely.app.report.data.mapper.ReportDocumentMapper
import com.migrely.app.report.data.mapper.ReportRecordsDomainMapper
import com.migrely.app.report.data.repository.ReportRepository
import com.migrely.app.report.domain.usecase.CleanUpReportsUseCase
import com.migrely.app.report.domain.usecase.CreateReportUseCase
import com.migrely.app.report.domain.usecase.GetReportOptionsUseCase
import com.migrely.app.report.presentation.mapper.ReportOptionsPresentationMapper
import com.migrely.app.report.presentation.mapper.ReportPeriodDomainMapper
import com.migrely.app.report.presentation.viewmodel.ReportViewModel
import com.migrely.app.report.ui.di.ReportDependencies
import com.migrely.app.report.ui.mapper.ReportEventPresenter
import com.migrely.app.report.ui.mapper.ReportMessageBehaviourUiMapper
import com.migrely.app.report.ui.mapper.ReportNotificationUiMapper
import com.migrely.app.report.ui.mapper.ReportPeriodPresentationMapper
import com.migrely.app.report.ui.mapper.ReportViewStateUiMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** The doctor report (change add-doctor-report, BRD D81–D85). One repository serves every repository interface. */
@Module
@InstallIn(SingletonComponent::class)
internal object ReportModule {
    @Provides
    fun providesReportDocumentDataSource(
        @ApplicationContext context: Context
    ): ReportDocumentDataSource = ReportDocumentDataSourceImpl(context.cacheDir) { file ->
        FileProvider.getUriForFile(context, "${context.packageName}.reports", file).toString()
    }

    /** The report follows the phone as History does, reading its settings when a report is made (EC-TIM-05). */
    @Provides
    fun providesReportRepository(
        @ApplicationContext context: Context,
        formattingLocales: FormattingLocales,
        backupDataSource: BackupDataSource,
        reportDocumentDataSource: ReportDocumentDataSource,
        clockDataSource: ClockDataSource
    ) = ReportRepository(
        backupDataSource,
        reportDocumentDataSource,
        clockDataSource,
        ReportRecordsDomainMapper(),
        ReportDocumentMapper(
            ReportDateTimeFormatter(
                locale = { formattingLocales.of(context.resources.configuration) },
                is24HourFormat = { DateFormat.is24HourFormat(context) },
                bestDateTimePattern = { locale, skeleton ->
                    DateFormat.getBestDateTimePattern(locale, skeleton)
                }
            )
        )
    )

    @Provides
    fun providesCleanUpReportsUseCase(
        repository: ReportRepository,
        coroutines: CoroutineContextProvider
    ) = CleanUpReportsUseCase(repository, coroutines)

    /** One for the app, so a report being made survives a rotation (ARCHITECTURE section 13). */
    @Provides
    @Singleton
    fun providesReportViewModel(
        repository: ReportRepository,
        coroutines: CoroutineContextProvider,
        useCaseExecutor: UseCaseExecutor
    ) = ReportViewModel(
        GetReportOptionsUseCase(repository, repository, coroutines),
        CreateReportUseCase(repository, repository, repository, repository, coroutines),
        ReportPeriodDomainMapper(),
        ReportOptionsPresentationMapper(),
        useCaseExecutor
    )

    @Provides
    @Singleton
    fun providesReportDependencies(reportViewModel: ReportViewModel): ReportDependencies {
        val presenter = ReportEventPresenter()
        return ReportDependencies(
            reportViewModel = reportViewModel,
            reportViewStateUiMapper = ReportViewStateUiMapper(),
            reportPeriodPresentationMapper = ReportPeriodPresentationMapper(),
            reportEventPresenter = presenter,
            reportMessageBehaviourUiMapper = ReportMessageBehaviourUiMapper(),
            navigationMapper = ReportNavigationEventDestinationMapper(),
            notificationMapper = ReportNotificationUiMapper(presenter)
        )
    }
}
