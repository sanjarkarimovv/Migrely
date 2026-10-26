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
     * A table that [follows] another is shortened only with it (D84).
     */
    data class Table(
        val columns: List<ReportColumnDataModel>,
        val rows: List<List<String>>,
        val shortenedNote: String? = null,
        val follows: ReportFollowedTableDataModel? = null
    ) : ReportBlockDataModel

    /** A calendar per month and one [legend]; [weekdays] heads the seven columns, from the week's first day. */
    data class HeatMap(
        val weekdays: List<String>,
        val months: List<ReportHeatMonthDataModel>,
        val legend: List<ReportLegendDataModel>
    ) : ReportBlockDataModel

    data object PageBreak : ReportBlockDataModel
}

/**
 * The table whose rows a table's rows belong to, as notes belong to the migraines listed (BRD D84). [owners] holds, for
 * each row of the following table, the index of its row in [table], which must be the very table in the blocks. When
 * [table] keeps only its first N rows, the rows whose owner is among them stay, after [shortenedNote], which takes N as
 * in "Notes from the latest %1$d migraines."
 */
data class ReportFollowedTableDataModel(
    val table: ReportBlockDataModel.Table,
    val owners: List<Int>,
    val shortenedNote: String
)

data class ReportFactDataModel(val label: String, val value: String)

data class ReportColumnDataModel(val title: String, val weight: Float)

/**
 * One month of the heat map. [leadingEmptyDays] are the columns before its 1st, from 0 to 6; [levels] holds one level
 * per day of the month.
 */
data class ReportHeatMonthDataModel(
    val title: String,
    val leadingEmptyDays: Int,
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
