package com.migrely.app.settings.ui.mapper

import com.migrely.app.settings.ui.model.DeleteAllDataViewStateUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DeleteProgressUiResolverTest(
    private val case: String,
    private val started: Boolean,
    private val state: DeleteAllDataViewStateUiModel,
    private val shown: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun parameters() = listOf(
            arrayOf<Any?>("not started", false, DeleteAllDataViewStateUiModel.Confirming, false),
            arrayOf<Any?>(
                "confirmed, before the state says deleting",
                true,
                DeleteAllDataViewStateUiModel.Confirming,
                true
            ),
            arrayOf<Any?>(
                "rebuilt while deleting",
                false,
                DeleteAllDataViewStateUiModel.Deleting,
                true
            ),
            arrayOf<Any?>(
                "deleted, until the restart",
                true,
                DeleteAllDataViewStateUiModel.Deleting,
                true
            )
        )
    }

    @Test
    fun `When deciding then the progress shows from the confirmation on`() {
        assertEquals(shown, DeleteProgressUiResolver().resolve(started, state))
    }
}
