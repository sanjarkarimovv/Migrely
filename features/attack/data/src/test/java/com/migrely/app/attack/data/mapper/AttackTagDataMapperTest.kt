package com.migrely.app.attack.data.mapper

import com.migrely.app.attack.domain.model.AttackTagDomainModel
import com.migrely.app.attack.domain.model.BuiltInSymptomDomainModel
import com.migrely.app.attack.domain.model.BuiltInTriggerDomainModel
import com.migrely.app.attack.domain.model.CustomTagDomainModel
import com.migrely.app.attack.domain.model.HeadLocationDomainModel
import com.migrely.app.attack.domain.model.SymptomDomainModel
import com.migrely.app.attack.domain.model.TagKindDomainModel
import com.migrely.app.attack.domain.model.TriggerDomainModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AttackTagDataMapperTest(
    private val tag: AttackTagDomainModel,
    private val expected: AttackTagDataModel
) {
    companion object {
        private val customSymptom = CustomTagDomainModel(
            "s",
            TagKindDomainModel.Symptom,
            "Jaw pain",
            false
        )
        private val customTrigger = CustomTagDomainModel(
            "t",
            TagKindDomainModel.Trigger,
            "Perfume",
            true
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                AttackTagDomainModel.Location(HeadLocationDomainModel.AroundEyes),
                AttackTagDataModel.Location(HeadLocationDataModel.AroundEyes)
            ),
            arrayOf<Any>(
                AttackTagDomainModel.Symptom(
                    SymptomDomainModel.BuiltIn(BuiltInSymptomDomainModel.Nausea)
                ),
                AttackTagDataModel.BuiltInSymptom(BuiltInSymptomDataModel.Nausea)
            ),
            arrayOf<Any>(
                AttackTagDomainModel.Symptom(SymptomDomainModel.Custom(customSymptom)),
                AttackTagDataModel.CustomSymptom("s")
            ),
            arrayOf<Any>(
                AttackTagDomainModel.Trigger(
                    TriggerDomainModel.BuiltIn(BuiltInTriggerDomainModel.Period)
                ),
                AttackTagDataModel.BuiltInTrigger(BuiltInTriggerDataModel.Period)
            ),
            arrayOf<Any>(
                AttackTagDomainModel.Trigger(TriggerDomainModel.Custom(customTrigger)),
                AttackTagDataModel.CustomTrigger("t")
            )
        )
    }

    @Test
    fun `When mapping a tag to data then the stored tag matches`() {
        val classUnderTest = AttackTagDataMapper(
            HeadLocationDataMapper(),
            BuiltInSymptomDataMapper(),
            BuiltInTriggerDataMapper()
        )

        assertEquals(expected, classUnderTest.toData(tag))
    }
}
