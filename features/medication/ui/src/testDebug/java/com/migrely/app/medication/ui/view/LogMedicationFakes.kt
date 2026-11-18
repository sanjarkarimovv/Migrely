package com.migrely.app.medication.ui.view

import com.migrely.app.architecture.domain.UseCaseExecutor
import com.migrely.app.architecture.ui.navigation.mapper.NavigationEventDestinationMapper
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.ActiveCountResultDomainModel
import com.migrely.app.medication.domain.model.AddIntakeResultDomainModel
import com.migrely.app.medication.domain.model.AttackLookupResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.MedicationLookupResultDomainModel
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import com.migrely.app.medication.domain.model.MedicationsResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.AddIntakeRepository
import com.migrely.app.medication.domain.repository.CancelEffectQuestionRepository
import com.migrely.app.medication.domain.repository.CountActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.CreateMedicationRepository
import com.migrely.app.medication.domain.repository.DeleteIntakeRepository
import com.migrely.app.medication.domain.repository.FindAttackAtRepository
import com.migrely.app.medication.domain.repository.FindMedicationByNameRepository
import com.migrely.app.medication.domain.repository.GetActiveMedicationsRepository
import com.migrely.app.medication.domain.repository.GetAttackRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import com.migrely.app.medication.domain.repository.ScheduleEffectQuestionRepository
import com.migrely.app.medication.domain.repository.SetMedicationArchivedRepository
import com.migrely.app.medication.domain.repository.UpdateMedicationRepository
import com.migrely.app.medication.domain.usecase.GetMedicationLogUseCase
import com.migrely.app.medication.domain.usecase.LogIntakeUseCase
import com.migrely.app.medication.domain.usecase.SaveMedicationUseCase
import com.migrely.app.medication.domain.usecase.UndoIntakeUseCase
import com.migrely.app.medication.presentation.mapper.IntakePresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationDomainMapper
import com.migrely.app.medication.presentation.mapper.MedicationLogPresentationMapper
import com.migrely.app.medication.presentation.mapper.MedicationPresentationMapper
import com.migrely.app.medication.presentation.mapper.SaveProblemPresentationMapper
import com.migrely.app.medication.presentation.navigation.LogMedicationPresentationNavigationEvent
import com.migrely.app.medication.presentation.viewmodel.LogMedicationViewModel
import com.migrely.app.medication.ui.di.LogMedicationDependencies
import com.migrely.app.medication.ui.mapper.IntakeUiMapper
import com.migrely.app.medication.ui.mapper.LogMedicationNotificationUiMapper
import com.migrely.app.medication.ui.mapper.LogMedicationViewStateUiMapper
import com.migrely.app.medication.ui.mapper.MedicationCategoryUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationKindUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.mapper.MedicationUiMapper
import com.migrely.app.medication.ui.mapper.MedicationUiToPresentationMapper
import com.migrely.app.medication.ui.mapper.SaveProblemUiMapper
import com.migrely.app.medication.ui.notification.MedicationEventPresenter
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

internal const val IBUPROFEN_NAME = "Ibuprofen"
private const val IBUPROFEN_ID = "med-ibuprofen"
private const val HOLD_MILLIS = 5_000L
private val LOG_ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

/** 2026-11-18 09:20 in Tashkent: the page's "now". */
private val NOW: Instant = Instant.parse("2026-11-18T04:20:00Z")

internal val IBUPROFEN_MEDICATION = MedicationDomainModel(
    id = IBUPROFEN_ID,
    name = IBUPROFEN_NAME,
    kind = MedicationKindDomainModel.Acute,
    category = MedicationCategoryDomainModel.SimplePainkiller,
    defaultDose = "400 mg",
    archived = false
)

/**
 * The diary behind Log medication, held in memory: its medications and the intakes stored. Each write can be made to
 * fail ([addFailure], [deleteFailure], [createFailure]) or to wait until the test lets it go ([holdAdd], [holdDelete]),
 * as a full disk or a slow write would.
 */
@Suppress("TooManyFunctions")
internal class FakeDiary(vararg medications: MedicationDomainModel) :
    GetCurrentTimeRepository,
    GetAttackRepository,
    FindAttackAtRepository,
    GetActiveMedicationsRepository,
    AddIntakeRepository,
    DeleteIntakeRepository,
    ScheduleEffectQuestionRepository,
    CancelEffectQuestionRepository,
    FindMedicationByNameRepository,
    CountActiveMedicationsRepository,
    CreateMedicationRepository,
    UpdateMedicationRepository,
    SetMedicationArchivedRepository {
    val medications = CopyOnWriteArrayList(medications.toList())
    val intakes = CopyOnWriteArrayList<String>()
    val addCalls = CopyOnWriteArrayList<String>()
    val deleteCalls = CopyOnWriteArrayList<String>()

    @Volatile var addFailure: AddIntakeResultDomainModel? = null

    @Volatile var deleteFailure: WriteResultDomainModel? = null

    @Volatile var createFailure: MedicationWriteResultDomainModel? = null

    @Volatile var holdAdd: CountDownLatch? = null

    @Volatile var holdDelete: CountDownLatch? = null

    private var nextIntake = 0

    override fun currentTime() = CurrentTimeDomainModel(NOW, LOG_ZONE)

    override fun attack(attackId: String) = AttackLookupResultDomainModel.None

    override fun attackDuring(from: Instant, to: Instant) = AttackLookupResultDomainModel.None

    override fun activeMedications() = MedicationsResultDomainModel.Loaded(medications.toList())

    override fun addIntake(
        medicationId: String,
        attackId: String?,
        takenAt: Instant,
        zone: ZoneId,
        dose: String?
    ): AddIntakeResultDomainModel {
        addCalls += medicationId
        holdAdd?.await(HOLD_MILLIS, TimeUnit.MILLISECONDS)
        addFailure?.let { return it }
        val id = synchronized(this) { "intake-${++nextIntake}" }
        intakes += id
        return AddIntakeResultDomainModel.Added(id)
    }

    override fun deleteIntake(intakeId: String): WriteResultDomainModel {
        deleteCalls += intakeId
        holdDelete?.await(HOLD_MILLIS, TimeUnit.MILLISECONDS)
        deleteFailure?.let { return it }
        intakes -= intakeId
        return WriteResultDomainModel.Done
    }

    override fun scheduleEffectQuestion(intakeId: String, at: Instant) = Unit

    override fun cancelEffectQuestion(intakeId: String) = Unit

    override fun medicationByNameKey(nameKey: String) =
        medications.firstOrNull { it.name.lowercase() == nameKey }
            ?.let(MedicationLookupResultDomainModel::Found)
            ?: MedicationLookupResultDomainModel.NotFound

    override fun activeMedicationCount() = ActiveCountResultDomainModel.Counted(medications.size)

    override fun createMedication(
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ): MedicationWriteResultDomainModel {
        createFailure?.let { return it }
        val created =
            MedicationDomainModel("med-$nameKey", name, kind, category, defaultDose, false)
        medications += created
        return MedicationWriteResultDomainModel.Done(created)
    }

    override fun updateMedication(
        medicationId: String,
        name: String,
        nameKey: String,
        kind: MedicationKindDomainModel,
        category: MedicationCategoryDomainModel?,
        defaultDose: String?
    ) = MedicationWriteResultDomainModel.Failed

    override fun setMedicationArchived(medicationId: String, archived: Boolean) =
        WriteResultDomainModel.Failed
}

private object LogTestContexts : CoroutineContextProvider {
    override val main: CoroutineContext = Dispatchers.Main
    override val io: CoroutineContext = Dispatchers.IO
}

internal val LOG_FORMATTER = MedicationTimeUiFormatter(
    locale = { Locale.UK },
    is24HourFormat = { true },
    deviceZone = { LOG_ZONE },
    bestDateTimePattern = { _, skeleton -> if (skeleton == "EEEdMMM") "EEE d MMM" else "HH:mm" }
)

/**
 * Log medication as the app builds it, over [diary]: the real use cases, ViewModel and mappers. [onDone] is where
 * "Done" and Back go; by default they stay.
 */
internal fun logMedicationDependencies(
    diary: FakeDiary,
    onDone: UiDestination = UiDestination { }
): LogMedicationDependencies {
    val presenter = MedicationEventPresenter()
    return LogMedicationDependencies(
        logMedicationViewModel = LogMedicationViewModel(
            GetMedicationLogUseCase(diary, diary, diary, LogTestContexts),
            LogIntakeUseCase(diary, diary, diary, diary, diary, LogTestContexts),
            UndoIntakeUseCase(diary, diary, LogTestContexts),
            SaveMedicationUseCase(diary, diary, diary, diary, diary, LogTestContexts),
            MedicationLogPresentationMapper(MedicationPresentationMapper()),
            MedicationDomainMapper(),
            IntakePresentationMapper(),
            SaveProblemPresentationMapper(),
            UseCaseExecutor()
        ),
        logMedicationViewStateUiMapper = LogMedicationViewStateUiMapper(MedicationUiMapper()),
        medicationUiToPresentationMapper = MedicationUiToPresentationMapper(
            MedicationKindUiToPresentationMapper(),
            MedicationCategoryUiToPresentationMapper()
        ),
        medicationCategoryUiToPresentationMapper = MedicationCategoryUiToPresentationMapper(),
        medicationTimeUiFormatter = LOG_FORMATTER,
        medicationEventPresenter = presenter,
        medicationMessageBehaviourUiMapper = MedicationMessageBehaviourUiMapper(),
        navigationMapper = LogDestinations(onDone),
        notificationMapper = LogMedicationNotificationUiMapper(
            presenter,
            SaveProblemUiMapper(),
            IntakeUiMapper()
        )
    )
}

private class LogDestinations(private val onDone: UiDestination) :
    NavigationEventDestinationMapper<LogMedicationPresentationNavigationEvent>(
        LogMedicationPresentationNavigationEvent::class
    ) {
    override fun mapTypedEvent(navigationEvent: LogMedicationPresentationNavigationEvent) =
        when (navigationEvent) {
            LogMedicationPresentationNavigationEvent.OnDone -> onDone
            LogMedicationPresentationNavigationEvent.OnManageMedications -> UiDestination { }
        }
}
