package com.migrely.app.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Icons drawn in code from path data (BRD D19); the shapes follow Material Symbols (Apache 2.0) on a 24-unit grid. */
object MigrelyIcons {
    private val MIRRORED = setOf("Back", "ChevronStart", "ChevronEnd")

    val Back: ImageVector =
        icon("Back", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z")

    val Close: ImageVector = icon(
        "Close",
        "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"
    )

    val Add: ImageVector = icon("Add", "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")

    val Notifications: ImageVector = icon(
        "Notifications",
        "M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.89,2 2,2zM18,16v-5c0,-3.07 -1.64,-5.64 " +
            "-4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68" +
            "C7.63,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z"
    )

    val Check: ImageVector = icon("Check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z")

    /** The letter i in a circle, for calm information such as the medication cards (BRD 18.4). */
    val Information: ImageVector = icon(
        "Information",
        "M11,7h2v2h-2zM11,11h2v6h-2z" +
            "M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2z" +
            "M12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8z"
    )

    val Warning: ImageVector =
        icon("Warning", "M1,21h22L12,2 1,21zM13,18h-2v-2h2v2zM13,14h-2v-4h2v4z")

    val ChevronStart: ImageVector =
        icon("ChevronStart", "M15.41,7.41L14,6l-6,6 6,6 1.41,-1.41L10.83,12z")

    val ChevronEnd: ImageVector =
        icon("ChevronEnd", "M10,6L8.59,7.41 13.17,12l-4.58,4.59L10,18l6,-6z")

    private fun icon(name: String, pathData: String): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
        // Direction icons point the other way in right-to-left layouts (BRD EC-UX-05).
        autoMirror = name in MIRRORED
    ).addPath(
        pathData = PathParser().parsePathString(pathData).toNodes(),
        fill = SolidColor(Color.Black)
    ).build()
}
