package com.migrely.app.datasource.attack.mapper

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.BuiltInTriggerDataModel
import com.migrely.app.datasource.attack.model.HeadLocationDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AttackTagLocalMapperTest(
    private val tag: AttackTagDataModel,
    private val local: AttackTagLocalModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: {0}")
        fun data(): Collection<Array<*>> = listOf(
            arrayOf<Any>(
                AttackTagDataModel.Location(HeadLocationDataModel.BackOfHead),
                AttackTagLocalModel("a", "LOCATION", "back_of_head")
            ),
            arrayOf<Any>(
                AttackTagDataModel.BuiltInSymptom(BuiltInSymptomDataModel.LightSensitivity),
                AttackTagLocalModel("a", "SYMPTOM", "light_sensitivity")
            ),
            arrayOf<Any>(
                AttackTagDataModel.BuiltInTrigger(BuiltInTriggerDataModel.TooLittleSleep),
                AttackTagLocalModel("a", "TRIGGER", "too_little_sleep")
            ),
            arrayOf<Any>(
                AttackTagDataModel.CustomSymptom("t-1"),
                AttackTagLocalModel("a", "SYMPTOM", "custom:t-1")
            ),
            arrayOf<Any>(
                AttackTagDataModel.CustomTrigger("t-2"),
                AttackTagLocalModel("a", "TRIGGER", "custom:t-2")
            )
        )
    }

    private val classUnderTest = AttackTagLocalMapper()

    @Test
    fun `When storing a tag then the row has its stable key`() {
        assertEquals(local, classUnderTest.toLocal("a", tag))
    }

    @Test
    fun `When reading the row then the same tag comes back`() {
        assertEquals(tag, classUnderTest.toData(local))
    }
}

class AttackTagLocalMapperUnknownKeysTest {
    private val classUnderTest = AttackTagLocalMapper()

    @Test
    fun `Given a key this version does not know when reading then it is skipped`() {
        assertNull(classUnderTest.toData(AttackTagLocalModel("a", "SYMPTOM", "tingling")))
    }

    @Test
    fun `Given a kind this version does not know when reading then it is skipped`() {
        assertNull(classUnderTest.toData(AttackTagLocalModel("a", "MOOD", "calm")))
    }
}
