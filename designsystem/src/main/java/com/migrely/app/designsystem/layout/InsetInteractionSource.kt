package com.migrely.app.designsystem.layout

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A touch area's interactions as a smaller drawn part inside it sees them: each press moved by [inset], the drawn
 * part's offset in the touch area, so its ripple starts under the finger. A release or cancel names the moved press.
 * For a control whose touch area is larger than what it draws (#255, lead decision 2026-11-19, option 1).
 */
internal class InsetInteractionSource(
    private val source: InteractionSource,
    private val inset: Offset
) : InteractionSource {
    override val interactions: Flow<Interaction> = flow {
        val moved = mutableMapOf<PressInteraction.Press, PressInteraction.Press>()
        source.interactions.collect { interaction ->
            emit(
                when (interaction) {
                    is PressInteraction.Press ->
                        PressInteraction.Press(interaction.pressPosition - inset)
                            .also { moved[interaction] = it }
                    is PressInteraction.Release ->
                        PressInteraction.Release(
                            moved.remove(interaction.press) ?: interaction.press
                        )
                    is PressInteraction.Cancel ->
                        PressInteraction.Cancel(
                            moved.remove(interaction.press) ?: interaction.press
                        )
                    else -> interaction
                }
            )
        }
    }
}
