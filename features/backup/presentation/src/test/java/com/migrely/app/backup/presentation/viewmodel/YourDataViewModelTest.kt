package com.migrely.app.backup.presentation.viewmodel

import com.migrely.app.architecture.domain.exception.UnknownDomainException
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
import com.migrely.app.backup.domain.model.ImportModeDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.ReadBackupResultDomainModel
import com.migrely.app.backup.domain.usecase.ExportBackupUseCase
import com.migrely.app.backup.domain.usecase.ExportCsvUseCase
import com.migrely.app.backup.domain.usecase.ImportBackupUseCase
import com.migrely.app.backup.domain.usecase.ReadBackupUseCase
import com.migrely.app.backup.presentation.mapper.BackupSummaryPresentationMapper
import com.migrely.app.backup.presentation.mapper.ImportModeDomainMapper
import com.migrely.app.backup.presentation.model.BackupSummaryPresentationModel
import com.migrely.app.backup.presentation.model.ImportModePresentationModel
import com.migrely.app.backup.presentation.model.YourDataPresentationNotification
import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.presentation.navigation.YourDataPresentationNavigationEvent
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

private const val URI = "content://downloads/1"
private val SUMMARY = BackupSummaryDomainModel(attacks = 10, medications = 2, dayMarks = 5)
private val REPLACE = ImportBackupRequestDomainModel(URI, ImportModeDomainModel.Replace)
private val ADD = ImportBackupRequestDomainModel(URI, ImportModeDomainModel.Add)
private val SUMMARY_SHOWN =
    BackupSummaryPresentationModel(attacks = 10, medications = 2, dayMarks = 5)

@RunWith(MockitoJUnitRunner::class)
class YourDataViewModelTest :
    BaseViewModelTest<YourDataViewState, YourDataPresentationNotification, YourDataViewModel>() {
    @Mock private lateinit var exportBackupUseCase: ExportBackupUseCase

    @Mock private lateinit var exportCsvUseCase: ExportCsvUseCase

    @Mock private lateinit var readBackupUseCase: ReadBackupUseCase

    @Mock private lateinit var importBackupUseCase: ImportBackupUseCase

    @Before
    fun setUp() {
        classUnderTest = YourDataViewModel(
            exportBackupUseCase,
            exportCsvUseCase,
            readBackupUseCase,
            importBackupUseCase,
            BackupSummaryPresentationMapper(),
            ImportModeDomainMapper(),
            useCaseExecutor
        )
    }

    private suspend fun notificationOf(action: () -> Unit): YourDataPresentationNotification =
        coroutineScope {
            val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
            action()
            notification.await()
        }

    @Test
    fun `When entering then idle`() = runTest {
        val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

        classUnderTest.onEnter()

        assertEquals(YourDataViewState.Idle, state.await())
    }

    @Test
    fun `When exporting then working, then idle with how it went`() = runTest {
        exportBackupUseCase.givenSuccessfulExecution(URI, BackupWriteResultDomainModel.Saved)
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        assertEquals(
            YourDataPresentationNotification.BackupSaved,
            notificationOf { classUnderTest.onExportBackupAction(URI) }
        )
        assertEquals(listOf(YourDataViewState.Working, YourDataViewState.Idle), states.await())
    }

    @Test
    fun `When exporting for Excel then saved, storage full or something went wrong`() = runTest {
        mapOf(
            BackupWriteResultDomainModel.Saved to YourDataPresentationNotification.CsvSaved,
            BackupWriteResultDomainModel.StorageFull to
                YourDataPresentationNotification.StorageFull,
            BackupWriteResultDomainModel.Failed to
                YourDataPresentationNotification.SomethingWentWrong
        ).forEach { (result, expected) ->
            exportCsvUseCase.givenSuccessfulExecution(URI, result)
            assertEquals(expected, notificationOf { classUnderTest.onExportCsvAction(URI) })
        }
    }

    @Test
    fun `When a file is picked then what it holds for confirmation, or why not`() = runTest {
        mapOf(
            ReadBackupResultDomainModel.Readable(SUMMARY, true) to
                YourDataPresentationNotification.ConfirmImport(URI, SUMMARY_SHOWN, true),
            ReadBackupResultDomainModel.NotABackup to
                YourDataPresentationNotification.NotABackup,
            ReadBackupResultDomainModel.Newer to YourDataPresentationNotification.Newer,
            ReadBackupResultDomainModel.Failed to
                YourDataPresentationNotification.SomethingWentWrong
        ).forEach { (result, expected) ->
            readBackupUseCase.givenSuccessfulExecution(URI, result)
            assertEquals(expected, notificationOf { classUnderTest.onImportFileAction(URI) })
        }
    }

    @Test
    fun `When the import is confirmed then it runs in that mode and reports how it went`() =
        runTest {
            importBackupUseCase.givenSuccessfulExecution(
                REPLACE,
                ImportResultDomainModel.Replaced(SUMMARY)
            )
            assertEquals(
                YourDataPresentationNotification.Replaced(SUMMARY_SHOWN),
                notificationOf {
                    classUnderTest.onImportAction(URI, ImportModePresentationModel.Replace)
                }
            )
            verify(useCaseExecutor).execute(
                useCase = eq(importBackupUseCase),
                value = eq(REPLACE),
                onResult = any(),
                onException = any()
            )

            mapOf(
                ImportResultDomainModel.Added(6, 4) to YourDataPresentationNotification.Added(6, 4),
                ImportResultDomainModel.NotABackup to YourDataPresentationNotification.NotABackup,
                ImportResultDomainModel.Newer to YourDataPresentationNotification.Newer,
                ImportResultDomainModel.StorageFull to YourDataPresentationNotification.StorageFull,
                ImportResultDomainModel.Failed to
                    YourDataPresentationNotification.SomethingWentWrong
            ).forEach { (result, expected) ->
                importBackupUseCase.givenSuccessfulExecution(ADD, result)
                assertEquals(
                    expected,
                    notificationOf {
                        classUnderTest.onImportAction(URI, ImportModePresentationModel.Add)
                    }
                )
            }
        }

    @Test
    fun `Given an unexpected failure then idle again and something went wrong`() = runTest {
        importBackupUseCase.givenFailedExecution(ADD, UnknownDomainException())
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        assertEquals(
            YourDataPresentationNotification.SomethingWentWrong,
            notificationOf { classUnderTest.onImportAction(URI, ImportModePresentationModel.Add) }
        )
        assertEquals(listOf(YourDataViewState.Working, YourDataViewState.Idle), states.await())
    }

    @Test
    fun `When going back then back`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onBackAction()

        assertEquals(YourDataPresentationNavigationEvent.OnBack, event.await())
    }
}
