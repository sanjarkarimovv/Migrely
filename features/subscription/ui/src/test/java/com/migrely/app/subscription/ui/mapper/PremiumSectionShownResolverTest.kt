package com.migrely.app.subscription.ui.mapper

import com.migrely.app.subscription.ui.model.PremiumSectionUiModel
import com.migrely.app.subscription.ui.model.PremiumStatusUiModel
import com.migrely.app.subscription.ui.model.RestoreNoteUiModel
import org.junit.Assert.assertEquals
import org.junit.Test

private val FREE = PremiumStatusUiModel.Free
private val LIFETIME = PremiumStatusUiModel.Lifetime(subscriptionRenewsToo = false)

class PremiumSectionShownResolverTest {
    private val classUnderTest = PremiumSectionShownResolver()

    @Test
    fun `Given a restore note when the same status is read after a rotation then it stays`() {
        val shown = PremiumSectionUiModel(FREE, RestoreNoteUiModel.NothingFound)

        val resolved = classUnderTest.resolve(shown, PremiumSectionUiModel(FREE, note = null))

        assertEquals(shown, resolved)
    }

    @Test
    fun `Given a restore's note when the status changes then the note goes`() {
        val shown = PremiumSectionUiModel(FREE, RestoreNoteUiModel.NothingFound)

        val resolved = classUnderTest.resolve(shown, PremiumSectionUiModel(LIFETIME, note = null))

        assertEquals(PremiumSectionUiModel(LIFETIME, note = null), resolved)
    }

    @Test
    fun `Given a new restore when it answers then its own note shows`() {
        val shown = PremiumSectionUiModel(FREE, RestoreNoteUiModel.NothingFound)
        val answer = PremiumSectionUiModel(FREE, RestoreNoteUiModel.Unreachable)

        assertEquals(answer, classUnderTest.resolve(shown, answer))
    }

    @Test
    fun `Given nothing shown yet when the status is read then it shows`() {
        val answer = PremiumSectionUiModel(FREE, note = null)

        assertEquals(answer, classUnderTest.resolve(null, answer))
    }
}
