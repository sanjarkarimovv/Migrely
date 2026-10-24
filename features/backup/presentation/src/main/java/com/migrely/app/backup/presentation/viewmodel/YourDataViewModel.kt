package com.migrely.app.backup.presentation.viewmodel

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.domain.usecase.UseCase
import com.migrely.app.architecture.presentation.viewmodel.BaseViewModel
import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel
import com.migrely.app.backup.domain.model.ImportBackupRequestDomainModel
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
import com.migrely.app.backup.presentation.model.ImportModePresentationModel
import com.migrely.app.backup.presentation.model.YourDataPresentationNotification
import com.migrely.app.backup.presentation.model.YourDataViewState
import com.migrely.app.backup.presentation.navigation.YourDataPresentationNavigationEvent

/**
 * "Your data" (BRD BAK-F1–F3, D71–D73, D98). Each event carries the file the user picked through the system's picker.
 *
 * Writing or reading a file can outlast a rotation, so this view model lives as long as the app and keeps three
 * things, only for delivery: how many files are being worked on, an outcome the screen has not taken yet, and the
 * newest automatic copy last read, so the screen shows its row while work goes on. On returning, the screen sees the
 * work still going and gets the outcome it missed (ARCHITECTURE section 13).
 */
@Suppress("LongParameterList", "TooManyFunctions")
class YourDataViewModel(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val exportCsvUseCase: ExportCsvUseCase,
    private val readBackupUseCase: ReadBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val getAutomaticCopyUseCase: GetAutomaticCopyUseCase,
    private val restoreAutomaticCopyUseCase: RestoreAutomaticCopyUseCase,
    private val backupSummaryPresentationMapper: BackupSummaryPresentationMapper,
    private val automaticCopyPresentationMapper: AutomaticCopyPresentationMapper,
    private val importModeDomainMapper: ImportModeDomainMapper,
    useCaseExecutor: UseCaseExecutor
) : BaseViewModel<YourDataViewState, YourDataPresentationNotification>(useCaseExecutor) {
    private var working = 0
    private var undelivered: YourDataPresentationNotification? = null
    private var automaticCopy: AutomaticCopyPresentationModel? = null

    fun onEnter() {
        updateViewState(state())
        readAutomaticCopy { updateViewState(state()) }
    }

    /** The screen listens again, after a rotation for example: it gets the state and an outcome it missed. */
    fun onResume() = show()

    /** The screen showed the last outcome, so it is not shown again. */
    fun onOutcomeShownAction() {
        undelivered = null
    }

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

            is ImportResultDomainModel.Added -> YourDataPresentationNotification.Added(
                result.addedAttacks,
                result.skippedAttacks,
                result.addedOthers
            )

            ImportResultDomainModel.NotABackup -> YourDataPresentationNotification.NotABackup
            ImportResultDomainModel.Newer -> YourDataPresentationNotification.Newer
            ImportResultDomainModel.StorageFull -> YourDataPresentationNotification.StorageFull
            ImportResultDomainModel.Failed -> YourDataPresentationNotification.SomethingWentWrong
        }
    }

    /** The user confirmed restoring the newest automatic copy (BRD D98). */
    fun onRestoreCopyAction() = work(restoreAutomaticCopyUseCase, Unit) { result ->
        when (result) {
            is RestoreResultDomainModel.Restored -> YourDataPresentationNotification.Restored(
                backupSummaryPresentationMapper.toPresentation(result.summary)
            )

            RestoreResultDomainModel.StorageFull -> YourDataPresentationNotification.StorageFull
            RestoreResultDomainModel.Failed -> YourDataPresentationNotification.SomethingWentWrong
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

    private fun state() = if (working > 0) {
        YourDataViewState.Working(automaticCopy)
    } else {
        YourDataViewState.Idle(automaticCopy)
    }

    private fun show() {
        updateViewState(state())
        undelivered?.let(::notify)
    }

    /**
     * Shows the work going on, then its outcome. Expected failures arrive as results; a `DomainException` routed to
     * `onException` is reported as something going wrong.
     */
    private fun <INPUT, OUTPUT> work(
        useCase: UseCase<INPUT, OUTPUT>,
        value: INPUT,
        outcome: (OUTPUT) -> YourDataPresentationNotification
    ) {
        working++
        updateViewState(state())
        useCase(
            value = value,
            onResult = { result -> finish(outcome(result)) },
            onException = { finish(YourDataPresentationNotification.SomethingWentWrong) }
        )
    }

    private fun finish(outcome: YourDataPresentationNotification) {
        working = (working - 1).coerceAtLeast(0)
        undelivered = outcome
        // A replace or a restore saves a new copy, so the copy is read again before the outcome shows.
        readAutomaticCopy(::show)
    }

    private fun readAutomaticCopy(then: () -> Unit) = getAutomaticCopyUseCase(
        onResult = { copy ->
            automaticCopy = automaticCopyPresentationMapper.toPresentation(copy)
            then()
        },
        onException = { then() }
    )
}
