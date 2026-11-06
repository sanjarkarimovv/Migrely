package com.migrely.app.medication.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OveruseStatusRowDomainModelTest {
    @Test
    fun `Given one day below a threshold of 10 then not reached`() {
        assertFalse(OveruseStatusRowDomainModel(OveruseCategoryDomainModel.Triptan, 9, 10).reached)
    }

    @Test
    fun `Given one day below a threshold of 15 then not reached`() {
        assertFalse(
            OveruseStatusRowDomainModel(OveruseCategoryDomainModel.SimplePainkiller, 14, 15).reached
        )
    }

    @Test
    fun `Given exactly a threshold of 10 then reached`() {
        assertTrue(OveruseStatusRowDomainModel(OveruseCategoryDomainModel.Triptan, 10, 10).reached)
    }

    @Test
    fun `Given exactly a threshold of 15 then reached`() {
        assertTrue(
            OveruseStatusRowDomainModel(OveruseCategoryDomainModel.SimplePainkiller, 15, 15).reached
        )
    }
}
