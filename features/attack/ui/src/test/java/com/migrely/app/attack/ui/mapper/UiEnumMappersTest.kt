package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.presentation.model.BuiltInSymptomPresentationModel
import com.migrely.app.attack.presentation.model.BuiltInTriggerPresentationModel
import com.migrely.app.attack.presentation.model.FunctionalImpactPresentationModel
import com.migrely.app.attack.presentation.model.HeadLocationPresentationModel
import com.migrely.app.attack.presentation.model.TagKindPresentationModel
import com.migrely.app.attack.presentation.model.TimeFieldPresentationModel
import com.migrely.app.attack.ui.model.BuiltInSymptomUiModel
import com.migrely.app.attack.ui.model.BuiltInTriggerUiModel
import com.migrely.app.attack.ui.model.FunctionalImpactUiModel
import com.migrely.app.attack.ui.model.HeadLocationUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

/** Every enum value maps to the value of the same name and back, and every choice has its own label. */
class UiEnumMappersTest {
    @Test
    fun `Locations round-trip and have distinct labels`() {
        HeadLocationPresentationModel.entries.forEach { presentation ->
            val ui = HeadLocationUiMapper().toUi(presentation)
            assertEquals(presentation.name, ui.name)
            assertEquals(presentation, HeadLocationUiToPresentationMapper().toPresentation(ui))
        }
        assertDistinct(HeadLocationUiModel.entries.map(HeadLocationLabelUiMapper()::toUi))
    }

    @Test
    fun `Symptoms round-trip and have distinct labels`() {
        BuiltInSymptomPresentationModel.entries.forEach { presentation ->
            val ui = BuiltInSymptomUiMapper().toUi(presentation)
            assertEquals(presentation.name, ui.name)
            assertEquals(presentation, BuiltInSymptomUiToPresentationMapper().toPresentation(ui))
        }
        assertDistinct(BuiltInSymptomUiModel.entries.map(BuiltInSymptomLabelUiMapper()::toUi))
    }

    @Test
    fun `Triggers round-trip and have distinct labels`() {
        BuiltInTriggerPresentationModel.entries.forEach { presentation ->
            val ui = BuiltInTriggerUiMapper().toUi(presentation)
            assertEquals(presentation.name, ui.name)
            assertEquals(presentation, BuiltInTriggerUiToPresentationMapper().toPresentation(ui))
        }
        assertDistinct(BuiltInTriggerUiModel.entries.map(BuiltInTriggerLabelUiMapper()::toUi))
    }

    @Test
    fun `Impacts round-trip and have distinct labels`() {
        FunctionalImpactPresentationModel.entries.forEach { presentation ->
            val ui = FunctionalImpactUiMapper().toUi(presentation)
            assertEquals(presentation.name, ui.name)
            assertEquals(presentation, FunctionalImpactUiToPresentationMapper().toPresentation(ui))
        }
        assertDistinct(FunctionalImpactUiModel.entries.map(FunctionalImpactLabelUiMapper()::toUi))
    }

    @Test
    fun `Tag kinds and time fields round-trip`() {
        TagKindPresentationModel.entries.forEach { presentation ->
            assertEquals(
                presentation,
                TagKindUiToPresentationMapper()
                    .toPresentation(TagKindUiMapper().toUi(presentation))
            )
        }
        TimeFieldPresentationModel.entries.forEach { presentation ->
            assertEquals(
                presentation,
                TimeFieldUiToPresentationMapper()
                    .toPresentation(TimeFieldUiMapper().toUi(presentation))
            )
        }
    }

    private fun assertDistinct(labels: List<Int>) = assertEquals(labels.size, labels.toSet().size)
}
