package com.migrely.app.datasource.backupfile.validator

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.BuiltInSymptomDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.backup.model.BackupAttackDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.RECORDS
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRecordsValidatorTest {
    private val classUnderTest = BackupRecordsValidator()

    private fun BackupRecordsDataModel.refused() = assertFalse(classUnderTest.isValid(this))

    @Test
    fun `Given records the app wrote then valid`() {
        assertTrue(classUnderTest.isValid(RECORDS))
    }

    @Test
    fun `Given a repeated or blank ID then refused`() {
        RECORDS.copy(attacks = RECORDS.attacks.map { it.copy(id = "a") }).refused()
        RECORDS.copy(medications = RECORDS.medications.map { it.copy(id = "m") }).refused()
        RECORDS.copy(customTags = RECORDS.customTags.map { it.copy(id = "t") }).refused()
        RECORDS.copy(intakes = RECORDS.intakes.map { it.copy(id = "i") }).refused()
        RECORDS.copy(intakes = RECORDS.intakes.take(1).map { it.copy(id = " ") }).refused()
        RECORDS.copy(dayMarks = RECORDS.dayMarks.map { it.copy(date = RECORDS.dayMarks[0].date) })
            .refused()
    }

    @Test
    fun `Given two names that differ only in case or spaces then refused`() {
        RECORDS.copy(
            medications = RECORDS.medications.mapIndexed { index, it ->
                it.copy(name = if (index == 0) "Ibuprofen" else " IBUPROFEN")
            }
        ).refused()
        RECORDS.copy(
            customTags = RECORDS.customTags.map {
                it.copy(kind = RECORDS.customTags[0].kind, name = "Exams")
            }
        ).refused()
        RECORDS.copy(
            medications = RECORDS.medications.take(1).map {
                it.copy(name = " ")
            },
            intakes = emptyList()
        )
            .refused()
    }

    @Test
    fun `Given the same name for a symptom and a trigger then valid`() {
        assertTrue(
            classUnderTest.isValid(
                RECORDS.copy(
                    customTags = RECORDS.customTags.map {
                        it.copy(name = "Same")
                    }
                )
            )
        )
    }

    @Test
    fun `Given an intake pointing at nothing in the file then refused`() {
        RECORDS.copy(intakes = RECORDS.intakes.map { it.copy(medicationId = "gone") }).refused()
        RECORDS.copy(intakes = RECORDS.intakes.map { it.copy(attackId = "gone") }).refused()
    }

    @Test
    fun `Given an attack this app could not have stored then refused`() {
        val attack = RECORDS.attacks[0]
        fun with(changed: BackupAttackDataModel) =
            RECORDS.copy(attacks = listOf(changed, RECORDS.attacks[1]))

        with(attack.copy(end = attack.start.minusSeconds(1))).refused()
        with(attack.copy(end = null)).refused()
        with(
            attack.copy(intensityPoints = listOf(IntensityPointDataModel(attack.start, 11)))
        ).refused()
        with(
            attack.copy(intensityPoints = listOf(IntensityPointDataModel(attack.start, 0)))
        ).refused()
        with(attack.copy(functionalImpact = 4)).refused()
        with(
            attack.copy(
                tags = List(2) { AttackTagDataModel.BuiltInSymptom(BuiltInSymptomDataModel.Aura) }
            )
        ).refused()
    }

    @Test
    fun `Given a date with no mark on it then refused`() {
        RECORDS.copy(
            dayMarks = RECORDS.dayMarks.map {
                it.copy(period = false, painFree = false)
            }
        ).refused()
    }
}
