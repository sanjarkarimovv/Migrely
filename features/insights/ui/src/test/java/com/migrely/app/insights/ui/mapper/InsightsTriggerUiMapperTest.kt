package com.migrely.app.insights.ui.mapper

import com.migrely.app.insights.presentation.model.InsightsBuiltInTriggerPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerPresentationModel
import com.migrely.app.insights.ui.model.InsightsBuiltInTriggerUiModel
import com.migrely.app.insights.ui.model.InsightsTriggerUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

class InsightsTriggerUiMapperTest {
    private val classUnderTest = InsightsTriggerUiMapper()

    @Test
    fun `Each built-in trigger maps to its own, and the user's own keeps its name`() {
        InsightsBuiltInTriggerPresentationModel.entries.forEach { trigger ->
            assertEquals(
                InsightsTriggerUiModel.BuiltIn(InsightsBuiltInTriggerUiModel.valueOf(trigger.name)),
                classUnderTest.toUi(InsightsTriggerPresentationModel.BuiltIn(trigger))
            )
        }
        assertEquals(
            InsightsTriggerUiModel.Custom("Long drive"),
            classUnderTest.toUi(InsightsTriggerPresentationModel.Custom("Long drive"))
        )
    }
}
