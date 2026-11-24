package com.migrely.app.report.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.report.domain.model.ReportCreditWriteResultDomainModel
import com.migrely.app.report.domain.repository.SetReportCreditRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class SetReportCreditUseCaseTest {
    private val setReportCreditRepository: SetReportCreditRepository = mock {
        on { setReportCreditShown(false) } doReturn ReportCreditWriteResultDomainModel.Saved
        on { setReportCreditShown(true) } doReturn ReportCreditWriteResultDomainModel.Failed
    }
    private val classUnderTest =
        SetReportCreditUseCase(setReportCreditRepository, CoroutineContextProvider.Default)

    @Test
    fun `When the switch changes then the choice is kept, and a failure comes back as a value`() {
        assertEquals(
            ReportCreditWriteResultDomainModel.Saved,
            classUnderTest.executeInBackground(false)
        )
        assertEquals(
            ReportCreditWriteResultDomainModel.Failed,
            classUnderTest.executeInBackground(true)
        )
    }
}
