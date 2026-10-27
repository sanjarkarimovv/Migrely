package com.migrely.app.datasource.tag.mapper

import com.migrely.app.datasource.tag.model.CustomTagDataModel
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CustomTagLocalMapperTest {
    private val classUnderTest = CustomTagLocalMapper()

    @Test
    fun `When reading a trigger row then maps every field`() {
        val local = CustomTagLocalModel("t", "TRIGGER", "Perfume", "perfume", archived = true, 1L)

        assertEquals(
            CustomTagDataModel("t", CustomTagKindDataModel.Trigger, "Perfume", archived = true),
            classUnderTest.toData(local)
        )
    }

    @Test
    fun `Given an unknown kind when reading then skipped`() {
        assertNull(
            classUnderTest.toData(CustomTagLocalModel("t", "MOOD", "Calm", "calm", false, 1L))
        )
    }

    @Test
    fun `When storing a symptom kind then uses SYMPTOM`() {
        assertEquals("SYMPTOM", classUnderTest.toLocalKind(CustomTagKindDataModel.Symptom))
    }
}
