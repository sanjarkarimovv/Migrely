package com.migrely.app.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertTrue
import org.junit.Test

private const val MIN_BODY_LINE_HEIGHT_RATIO = 1.4f

class TypographyTest {
    private val allStyles = with(Typography) {
        mapOf(
            "displayLarge" to displayLarge,
            "displayMedium" to displayMedium,
            "displaySmall" to displaySmall,
            "headlineLarge" to headlineLarge,
            "headlineMedium" to headlineMedium,
            "headlineSmall" to headlineSmall,
            "titleLarge" to titleLarge,
            "titleMedium" to titleMedium,
            "titleSmall" to titleSmall,
            "bodyLarge" to bodyLarge,
            "bodyMedium" to bodyMedium,
            "bodySmall" to bodySmall,
            "labelLarge" to labelLarge,
            "labelMedium" to labelMedium,
            "labelSmall" to labelSmall
        )
    }

    @Test
    fun `Every body style has a line height of at least 1,4 times its size`() {
        listOf(Typography.bodyLarge, Typography.bodyMedium, Typography.bodySmall).forEach { style ->
            val ratio = style.lineHeight.value / style.fontSize.value
            assertTrue("line height ratio $ratio", ratio >= MIN_BODY_LINE_HEIGHT_RATIO)
        }
    }

    @Test
    fun `No style is lighter than weight 400`() {
        allStyles.forEach { (name, style: TextStyle) ->
            val weight = requireNotNull(style.fontWeight) { "$name has no weight" }
            assertTrue("$name weight ${weight.weight}", weight >= FontWeight.Normal)
        }
    }
}
