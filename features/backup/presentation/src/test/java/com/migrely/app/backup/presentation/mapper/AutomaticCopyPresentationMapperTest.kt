package com.migrely.app.backup.presentation.mapper

import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel
import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.presentation.model.AutomaticCopyPresentationModel
import com.migrely.app.backup.presentation.model.BackupSummaryPresentationModel
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AutomaticCopyPresentationMapperTest {
    private val classUnderTest = AutomaticCopyPresentationMapper(BackupSummaryPresentationMapper())

    @Test
    fun `When mapping a copy then when it was made and what it holds, and none for none`() {
        val madeAt = Instant.parse("2026-10-24T19:30:00Z")

        assertEquals(
            AutomaticCopyPresentationModel(madeAt, BackupSummaryPresentationModel(16, 2, 5)),
            classUnderTest.toPresentation(
                AutomaticCopyDomainModel.Available(madeAt, BackupSummaryDomainModel(16, 2, 5))
            )
        )
        assertNull(classUnderTest.toPresentation(AutomaticCopyDomainModel.None))
    }
}
