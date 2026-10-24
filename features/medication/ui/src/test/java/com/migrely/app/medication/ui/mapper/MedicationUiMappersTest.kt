package com.migrely.app.medication.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.medication.presentation.model.EditMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.EditMedicationViewState
import com.migrely.app.medication.presentation.model.IntakePresentationModel
import com.migrely.app.medication.presentation.model.LogMedicationPresentationNotification
import com.migrely.app.medication.presentation.model.LogMedicationViewState
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationLogPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPresentationModel
import com.migrely.app.medication.presentation.model.MedicationsPresentationNotification
import com.migrely.app.medication.presentation.model.MedicationsViewState
import com.migrely.app.medication.presentation.model.SaveProblemPresentationModel
import com.migrely.app.medication.ui.model.EditMedicationViewStateUiModel
import com.migrely.app.medication.ui.model.IntakeUiModel
import com.migrely.app.medication.ui.model.LogMedicationViewStateUiModel
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import com.migrely.app.medication.ui.model.MedicationMessageBehaviourUiModel
import com.migrely.app.medication.ui.model.MedicationMessageUiModel
import com.migrely.app.medication.ui.model.MedicationUiModel
import com.migrely.app.medication.ui.model.MedicationsViewStateUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel
import com.migrely.app.medication.ui.notification.MedicationEventPresenter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
private val TIME: Instant = Instant.parse("2026-10-24T09:20:00Z")

private val PRESENTATION = MedicationPresentationModel(
    "m",
    "Ibuprofen",
    MedicationKindPresentationModel.Acute,
    MedicationCategoryPresentationModel.SimplePainkiller,
    "400 mg"
)
private val UI = MedicationUiModel(
    "m",
    "Ibuprofen",
    MedicationKindUiModel.Acute,
    MedicationCategoryUiModel.SimplePainkiller,
    "400 mg"
)
private val INTAKE = IntakePresentationModel("i", "Ibuprofen", "400 mg", TIME, ZONE, "a")
private val INTAKE_UI =
    IntakeUiModel("i", "Ibuprofen", "400 mg", TIME.toEpochMilli(), "Asia/Tashkent", "a")

class MedicationUiMappersTest {
    private val medicationUiMapper = MedicationUiMapper()

    @Test
    fun `When mapping a medication both ways then every field round-trips`() {
        val toPresentation = MedicationUiToPresentationMapper(
            MedicationKindUiToPresentationMapper(),
            MedicationCategoryUiToPresentationMapper()
        )
        listOf(
            PRESENTATION,
            PRESENTATION.copy(kind = MedicationKindPresentationModel.Preventive, category = null),
            PRESENTATION.copy(category = MedicationCategoryPresentationModel.Triptan),
            PRESENTATION.copy(category = MedicationCategoryPresentationModel.CombinationOrOther)
        ).forEach { medication ->
            assertEquals(
                medication,
                toPresentation.toPresentation(medicationUiMapper.toUi(medication))
            )
        }
        assertEquals(UI, medicationUiMapper.toUi(PRESENTATION))
    }

    @Test
    fun `When mapping view states then each screen's ui model`() {
        assertEquals(
            MedicationsViewStateUiModel.Ready(listOf(UI)),
            MedicationsViewStateUiMapper(
                medicationUiMapper
            ).toUi(MedicationsViewState.Ready(listOf(PRESENTATION)))
        )
        assertEquals(
            MedicationsViewStateUiModel.Loading,
            MedicationsViewStateUiMapper(medicationUiMapper).toUi(MedicationsViewState.Loading)
        )
        val edit = EditMedicationViewStateUiMapper(medicationUiMapper)
        assertEquals(
            EditMedicationViewStateUiModel.Ready(null),
            edit.toUi(EditMedicationViewState.Ready(null))
        )
        assertEquals(
            EditMedicationViewStateUiModel.NotFound,
            edit.toUi(EditMedicationViewState.NotFound)
        )
        assertEquals(
            LogMedicationViewStateUiModel.Ready(
                MedicationLogUiModel(TIME.toEpochMilli(), "Asia/Tashkent", "a", listOf(UI))
            ),
            LogMedicationViewStateUiMapper(medicationUiMapper).toUi(
                LogMedicationViewState.Ready(
                    MedicationLogPresentationModel(TIME, ZONE, "a", listOf(PRESENTATION))
                )
            )
        )
    }

    @Test
    fun `When mapping an intake and save problems then their ui models`() {
        assertEquals(INTAKE_UI, IntakeUiMapper().toUi(INTAKE))
        val problems = SaveProblemUiMapper()
        assertEquals(
            SaveProblemUiModel.Duplicate("Ibuprofen"),
            problems.toUi(SaveProblemPresentationModel.Duplicate("Ibuprofen"))
        )
        assertEquals(
            SaveProblemUiModel.ArchivedName("Sumatriptan"),
            problems.toUi(SaveProblemPresentationModel.ArchivedName("Sumatriptan"))
        )
        assertEquals(
            SaveProblemUiModel.LimitReached,
            problems.toUi(SaveProblemPresentationModel.LimitReached)
        )
        assertEquals(
            SaveProblemUiModel.InvalidName,
            problems.toUi(SaveProblemPresentationModel.InvalidName)
        )
        assertEquals(
            SaveProblemUiModel.InvalidDose,
            problems.toUi(SaveProblemPresentationModel.InvalidDose)
        )
    }

    @Test
    fun `When the list reports then the presenter shows the message`() {
        val presenter: MedicationEventPresenter = mock()
        val mapper = MedicationsNotificationUiMapper(presenter)

        mapper.toUi(MedicationsPresentationNotification.Archived("m")).present()
        mapper.toUi(MedicationsPresentationNotification.StorageFull).present()
        mapper.toUi(MedicationsPresentationNotification.LimitReached).present()

        verify(
            presenter
        ).show(MedicationEventUiModel.Message(MedicationMessageUiModel.Archived("m")))
        verify(presenter)
            .show(MedicationEventUiModel.Message(MedicationMessageUiModel.LimitReached))
        verify(presenter).show(MedicationEventUiModel.Message(MedicationMessageUiModel.StorageFull))
    }

    @Test
    fun `When the editor refuses then a field problem, or a message for the limit`() {
        val presenter: MedicationEventPresenter = mock()
        val mapper = EditMedicationNotificationUiMapper(presenter, SaveProblemUiMapper())

        mapper.toUi(
            EditMedicationPresentationNotification.NotSaved(
                SaveProblemPresentationModel.InvalidName
            )
        ).present()
        mapper.toUi(
            EditMedicationPresentationNotification.NotSaved(
                SaveProblemPresentationModel.LimitReached
            )
        ).present()

        verify(presenter).show(MedicationEventUiModel.NotSaved(SaveProblemUiModel.InvalidName))
        verify(
            presenter
        ).show(MedicationEventUiModel.Message(MedicationMessageUiModel.LimitReached))
    }

    @Test
    fun `When logging reports then logged, undone or in future`() {
        val presenter: MedicationEventPresenter = mock()
        val mapper =
            LogMedicationNotificationUiMapper(presenter, SaveProblemUiMapper(), IntakeUiMapper())

        mapper.toUi(LogMedicationPresentationNotification.Logged(INTAKE)).present()
        mapper.toUi(LogMedicationPresentationNotification.IntakeUndone).present()
        mapper.toUi(LogMedicationPresentationNotification.InFuture).present()

        verify(presenter).show(MedicationEventUiModel.Logged(INTAKE_UI))
        verify(
            presenter
        ).show(MedicationEventUiModel.Message(MedicationMessageUiModel.IntakeUndone))
        verify(presenter).show(MedicationEventUiModel.Message(MedicationMessageUiModel.InFuture))
    }

    @Test
    fun `When mapping behaviours then only an archive offers undo and storage full stays`() {
        val mapper = MedicationMessageBehaviourUiMapper()

        assertEquals(
            MedicationMessageBehaviourUiModel(SnackbarTone.Information, true, 5_000L),
            mapper.toUi(MedicationMessageUiModel.Archived("m"))
        )
        assertEquals(
            MedicationMessageBehaviourUiModel(SnackbarTone.Information, false, 8_000L),
            mapper.toUi(MedicationMessageUiModel.IntakeUndone)
        )
        assertEquals(
            MedicationMessageBehaviourUiModel(SnackbarTone.Error, false, null),
            mapper.toUi(MedicationMessageUiModel.StorageFull)
        )
    }

    @Test
    fun `When formatting a time then the clock today and the date on another day`() {
        val formatter = MedicationTimeUiFormatter(
            locale = { Locale.UK },
            is24HourFormat = { true },
            deviceZone = { ZONE },
            bestDateTimePattern = { _, skeleton ->
                if (skeleton ==
                    "EEEdMMM"
                ) {
                    "EEE d MMM"
                } else {
                    "HH:mm"
                }
            }
        )
        fun millis(local: String) =
            LocalDateTime.parse(local).atZone(ZONE).toInstant().toEpochMilli()
        val now = millis("2026-10-24T17:00")

        assertEquals(
            "14:20",
            formatter.formatTime(millis("2026-10-24T14:20"), "Asia/Tashkent", now)
        )
        assertEquals(
            "Thu 22 Oct, 09:05",
            formatter.formatTime(millis("2026-10-22T09:05"), "Asia/Tashkent", now)
        )
        // Taken where the attack started, away from the device: that zone's clock and city (EC-TIM-02).
        assertEquals(
            "18:20 (Tokyo)",
            formatter.formatTime(millis("2026-10-24T14:20"), "Asia/Tokyo", now)
        )
    }
}
