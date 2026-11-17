package com.migrely.app.home.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val ADD_PAST = "Add a past migraine"
private const val LOG = "Log medication"

/** The links' row on a 360 dp phone (ux A5): the window less Today's 16 dp padding on each side. */
private val ROW = 328.dp

/**
 * Home Today's two quiet links stack, each a full-width row of at least 48 dp, when either
 * label's longest word does not fit half the row; side by side otherwise. Tab order stays "Add a
 * past migraine", then "Log medication" ([QuietLinksOrderTest]) (BRD D181 (5), EC-UX-48). Native
 * graphics, so text is measured with real fonts.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp")
class QuietLinksTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(fontScale: Float) = rule.setContent {
        val density = LocalDensity.current
        CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
            MigrelyTheme {
                TodayContents(
                    today = TodayUiModel(SkyUiModel.Calm, 0, null, periodOffered = false),
                    dayTitle = "Today",
                    nowMillis = 0L,
                    skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                    timeUiFormatter = previewFormatter,
                    onStartAttack = {},
                    onAttackDetails = {},
                    onAddPastAttack = {}
                )
            }
        }
    }

    private fun bounds(label: String): DpRect =
        rule.onNodeWithText(label).getUnclippedBoundsInRoot()

    @Test
    fun `At 200 percent the links stack, each full width and at least 48 dp tall`() {
        show(fontScale = 2f)

        val addPast = bounds(ADD_PAST)
        val log = bounds(LOG)
        assertEquals(addPast.left.value, log.left.value, 0.5f)
        assertNotEquals(addPast.top.value, log.top.value, 0.5f)
        assertTrue("Add a past migraine above", addPast.bottom <= log.top)
        listOf(addPast, log).forEach { link ->
            assertTrue("${link.bottom - link.top} tall", link.bottom - link.top >= 48.dp)
            assertEquals(ROW.value, (link.right - link.left).value, 0.5f)
        }
    }

    @Test
    fun `At 100 percent the links stay side by side`() {
        show(fontScale = 1f)

        val addPast = bounds(ADD_PAST)
        val log = bounds(LOG)
        assertEquals(addPast.top.value, log.top.value, 0.5f)
        assertTrue("Add a past migraine first", addPast.right <= log.left)
    }
}
