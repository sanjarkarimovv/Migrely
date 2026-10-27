package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.presentation.model.DeleteAllDataPresentationNotification
import com.migrely.app.settings.presentation.model.DeleteAllDataViewState
import com.migrely.app.settings.ui.model.DeleteAllDataViewStateUiModel
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteAllDataUiMappersTest {
    @Test
    fun `When mapping the state then confirming or deleting`() {
        assertEquals(
            DeleteAllDataViewStateUiModel.Confirming,
            DeleteAllDataViewStateUiMapper().toUi(DeleteAllDataViewState.Confirming)
        )
        assertEquals(
            DeleteAllDataViewStateUiModel.Deleting,
            DeleteAllDataViewStateUiMapper().toUi(DeleteAllDataViewState.Deleting)
        )
    }

    @Test
    fun `When deleting all data stopped then the presenter says so`() = runTest {
        val presenter = DeleteAllDataEventPresenter()
        val shown = async(start = UNDISPATCHED) { presenter.notDeleted.first() }

        DeleteAllDataNotificationUiMapper(presenter)
            .toUi(DeleteAllDataPresentationNotification.NotDeleted)
            .present()

        assertEquals(Unit, shown.await())
    }
}
