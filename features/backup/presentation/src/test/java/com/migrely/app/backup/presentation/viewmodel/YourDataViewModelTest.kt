package com.migrely.app.backup.presentation.viewmodel

import com.migrely.app.architecture.domain.exception.UnknownDomainException
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModelTest
import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel
import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
import com.migrely.app.backup.domain.model.ImportModeDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.ReadBackupResultDomainModel
import com.migrely.app.backup.domain.model.RestoreResultDomainModel
import com.migrely.app.backup.domain.usecase.ExportBackupUseCase
import com.migrely.app.backup.domain.usecase.ExportCsvUseCase
import com.migrely.app.backup.domain.usecase.GetAutomaticCopyUseCase
import com.migrely.app.backup.domain.usecase.ImportBackupUseCase
import com.migrely.app.backup.domain.usecase.ReadBackupUseCase
import com.migrely.app.backup.domain.usecase.RestoreAutomaticCopyUseCase
import com.migrely.app.backup.presentation.mapper.AutomaticCopyPresentationMapper
import com.migrely.app.backup.presentation.mapper.BackupSummaryPresentationMapper
import com.migrely.app.backup.presentation.mapper.ImportModeDomainMapper
import com.migrely.app.backup.presentation.model.AutomaticCopyPresentationModel
import com.migrely.app.backup.presentation.model.BackupSummaryPresentationModel
import com.migrely.app.backup.presentation.model.ImportModePresentationModel
import com.migrely.app.backup.presentation.model.YourDataPresentationNotification
import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.presentation.navigation.YourDataPresentationNavigationEvent
import java.time.Instant
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
private val MADE_AT = Instant.parse("2026-10-24T19:30:00Z")
private val COPY = AutomaticCopyDomainModel.Available(MADE_AT, SUMMARY)
private val COPY_SHOWN = AutomaticCopyPresentationModel(MADE_AT, SUMMARY_SHOWN)
private val IDLE = YourDataViewState.Idle(automaticCopy = null)
private val WORKING = YourDataViewState.Working(automaticCopy = null)

@RunWith(MockitoJUnitRunner::class)
class YourDataViewModelTest :
    BaseViewModelTest<YourDataViewState, YourDataPresentationNotification, YourDataViewModel>() {
    @Mock private lateinit var exportBackupUseCase: ExportBackupUseCase

    @Mock private lateinit var exportCsvUseCase: ExportCsvUseCase

    @Mock private lateinit var readBackupUseCase: ReadBackupUseCase

    @Mock private lateinit var importBackupUseCase: ImportBackupUseCase

    @Mock private lateinit var getAutomaticCopyUseCase: GetAutomaticCopyUseCase

    @Mock private lateinit var restoreAutomaticCopyUseCase: RestoreAutomaticCopyUseCase

    @Before
    fun setUp() {
        classUnderTest = YourDataViewModel(
            exportBackupUseCase,
            exportCsvUseCase,
            readBackupUseCase,
            importBackupUseCase,
            getAutomaticCopyUseCase,
            restoreAutomaticCopyUseCase,
            BackupSummaryPresentationMapper(),
            AutomaticCopyPresentationMapper(BackupSummaryPresentationMapper()),
            ImportModeDomainMapper(),
            useCaseExecutor
        )
    }

    /** Work that finishes reads the copy again, so every test that finishes work says what it finds. */
    private fun givenNoCopy() =
        getAutomaticCopyUseCase.givenSuccessfulExecution(AutomaticCopyDomainModel.None)

    private suspend fun notificationOf(action: () -> Unit): YourDataPresentationNotification =
        coroutineScope {
            val notification = async(start = UNDISPATCHED) { classUnderTest.notification.first() }
            action()
            notification.await()
        }

    @Test
    fun `When entering then idle, then the copy there is to restore`() = runTest {
        getAutomaticCopyUseCase.givenSuccessfulExecution(COPY)
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        classUnderTest.onEnter()

        assertEquals(listOf(IDLE, YourDataViewState.Idle(COPY_SHOWN)), states.await())
    }

    @Test
    fun `When exporting then working, then idle with how it went`() = runTest {
        givenNoCopy()
        exportBackupUseCase.givenSuccessfulExecution(URI, BackupWriteResultDomainModel.Saved)
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        assertEquals(
            YourDataPresentationNotification.BackupSaved,
            notificationOf { classUnderTest.onExportBackupAction(URI) }
        )
        assertEquals(listOf(WORKING, IDLE), states.await())
    }

    @Test
    fun `When exporting for Excel then saved, storage full or something went wrong`() = runTest {
        givenNoCopy()
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
        givenNoCopy()
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
            givenNoCopy()
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
                ImportResultDomainModel.Added(6, 4, 1) to
                    YourDataPresentationNotification.Added(6, 4, 1),
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
        givenNoCopy()
        importBackupUseCase.givenFailedExecution(ADD, UnknownDomainException())
        val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

        assertEquals(
            YourDataPresentationNotification.SomethingWentWrong,
            notificationOf { classUnderTest.onImportAction(URI, ImportModePresentationModel.Add) }
        )
        assertEquals(listOf(WORKING, IDLE), states.await())
    }

    @Test
    fun `Given an outcome not taken when the screen listens again then it is sent again`() =
        runTest {
            givenNoCopy()
            exportCsvUseCase.givenSuccessfulExecution(URI, BackupWriteResultDomainModel.Saved)
            classUnderTest.onExportCsvAction(URI)

            assertEquals(
                YourDataPresentationNotification.CsvSaved,
                notificationOf {
                    classUnderTest.onResume()
                }
            )

            classUnderTest.onOutcomeShownAction()
            val states = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }
            classUnderTest.onResume()
            assertEquals(IDLE, states.await())
            verify(useCaseExecutor).execute(
                useCase = eq(exportCsvUseCase),
                value = eq(URI),
                onResult = any(),
                onException = any()
            )
        }

    @Test
    fun `Given a file is still being worked on when the screen comes back then working`() =
        runTest {
            classUnderTest.onExportBackupAction(URI)
            val state = async(start = UNDISPATCHED) { classUnderTest.viewState.first() }

            classUnderTest.onEnter()

            assertEquals(WORKING, state.await())
        }

    @Test
    fun `When the copy is restored then working, then the copy now there and how it went`() =
        runTest {
            getAutomaticCopyUseCase.givenSuccessfulExecution(COPY)
            restoreAutomaticCopyUseCase.givenSuccessfulExecution(
                MADE_AT,
                RestoreResultDomainModel.Restored(SUMMARY)
            )
            val states = async(start = UNDISPATCHED) { classUnderTest.viewState.take(2).toList() }

            assertEquals(
                YourDataPresentationNotification.Restored(SUMMARY_SHOWN),
                notificationOf { classUnderTest.onRestoreCopyAction(MADE_AT) }
            )
            assertEquals(listOf(WORKING, YourDataViewState.Idle(COPY_SHOWN)), states.await())
        }

    @Test
    fun `Given restoring fails then storage full or something went wrong, as an import`() =
        runTest {
            givenNoCopy()
            mapOf(
                RestoreResultDomainModel.StorageFull to
                    YourDataPresentationNotification.StorageFull,
                RestoreResultDomainModel.Failed to
                    YourDataPresentationNotification.SomethingWentWrong
            ).forEach { (result, expected) ->
                restoreAutomaticCopyUseCase.givenSuccessfulExecution(MADE_AT, result)
                assertEquals(
                    expected,
                    notificationOf {
                        classUnderTest.onRestoreCopyAction(MADE_AT)
                    }
                )
            }
        }

    @Test
    fun `When going back then back`() = runTest {
        val event = async(start = UNDISPATCHED) { classUnderTest.navigationEvent.first() }

        classUnderTest.onBackAction()

        assertEquals(YourDataPresentationNavigationEvent.OnBack, event.await())
    }
}
