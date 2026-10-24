package com.migrely.app.backup.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.domain.usecase.UseCase
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
import com.migrely.app.backup.domain.model.ImportResultDomainModel
import com.migrely.app.backup.domain.model.ReadBackupResultDomainModel
import com.migrely.app.backup.domain.usecase.ExportBackupUseCase
import com.migrely.app.backup.domain.usecase.ExportCsvUseCase
import com.migrely.app.backup.domain.usecase.ImportBackupUseCase
import com.migrely.app.backup.domain.usecase.ReadBackupUseCase
import com.migrely.app.backup.presentation.mapper.BackupSummaryPresentationMapper
import com.migrely.app.backup.presentation.mapper.ImportModeDomainMapper
import com.migrely.app.backup.presentation.model.ImportModePresentationModel
import com.migrely.app.backup.presentation.model.YourDataPresentationNotification
import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.presentation.navigation.YourDataPresentationNavigationEvent

/**
 * "Your data" (BRD BAK-F1–F3, D71–D73). Each event carries the file the user picked through the system's picker, so the
 * view model keeps nothing: it shows the work in progress and reports how it went.
 */
class YourDataViewModel(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val readBackupUseCase: ReadBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val backupSummaryPresentationMapper: BackupSummaryPresentationMapper,
    private val importModeDomainMapper: ImportModeDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<YourDataViewState, YourDataPresentationNotification>(useCaseExecutor) {
    fun onEnter() = updateViewState(YourDataViewState.Idle)

    fun onBackAction() = emitNavigationEvent(YourDataPresentationNavigationEvent.OnBack)

    /** The user chose where to save the backup. */
    fun onExportBackupAction(uri: String) = work(exportBackupUseCase, uri) {
        written(it, YourDataPresentationNotification.BackupSaved)
    }

    /** The user chose where to save the spreadsheet. */
    fun onExportCsvAction(uri: String) = work(exportCsvUseCase, uri) {
        written(it, YourDataPresentationNotification.CsvSaved)
    }

    /** The user picked a file to import; nothing changes until they confirm. */
    fun onImportFileAction(uri: String) = work(readBackupUseCase, uri) { result ->
        when (result) {
            is ReadBackupResultDomainModel.Readable ->
                YourDataPresentationNotification.ConfirmImport(
                    uri,
                    backupSummaryPresentationMapper.toPresentation(result.summary),
                    result.phoneHasRecords
                )

            ReadBackupResultDomainModel.NotABackup -> YourDataPresentationNotification.NotABackup
            ReadBackupResultDomainModel.Newer -> YourDataPresentationNotification.Newer
            ReadBackupResultDomainModel.Failed ->
                YourDataPresentationNotification.SomethingWentWrong
        }
    }

    /** The user confirmed the import of [uri]. */
    fun onImportAction(uri: String, mode: ImportModePresentationModel) = work(
        importBackupUseCase,
        ImportBackupRequestDomainModel(uri, importModeDomainMapper.toDomain(mode))
    ) { result ->
        when (result) {
            is ImportResultDomainModel.Replaced -> YourDataPresentationNotification.Replaced(
                backupSummaryPresentationMapper.toPresentation(result.summary)
            )

            is ImportResultDomainModel.Added ->
                YourDataPresentationNotification.Added(result.addedAttacks, result.skippedAttacks)

            ImportResultDomainModel.NotABackup -> YourDataPresentationNotification.NotABackup
            ImportResultDomainModel.Newer -> YourDataPresentationNotification.Newer
            ImportResultDomainModel.StorageFull -> YourDataPresentationNotification.StorageFull
            ImportResultDomainModel.Failed -> YourDataPresentationNotification.SomethingWentWrong
        }
    }

    private fun written(
        result: BackupWriteResultDomainModel,
        saved: YourDataPresentationNotification
    ) = when (result) {
        BackupWriteResultDomainModel.Saved -> saved
        BackupWriteResultDomainModel.StorageFull -> YourDataPresentationNotification.StorageFull
        BackupWriteResultDomainModel.Failed -> YourDataPresentationNotification.SomethingWentWrong
    }

    /** Shows the work going on, then its outcome; an unexpected failure is reported as one. */
    private fun <INPUT, OUTPUT> work(
        useCase: UseCase<INPUT, OUTPUT>,
        value: INPUT,
        outcome: (OUTPUT) -> YourDataPresentationNotification
    ) {
        updateViewState(YourDataViewState.Working)
        useCase(
            value = value,
            onResult = { result ->
                updateViewState(YourDataViewState.Idle)
                notify(outcome(result))
            },
            onException = {
                updateViewState(YourDataViewState.Idle)
                notify(YourDataPresentationNotification.SomethingWentWrong)
            }
        )
    }
}
