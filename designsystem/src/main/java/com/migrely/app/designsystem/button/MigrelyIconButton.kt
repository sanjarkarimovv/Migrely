package com.migrely.app.designsystem.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.layout.InsetInteractionSource

// Material's minimum interactive size and its standard icon button's drawn circle.
private val TouchSize = 48.dp
private val CircleSize = 40.dp

/**
 * Material's standard icon button, with a touch area of its own 48 dp (#255, lead decision 2026-11-19, option 1).
 * Material draws a 40 dp circle and makes the ring around it touchable only through the view configuration's minimum
 * touch target, which a page's exact bounds set to zero (`PageScroll`): a tap in that ring then did nothing. Here the
 * 48 dp node takes the click and the 40 dp circle draws the container, the icon and the ripple as Material does. Use it
 * for every icon button inside a page; a top bar's keeps Material's.
 */
@Composable
fun MigrelyIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = IconButtonDefaults.iconButtonColors()
    val interactions = remember { MutableInteractionSource() }
    val inset = with(LocalDensity.current) { ((TouchSize - CircleSize) / 2).toPx() }
    val circleInteractions = remember(interactions, inset) {
        InsetInteractionSource(interactions, Offset(inset, inset))
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(TouchSize)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactions,
                indication = null
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(CircleSize)
                .clip(CircleShape)
                .background(
                    color = if (enabled) colors.containerColor else colors.disabledContainerColor,
                    shape = CircleShape
                )
                .indication(circleInteractions, ripple())
        ) {
            val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor
            CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
        }
    }
}
