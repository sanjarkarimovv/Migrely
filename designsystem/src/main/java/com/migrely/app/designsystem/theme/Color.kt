package com.migrely.app.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Low-brightness palette for people with photophobia (BRD EC-UX-01): near-black backgrounds instead of
// pure black to reduce halation, soft grey text instead of white, muted accents.

internal val Background = Color(0xFF0F1115)
internal val OnBackground = Color(0xFFC8CCD2)

internal val Surface = Color(0xFF161A20)
internal val OnSurface = Color(0xFFC8CCD2)
internal val SurfaceVariant = Color(0xFF20252D)
internal val OnSurfaceVariant = Color(0xFF9AA0A9)
internal val SurfaceContainerLowest = Color(0xFF0B0D10)
internal val SurfaceContainerLow = Color(0xFF13161B)
internal val SurfaceContainer = Color(0xFF181C22)
internal val SurfaceContainerHigh = Color(0xFF1F242B)
internal val SurfaceContainerHighest = Color(0xFF262C34)

internal val Primary = Color(0xFFA9B1E8)
internal val OnPrimary = Color(0xFF1B1F3A)
internal val PrimaryContainer = Color(0xFF2E3460)
internal val OnPrimaryContainer = Color(0xFFD6DAF5)

internal val Secondary = Color(0xFF9FB4C7)
internal val OnSecondary = Color(0xFF16202A)
internal val SecondaryContainer = Color(0xFF26323E)
internal val OnSecondaryContainer = Color(0xFFCFDBE6)

// Desaturated amber, reserved for warnings such as medication overuse.
internal val Tertiary = Color(0xFFD9B26F)
internal val OnTertiary = Color(0xFF2A1F0B)
internal val TertiaryContainer = Color(0xFF3E3220)
internal val OnTertiaryContainer = Color(0xFFEBD5AE)

internal val Error = Color(0xFFE6A4A4)
internal val OnError = Color(0xFF3A1414)
internal val ErrorContainer = Color(0xFF4A2424)
internal val OnErrorContainer = Color(0xFFF0CFCF)

internal val Outline = Color(0xFF3A414B)
internal val OutlineVariant = Color(0xFF2A3038)

internal val InverseSurface = Color(0xFFC8CCD2)
internal val InverseOnSurface = Color(0xFF1A1D22)
internal val InversePrimary = Color(0xFF4A5290)

internal val Scrim = Color(0xFF000000)

internal val DarkPositive = Color(0xFF8FB8A0)

// The light theme (BRD D94): a warm paper background instead of pure white, which glares, and the same indigo, slate
// and amber hues made deep enough that every text colour keeps at least 4.5:1 on its background and on the containers
// drawn over it. Outlines keep 3:1, the minimum for the edge of a control.

internal val LightBackground = Color(0xFFF5F3EE)
internal val LightOnBackground = Color(0xFF1E232A)

internal val LightSurface = Color(0xFFF5F3EE)
internal val LightOnSurface = Color(0xFF1E232A)
internal val LightSurfaceVariant = Color(0xFFE4E1DA)
internal val LightOnSurfaceVariant = Color(0xFF4A505A)
internal val LightSurfaceContainerLowest = Color(0xFFFAF8F4)
internal val LightSurfaceContainerLow = Color(0xFFF0EEE8)
internal val LightSurfaceContainer = Color(0xFFEBE8E2)
internal val LightSurfaceContainerHigh = Color(0xFFE5E2DB)
internal val LightSurfaceContainerHighest = Color(0xFFDFDCD4)
internal val LightSurfaceDim = Color(0xFFDAD7CF)

internal val LightPrimary = Color(0xFF4A5290)
internal val LightOnPrimary = Color(0xFFF5F3EE)
internal val LightPrimaryContainer = Color(0xFFDDE0F5)
internal val LightOnPrimaryContainer = Color(0xFF262B55)

internal val LightSecondary = Color(0xFF4B6074)
internal val LightOnSecondary = Color(0xFFF5F3EE)
internal val LightSecondaryContainer = Color(0xFFD6DEE7)
internal val LightOnSecondaryContainer = Color(0xFF1D2B37)

internal val LightTertiary = Color(0xFF7A5916)
internal val LightOnTertiary = Color(0xFFF5F3EE)
internal val LightTertiaryContainer = Color(0xFFF0DFBB)
internal val LightOnTertiaryContainer = Color(0xFF3A2A08)

internal val LightError = Color(0xFF9E3434)
internal val LightOnError = Color(0xFFF5F3EE)
internal val LightErrorContainer = Color(0xFFF4D7D3)
internal val LightOnErrorContainer = Color(0xFF4A1515)

internal val LightOutline = Color(0xFF767C86)
internal val LightOutlineVariant = Color(0xFFC8C5BE)

internal val LightInverseSurface = Color(0xFF2C3036)
internal val LightInverseOnSurface = Color(0xFFEEECE7)
internal val LightInversePrimary = Color(0xFFA9B1E8)

internal val LightPositive = Color(0xFF2E6A4C)

/**
 * Colours outside the Material 3 scheme. The attack mode ones ("dark room", BRD 18.3 B) are the same in every theme,
 * because attack mode stays dark (BRD D94); [Positive] follows the theme.
 */
object MigrelyColors {
    val AttackModeBackground = Color(0xFF08090C)
    val AttackModeSurface = Color(0xFF12151A)
    val AttackModeOutline = Color(0xFF22272F)
    val AttackModeText = Color(0xFF8A909A)
    val AttackModeSelectedContainer = Color(0xFF252A4C)
    val AttackModeSelectedOutline = Color(0xFF7F86BC)
    val AttackModeSelectedText = Color(0xFFC9CDEA)
    val AttackModeEndContainer = Color(0xFF1F2A34)
    val AttackModeEndText = Color(0xFFBCC8D3)

    /** Good news, such as fewer migraine days or a saved file. */
    val Positive: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalPositive.current
}
