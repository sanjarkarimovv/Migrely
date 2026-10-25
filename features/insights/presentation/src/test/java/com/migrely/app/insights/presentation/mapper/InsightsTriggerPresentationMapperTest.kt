package com.migrely.app.insights.presentation.mapper

import com.migrely.app.insights.domain.model.InsightsBuiltInTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerDomainModel
import com.migrely.app.insights.presentation.model.InsightsBuiltInTriggerPresentationModel
import com.migrely.app.insights.presentation.model.InsightsTriggerPresentationModel
import org.junit.Assert.assertEquals
import org.junit.Test

class InsightsTriggerPresentationMapperTest {
    private val classUnderTest = InsightsTriggerPresentationMapper()

    @Test
    fun `Each built-in trigger maps to its own, and the user's own keeps its name`() {
        InsightsBuiltInTriggerDomainModel.entries.forEach { trigger ->
            assertEquals(
                InsightsTriggerPresentationModel.BuiltIn(
                    InsightsBuiltInTriggerPresentationModel.valueOf(trigger.name)
                ),
                classUnderTest.toPresentation(InsightsTriggerDomainModel.BuiltIn(trigger))
            )
        }
        assertEquals(
            InsightsTriggerPresentationModel.Custom("Long drive"),
            classUnderTest.toPresentation(InsightsTriggerDomainModel.Custom("Long drive"))
        )
    }
}
