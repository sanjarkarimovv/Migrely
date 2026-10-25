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

    /** A page with lines, for the doctor report (BRD D81). */
    val Report: ImageVector = icon(
        "Report",
        "M14,2H6c-1.1,0 -1.99,0.9 -1.99,2L4,20c0,1.1 0.89,2 1.99,2H18c1.1,0 2,-0.9 2,-2V8l-6,-6z" +
            "M16,18H8v-2h8v2zM16,14H8v-2h8v2zM13,9V3.5L18.5,9H13z"
    )

    /** A line chart with sparkles, Material's "insights", for Insights (BRD D87). */
    val Insights: ImageVector = icon(
        "Insights",
        "M21,8c-1.45,0 -2.26,1.44 -1.93,2.51l-3.55,3.56c-0.3,-0.09 -0.74,-0.09 -1.04,0" +
            "l-2.55,-2.55C12.27,10.45 11.46,9 10,9c-1.45,0 -2.27,1.44 -1.93,2.52" +
            "l-4.56,4.55C2.44,15.74 1,16.55 1,18c0,1.1 0.9,2 2,2c1.45,0 2.26,-1.44 1.93,-2.51" +
            "l4.55,-4.56c0.3,0.09 0.74,0.09 1.04,0l2.55,2.55C12.73,16.55 13.54,18 15,18" +
            "c1.45,0 2.27,-1.44 1.93,-2.52l3.56,-3.55c1.07,0.33 2.51,-0.48 2.51,-1.93" +
            "c0,-1.1 -0.9,-2 -2,-2z" +
            "M15,9l0.94,-2.07L18,6l-2.06,-0.93L15,3l-0.92,2.07L12,6l2.08,0.93z" +
            "M3.5,11L4,9l2,-0.5L4,8l-0.5,-2L3,8l-2,0.5L3,9z"
    )

    /** A gear, for Settings (BRD D70). */
    val Settings: ImageVector = icon(
        "Settings",
        "M19.14,12.94c0.04,-0.3 0.06,-0.61 0.06,-0.94c0,-0.32 -0.02,-0.64 -0.07,-0.94" +
            "l2.03,-1.58c0.18,-0.14 0.23,-0.41 0.12,-0.61l-1.92,-3.32" +
            "c-0.12,-0.22 -0.37,-0.29 -0.59,-0.22l-2.39,0.96" +
            "c-0.5,-0.38 -1.03,-0.7 -1.62,-0.94L14.4,2.81" +
            "c-0.04,-0.24 -0.24,-0.41 -0.48,-0.41h-3.84c-0.24,0 -0.43,0.17 -0.47,0.41" +
            "L9.25,5.35C8.66,5.59 8.12,5.92 7.63,6.29L5.24,5.33" +
            "c-0.22,-0.08 -0.47,0 -0.59,0.22L2.74,8.87C2.62,9.08 2.66,9.34 2.86,9.48" +
            "l2.03,1.58C4.84,11.36 4.8,11.69 4.8,12s0.02,0.64 0.07,0.94l-2.03,1.58" +
            "c-0.18,0.14 -0.23,0.41 -0.12,0.61l1.92,3.32c0.12,0.22 0.37,0.29 0.59,0.22" +
            "l2.39,-0.96c0.5,0.38 1.03,0.7 1.62,0.94l0.36,2.54" +
            "c0.05,0.24 0.24,0.41 0.48,0.41h3.84c0.24,0 0.44,-0.17 0.47,-0.41l0.36,-2.54" +
            "c0.59,-0.24 1.13,-0.56 1.62,-0.94l2.39,0.96c0.22,0.08 0.47,0 0.59,-0.22" +
            "l1.92,-3.32c0.12,-0.22 0.07,-0.47 -0.12,-0.61L19.14,12.94zM12,15.6" +
            "c-1.98,0 -3.6,-1.62 -3.6,-3.6s1.62,-3.6 3.6,-3.6s3.6,1.62 3.6,3.6" +
            "S13.98,15.6 12,15.6z"
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
