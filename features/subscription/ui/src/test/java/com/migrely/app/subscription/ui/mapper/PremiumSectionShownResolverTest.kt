package com.migrely.app.subscription.ui.mapper

import com.migrely.app.subscription.ui.model.PremiumPlanUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionUiModel
import com.migrely.app.subscription.ui.model.PremiumSectionViewStateUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import com.migrely.app.subscription.ui.model.RestoreNoteUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

private val FREE = PremiumStatusUiModel.Free
private val LIFETIME = PremiumStatusUiModel.Lifetime(subscriptionRenewsToo = false)
private val NOTHING_FOUND = PremiumSectionUiModel(FREE, RestoreNoteUiModel.NothingFound)

private fun ready(section: PremiumSectionUiModel) = PremiumSectionViewStateUiModel.Ready(section)

class PremiumSectionShownResolverTest {
    private val classUnderTest = PremiumSectionShownResolver()

    @Test
    fun `Given a restore note when the same status is read after a rotation then it stays`() {
        val resolved = classUnderTest.resolve(
            NOTHING_FOUND,
            ready(PremiumSectionUiModel(FREE, null))
        )

        assertEquals(NOTHING_FOUND, resolved)
    }

    @Test
    fun `Given a restore note when the status changes then the note goes`() {
        val resolved =
            classUnderTest.resolve(NOTHING_FOUND, ready(PremiumSectionUiModel(LIFETIME, null)))

        assertEquals(PremiumSectionUiModel(LIFETIME, note = null), resolved)
    }

    @Test
    fun `Given a trial's note when the trial has ended then the note goes`() {
        val trial = PremiumStatusUiModel.Subscribed(PremiumPlanUiModel.Yearly, trialEndMillis = 1L)
        val ended = PremiumStatusUiModel.Subscribed(
            PremiumPlanUiModel.Yearly,
            trialEndMillis = null
        )
        val shown = PremiumSectionUiModel(trial, RestoreNoteUiModel.Restored)

        val resolved = classUnderTest.resolve(shown, ready(PremiumSectionUiModel(ended, null)))

        assertEquals(PremiumSectionUiModel(ended, note = null), resolved)
    }

    @Test
    fun `Given a new restore when it answers then its own note shows`() {
        val answer = PremiumSectionUiModel(FREE, RestoreNoteUiModel.Unreachable)

        assertEquals(answer, classUnderTest.resolve(NOTHING_FOUND, ready(answer)))
    }

    @Test
    fun `Given a new restore when it is checking then the last note goes and the status stays`() {
        assertEquals(
            PremiumSectionUiModel(FREE, note = null),
            classUnderTest.resolve(NOTHING_FOUND, PremiumSectionViewStateUiModel.Checking)
        )
    }

    @Test
    fun `Given the section loads then what is shown stays`() {
        assertEquals(
            NOTHING_FOUND,
            classUnderTest.resolve(NOTHING_FOUND, PremiumSectionViewStateUiModel.Loading)
        )
        assertEquals(null, classUnderTest.resolve(null, PremiumSectionViewStateUiModel.Loading))
    }
}
