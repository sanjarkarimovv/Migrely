package com.migrely.app.medication.data.repository

import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.datasource.MedicationDataSource
import com.migrely.app.medication.data.mapper.AcuteIntakeDomainMapper
import com.migrely.app.medication.data.mapper.IntakeEffectDataMapper
import com.migrely.app.medication.data.mapper.OveruseDataMapper
import com.migrely.app.medication.data.mapper.OveruseNoticeDomainMapper
import com.migrely.app.medication.data.mapper.PendingEffectQuestionDomainMapper
import com.migrely.app.medication.domain.model.AcuteIntakesResultDomainModel
import com.migrely.app.medication.domain.model.IntakeEffectDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseLevelDomainModel
import com.migrely.app.medication.domain.model.OveruseNoticesResultDomainModel
import com.migrely.app.medication.domain.model.PendingEffectQuestionResultDomainModel
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import com.migrely.app.medication.domain.repository.AcknowledgeOveruseRepository
import com.migrely.app.medication.domain.repository.CloseEffectQuestionRepository
import com.migrely.app.medication.domain.repository.GetAcuteIntakesRepository
import com.migrely.app.medication.domain.repository.GetOveruseNoticesRepository
import com.migrely.app.medication.domain.repository.GetPendingEffectQuestionRepository
import com.migrely.app.medication.domain.repository.SetIntakeEffectRepository
import java.time.Instant

/**
 * What home's medication card and History's overuse indicator read and write (BRD MED-F4, MED-F5, D46, D59). Failures are values (docs/ARCHITECTURE.md
 * section 7).
 */
class MedicationPromptRepository(
    private val medicationDataSource: MedicationDataSource,
    private val pendingEffectQuestionDomainMapper: PendingEffectQuestionDomainMapper,
    private val acuteIntakeDomainMapper: AcuteIntakeDomainMapper,
    private val overuseNoticeDomainMapper: OveruseNoticeDomainMapper,
    private val intakeEffectDataMapper: IntakeEffectDataMapper,
    private val overuseDataMapper: OveruseDataMapper
) : GetPendingEffectQuestionRepository,
    GetAcuteIntakesRepository,
    GetOveruseNoticesRepository,
    SetIntakeEffectRepository,
    CloseEffectQuestionRepository,
    AcknowledgeOveruseRepository {
    override fun pendingEffectQuestion(
        takenFrom: Instant,
        takenTo: Instant
    ): PendingEffectQuestionResultDomainModel = try {
        medicationDataSource.pendingEffectQuestion(takenFrom, takenTo)
            ?.let {
                PendingEffectQuestionResultDomainModel.Found(
                    pendingEffectQuestionDomainMapper.toDomain(it)
                )
            }
            ?: PendingEffectQuestionResultDomainModel.None
    } catch (_: DataException) {
        PendingEffectQuestionResultDomainModel.Failed
    }

    override fun acuteIntakesTakenBetween(
        from: Instant,
        to: Instant
    ): AcuteIntakesResultDomainModel = try {
        AcuteIntakesResultDomainModel.Loaded(
            medicationDataSource.acuteIntakesTakenBetween(from, to)
                .map(acuteIntakeDomainMapper::toDomain)
        )
    } catch (_: DataException) {
        AcuteIntakesResultDomainModel.Failed
    }

    override fun overuseNotices(): OveruseNoticesResultDomainModel = try {
        OveruseNoticesResultDomainModel.Loaded(
            medicationDataSource.overuseNotices().map(overuseNoticeDomainMapper::toDomain)
        )
    } catch (_: DataException) {
        OveruseNoticesResultDomainModel.Failed
    }

    override fun setIntakeEffect(intakeId: String, effect: IntakeEffectDomainModel) = write {
        medicationDataSource.setIntakeEffect(intakeId, intakeEffectDataMapper.toData(effect))
    }

    override fun closeEffectQuestion(intakeId: String) = write {
        medicationDataSource.closeEffectQuestion(intakeId)
    }

    override fun acknowledgeOveruse(
        category: OveruseCategoryDomainModel,
        level: OveruseLevelDomainModel,
        acknowledgedAt: Instant
    ) = write {
        medicationDataSource.acknowledgeOveruse(
            overuseDataMapper.toData(category),
            overuseDataMapper.toData(level),
            acknowledgedAt
        )
    }

    private inline fun write(block: () -> Unit): WriteResultDomainModel = try {
        block()
        WriteResultDomainModel.Done
    } catch (_: StorageFullDataException) {
        WriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        WriteResultDomainModel.Failed
    }
}
