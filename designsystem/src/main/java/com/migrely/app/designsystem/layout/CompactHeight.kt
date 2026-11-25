package com.migrely.app.designsystem.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

private const val COMPACT_HEIGHT_DP = 480

/** True in a short window (a phone in landscape), where layouts put text and controls side by side. */
@Composable
fun isCompactHeight(): Boolean = LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP
