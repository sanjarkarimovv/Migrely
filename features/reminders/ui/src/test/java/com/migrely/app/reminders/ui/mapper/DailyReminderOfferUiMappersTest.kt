package com.migrely.app.reminders.ui.mapper

import com.migrely.app.reminders.presentation.model.DailyReminderOfferPresentationNotification
import com.migrely.app.reminders.presentation.model.DailyReminderOfferViewState
import com.migrely.app.reminders.ui.model.DailyReminderOfferEventUiModel
import com.migrely.app.reminders.ui.model.DailyReminderOfferUiModel
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyReminderOfferUiMappersTest {
    @Test
    fun `When mapping the offer then nothing while loading, then its minute of the day`() {
        assertNull(DailyReminderOfferUiMapper().toUi(DailyReminderOfferViewState.Loading))
        assertEquals(
            DailyReminderOfferUiModel(21 * 60 + 30),
            DailyReminderOfferUiMapper().toUi(
                DailyReminderOfferViewState.Ready(LocalTime.of(21, 30))
            )
        )
    }

    @Test
    fun `When mapping its notifications then ask for permission or done`() {
        assertEquals(
            DailyReminderOfferEventUiModel.AskForPermission,
            DailyReminderOfferEventUiMapper().toUi(
                DailyReminderOfferPresentationNotification.AskForPermission
            )
        )
        assertEquals(
            DailyReminderOfferEventUiModel.Done,
            DailyReminderOfferEventUiMapper().toUi(DailyReminderOfferPresentationNotification.Done)
        )
    }
}
