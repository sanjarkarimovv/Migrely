package com.migrely.app.report.ui.mapper

import com.migrely.app.architecture.ui.notification.mapper.NotificationUiMapper
import com.migrely.app.architecture.ui.notification.model.UiNotification
import com.migrely.app.report.presentation.model.ReportPeriodPresentationModel
import com.migrely.app.report.presentation.model.ReportPresentationNotification
import com.migrely.app.report.presentation.model.ReportViewState
import com.migrely.app.report.ui.model.ReportEventUiModel
import com.migrely.app.report.ui.model.ReportMessageUiModel
import com.migrely.app.report.ui.model.ReportOptionsUiModel
import com.migrely.app.report.ui.model.ReportPeriodUiModel
import com.migrely.app.report.ui.model.ReportViewStateUiModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ReportViewStateUiMapper {
    fun toUi(viewState: ReportViewState): ReportViewStateUiModel = when (viewState) {
        ReportViewState.Loading -> ReportViewStateUiModel.Loading
        is ReportViewState.Ready -> ReportViewStateUiModel.Ready(
            ReportOptionsUiModel(
                viewState.options.from.toEpochDay(),
                viewState.options.to.toEpochDay(),
                viewState.options.anythingLogged
            )
        )
        ReportViewState.Working -> ReportViewStateUiModel.Working
        ReportViewState.Unavailable -> ReportViewStateUiModel.Unavailable
    }
}

class ReportPeriodPresentationMapper {
    fun toPresentation(period: ReportPeriodUiModel): ReportPeriodPresentationModel = when (period) {
        is ReportPeriodUiModel.LastMonths -> ReportPeriodPresentationModel.LastMonths(period.months)
        is ReportPeriodUiModel.Range -> ReportPeriodPresentationModel.Range(
            LocalDate.ofEpochDay(period.fromEpochDay),
            LocalDate.ofEpochDay(period.toEpochDay)
        )
    }
}

class ReportEventPresenter {
    private val _events = MutableSharedFlow<ReportEventUiModel>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<ReportEventUiModel> = _events.asSharedFlow()

    fun show(event: ReportEventUiModel) {
        _events.tryEmit(event)
    }

    private companion object {
        const val EVENT_BUFFER = 8
    }
}

class ReportNotificationUiMapper(private val presenter: ReportEventPresenter) :
    NotificationUiMapper<ReportPresentationNotification> {
    override fun toUi(notification: ReportPresentationNotification): UiNotification {
        val event = when (notification) {
            is ReportPresentationNotification.Share -> ReportEventUiModel.Share(notification.uri)
            ReportPresentationNotification.NothingLogged ->
                ReportEventUiModel.Message(ReportMessageUiModel.NothingLogged)
            ReportPresentationNotification.StorageFull ->
                ReportEventUiModel.Message(ReportMessageUiModel.StorageFull)
            ReportPresentationNotification.SomethingWentWrong ->
                ReportEventUiModel.Message(ReportMessageUiModel.SomethingWentWrong)
        }
        return UiNotification { presenter.show(event) }
    }
}
