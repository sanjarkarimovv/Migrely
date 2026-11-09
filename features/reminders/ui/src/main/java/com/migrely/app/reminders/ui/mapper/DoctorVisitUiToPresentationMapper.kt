package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.DoctorVisitPresentationModel
import com.migrely.app.reminders.ui.model.DoctorVisitUiModel
import java.time.LocalDate

class DoctorVisitUiToPresentationMapper {
    fun toPresentation(visit: DoctorVisitUiModel) =
        DoctorVisitPresentationModel(visit.id, LocalDate.ofEpochDay(visit.epochDay))
}
