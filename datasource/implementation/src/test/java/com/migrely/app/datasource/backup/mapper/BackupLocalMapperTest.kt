package com.migrely.app.datasource.backup.mapper

import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.backupfile.RECORDS
import com.migrely.app.datasource.daymark.mapper.DayMarkLocalMapper
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupLocalMapperTest {
    private val classUnderTest = BackupLocalMapper(
        AttackTagLocalMapper(),
        CustomTagLocalMapper(),
        MedicationLocalMapper(),
        DayMarkLocalMapper()
    )

    @Test
    fun `When records are stored and read back then they are as they were`() {
        assertEquals(RECORDS, classUnderTest.toData(classUnderTest.toLocal(RECORDS)))
    }

    @Test
    fun `When storing then rows carry the stored keys and names their key`() {
        val local = classUnderTest.toLocal(RECORDS)

        assertEquals(
            listOf("custom:t1"),
            local.attackTags.filter { it.kind == "TRIGGER" && it.tagKey.startsWith("custom:") }
                .map(AttackTagLocalModel::tagKey)
        )
        assertEquals(listOf("ibuprofen", "topiramate"), local.medications.map { it.nameKey })
        assertEquals(listOf("exams", "yawning"), local.customTags.map { it.nameKey })
        assertEquals(listOf(0L, 0L, 0L), local.intensityPoints.map { it.id })
        assertEquals(listOf(null, null), local.attacks.map { it.deletedEpochMillis })
    }

    @Test
    fun `Given rows this version cannot read when reading then they are left out`() {
        val local = classUnderTest.toLocal(RECORDS)
        val read = classUnderTest.toData(
            local.copy(
                attackTags = local.attackTags + AttackTagLocalModel("a1", "SYMPTOM", "sneezing"),
                customTags = local.customTags + local.customTags[0].copy(id = "t9", kind = "MOOD"),
                dayMarks = local.dayMarks + DayMarkLocalModel("not a date", true, false, 1L)
            )
        )

        assertEquals(RECORDS, read)
    }
}
