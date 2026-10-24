package com.migrely.app.datasource.report.model

/**
 * A report to render (BRD REP-F2, REP-F3, D82). It is plain text and levels: what it says is decided before, and the
 * renderer only lays it out. [pageLabel] takes the page number and the page count, as in "Page %1$d of %2$d".
 */
data class ReportDocumentDataModel(
    val fileName: String,
    val paper: ReportPaperDataModel,
    val footer: String,
    val pageLabel: String,
    val maxPages: Int,
    val blocks: List<ReportBlockDataModel>
)

/** Page sizes in PostScript points. */
enum class ReportPaperDataModel(val width: Int, val height: Int) {
    A4(595, 842),
    Letter(612, 792)
}

sealed interface ReportBlockDataModel {
    data class Title(val text: String) : ReportBlockDataModel

    data class Heading(val text: String) : ReportBlockDataModel

    data class Paragraph(val text: String) : ReportBlockDataModel

    data class Facts(val facts: List<ReportFactDataModel>) : ReportBlockDataModel

    /**
     * A table whose header repeats on every page. With a [shortenedNote], taking the shown and total row counts as in
     * "Showing the latest %1$d of %2$d", rows may be dropped from its end so the report fits in its pages (EC-REP-02).
     */
    data class Table(
        val columns: List<ReportColumnDataModel>,
        val rows: List<List<String>>,
        val shortenedNote: String? = null
    ) : ReportBlockDataModel

    data class HeatMap(
        val months: List<ReportHeatMonthDataModel>,
        val legend: List<ReportLegendDataModel>
    ) : ReportBlockDataModel

    data object PageBreak : ReportBlockDataModel
}

data class ReportFactDataModel(val label: String, val value: String)

data class ReportColumnDataModel(val title: String, val weight: Float)

/** One month of the heat map. [firstWeekday] is 0 for Monday; [levels] holds one level per day of the month. */
data class ReportHeatMonthDataModel(
    val title: String,
    val firstWeekday: Int,
    val levels: List<ReportDayLevelDataModel>
)

/** A day's shade, light to dark so it reads in black and white too. [Outside] days are not in the period. */
enum class ReportDayLevelDataModel {
    Outside,
    Nothing,
    Clear,
    Mild,
    Moderate,
    Severe
}

data class ReportLegendDataModel(val level: ReportDayLevelDataModel, val label: String)
