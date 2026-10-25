package com.migrely.app.datasource.report.layout

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportDayLevelDataModel
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import com.migrely.app.datasource.report.model.ReportHeatMonthDataModel
import com.migrely.app.datasource.report.model.ReportLegendDataModel
import java.util.IdentityHashMap
import java.util.Locale

private const val MARGIN = 40f
private const val FOOTER_HEIGHT = 28f
private const val GAP = 8f
private const val CELL_PADDING = 3f
private const val HEADING_KEEP = 72f
private const val MONTHS_PER_ROW = 3
private const val DOT_X = 0.78f
private const val DOT_Y = 0.78f
private const val DOT_RADIUS = 0.1f
private const val WEEK_DAYS = 7
private const val SWATCH = 9f
private const val LINE_SPACING = 1.1f

/** A cell or paragraph longer than this is cut with an ellipsis, so no row is ever taller than a page. */
private const val MAXIMUM_LINES = 40

/**
 * Lays a report out on pages and draws it with Android's `PdfDocument` (ADR-07). Pages are laid out first, so every
 * footer can say how many there are. Shades go from white to near black, so the calendar reads in black and white.
 */
internal class ReportPageLayout(private val document: ReportDocumentDataModel) {
    private val width = document.paper.width.toFloat()
    private val height = document.paper.height.toFloat()
    private val contentWidth = width - 2 * MARGIN
    private val bottom = height - MARGIN - FOOTER_HEIGHT

    private val titlePaint = paint(18f, bold = true)
    private val headingPaint = paint(13f, bold = true)
    private val bodyPaint = paint(10f)
    private val cellPaint = paint(8.5f)
    private val headerCellPaint = paint(8.5f, bold = true)
    private val smallPaint = paint(7.5f, color = Color.DKGRAY)
    private val dayPaint = paint(6f)
    private val linePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
    }

    /** Each block's and each table row's piece, measured once however often the pages are counted (review I2). */
    private val pieces = IdentityHashMap<Any, Piece>()

    /** How many pages [blocks] take. */
    fun pageCount(blocks: List<ReportBlockDataModel>): Int = paginate(items(blocks)).size

    /** Draws [blocks]; past [maxPages] the rest is cut off, so the limit always holds (review I1). */
    fun render(blocks: List<ReportBlockDataModel>, maxPages: Int): PdfDocument {
        val pages = paginate(items(blocks)).take(maxPages)
        val pdf = PdfDocument()
        try {
            draw(pdf, pages)
        } catch (failure: RuntimeException) {
            pdf.close()
            throw failure
        }
        return pdf
    }

    private fun draw(pdf: PdfDocument, pages: List<List<Pair<Float, Piece>>>) {
        pages.forEachIndexed { index, placed ->
            val page = pdf.startPage(
                PdfDocument.PageInfo.Builder(
                    width.toInt(),
                    height.toInt(),
                    index + 1
                ).create()
            )
            placed.forEach { (top, piece) -> piece.draw(page.canvas, top) }
            drawFooter(page.canvas, index + 1, pages.size)
            pdf.finishPage(page)
        }
    }

    /** Something drawn at a top position. [header] is drawn first whenever it starts a page. */
    private class Piece(
        val height: Float,
        val keepWithNext: Boolean = false,
        val draw: (Canvas, Float) -> Unit
    )

    private sealed interface Item {
        class Draw(val piece: Piece, val header: Piece? = null) : Item

        data object Break : Item
    }

    private fun paginate(items: List<Item>): List<List<Pair<Float, Piece>>> {
        val pages = mutableListOf<List<Pair<Float, Piece>>>()
        var current = mutableListOf<Pair<Float, Piece>>()
        var y = MARGIN
        fun newPage() {
            if (current.isNotEmpty()) pages += current
            current = mutableListOf()
            y = MARGIN
        }
        items.forEach { item ->
            when (item) {
                Item.Break -> newPage()
                is Item.Draw -> {
                    val needed =
                        item.piece.height + if (item.piece.keepWithNext) HEADING_KEEP else 0f
                    if (y + needed > bottom && current.isNotEmpty()) {
                        newPage()
                        item.header?.let {
                            current += y to it
                            y += it.height
                        }
                    }
                    current += y to item.piece
                    y += item.piece.height + GAP / 2
                }
            }
        }
        if (current.isNotEmpty() || pages.isEmpty()) pages += current
        return pages
    }

    private fun items(blocks: List<ReportBlockDataModel>): List<Item> = blocks.flatMap { block ->
        when (block) {
            is ReportBlockDataModel.Title -> listOf(Item.Draw(text(block.text, titlePaint, GAP)))
            is ReportBlockDataModel.Heading ->
                listOf(Item.Draw(text(block.text, headingPaint, GAP, keepWithNext = true)))
            is ReportBlockDataModel.Paragraph -> listOf(
                Item.Draw(
                    text(
                        block.text,
                        bodyPaint,
                        GAP / 2
                    )
                )
            )
            is ReportBlockDataModel.Facts -> block.facts.map { fact ->
                Item.Draw(
                    row(listOf(fact.label, fact.value), listOf(2f, 1f), bodyPaint, ruled = true)
                )
            }
            is ReportBlockDataModel.Table -> {
                val weights = block.columns.map { it.weight }
                val header =
                    row(block.columns.map { it.title }, weights, headerCellPaint, ruled = true)
                listOf(Item.Draw(header)) + block.rows.map {
                    Item.Draw(row(it, weights, cellPaint, ruled = true), header)
                }
            }
            is ReportBlockDataModel.HeatMap ->
                block.months.chunked(MONTHS_PER_ROW).map {
                    Item.Draw(cached(it.first()) { monthRow(it, block.weekdays) })
                } +
                    Item.Draw(cached(block.legend) { legend(block.legend) })
            ReportBlockDataModel.PageBreak -> listOf(Item.Break)
        }
    }

    private fun cached(source: Any, make: () -> Piece): Piece = pieces.getOrPut(source, make)

    private fun text(
        text: String,
        paint: TextPaint,
        after: Float,
        keepWithNext: Boolean = false
    ): Piece {
        val layout = layout(text, paint, contentWidth)
        return Piece(layout.height + after, keepWithNext) { canvas, top ->
            draw(canvas, layout, MARGIN, top)
        }
    }

    /** A row of cells sharing the content width by [weights]; long text wraps within its cell. */
    private fun row(
        cells: List<String>,
        weights: List<Float>,
        paint: TextPaint,
        ruled: Boolean
    ): Piece {
        val total = weights.sum()
        val widths = weights.map { contentWidth * it / total }
        val layouts = cells.mapIndexed { index, cell ->
            layout(
                cell,
                paint,
                widths[index] - 2 * CELL_PADDING
            )
        }
        val rowHeight = (layouts.maxOfOrNull { it.height } ?: 0) + 2 * CELL_PADDING
        return Piece(rowHeight) { canvas, top ->
            var x = MARGIN
            layouts.forEachIndexed { index, layout ->
                draw(canvas, layout, x + CELL_PADDING, top + CELL_PADDING)
                x += widths[index]
            }
            if (ruled) {
                canvas.drawLine(
                    MARGIN,
                    top + rowHeight,
                    MARGIN + contentWidth,
                    top + rowHeight,
                    linePaint
                )
            }
        }
    }

    private fun monthRow(months: List<ReportHeatMonthDataModel>, weekdays: List<String>): Piece {
        val boxWidth = (contentWidth - (MONTHS_PER_ROW - 1) * GAP * 2) / MONTHS_PER_ROW
        val cell = boxWidth / WEEK_DAYS
        val titleHeight = layout("M", headingPaint, boxWidth).height.toFloat()
        // The weekday initials, then only the weeks the row draws.
        val boxHeight = titleHeight + (weekRowsOf(months) + 1) * cell + GAP
        return Piece(boxHeight) { canvas, top ->
            months.forEachIndexed { index, month ->
                val left = MARGIN + index * (boxWidth + GAP * 2)
                draw(canvas, layout(month.title, bodyPaint, boxWidth), left, top)
                val gridTop = top + titleHeight
                weekdays.forEachIndexed { day, letter ->
                    draw(
                        canvas,
                        layout(letter, dayPaint, cell),
                        left + day * cell + cell / 3,
                        gridTop + cell / 3
                    )
                }
                month.levels.forEachIndexed { dayIndex, level ->
                    val slot = month.leadingEmptyDays + dayIndex
                    val x = left + (slot % WEEK_DAYS) * cell
                    val y = gridTop + (slot / WEEK_DAYS + 1) * cell
                    drawDay(canvas, x, y, cell, level, (dayIndex + 1).toString())
                }
            }
        }
    }

    private fun legend(legend: List<ReportLegendDataModel>): Piece {
        val itemWidth = contentWidth / legend.size.coerceAtLeast(1)
        val layouts = legend.map { layout(it.label, smallPaint, itemWidth - SWATCH - GAP) }
        val legendHeight = maxOf(SWATCH, layouts.maxOfOrNull { it.height.toFloat() } ?: 0f) + GAP
        return Piece(legendHeight) { canvas, top ->
            legend.forEachIndexed { index, item ->
                val left = MARGIN + index * itemWidth
                drawDay(canvas, left, top, SWATCH, item.level, null)
                draw(canvas, layouts[index], left + SWATCH + GAP / 2, top)
            }
        }
    }

    private fun drawDay(
        canvas: Canvas,
        x: Float,
        y: Float,
        size: Float,
        level: ReportDayLevelDataModel,
        label: String?
    ) {
        if (level == ReportDayLevelDataModel.Outside) return
        val inset = 0.6f
        val fill = Paint().apply {
            style = Paint.Style.FILL
            color = shade(level)
        }
        canvas.drawRect(x + inset, y + inset, x + size - inset, y + size - inset, fill)
        canvas.drawRect(x + inset, y + inset, x + size - inset, y + size - inset, linePaint)
        if (label != null) {
            val number = TextPaint(dayPaint).apply {
                color =
                    if (level.dark()) Color.WHITE else Color.DKGRAY
            }
            draw(canvas, layout(label, number, size), x + inset * 2, y + inset * 2)
        }
    }

    private fun drawFooter(canvas: Canvas, page: Int, pages: Int) {
        val top = height - MARGIN - FOOTER_HEIGHT / 2
        canvas.drawLine(MARGIN, top - GAP / 2, MARGIN + contentWidth, top - GAP / 2, linePaint)
        draw(canvas, layout(document.footer, smallPaint, contentWidth * 2 / 3), MARGIN, top)
        val label =
            layout(
                String.format(Locale.ENGLISH, document.pageLabel, page, pages),
                smallPaint,
                contentWidth / 3
            )
        draw(canvas, label, MARGIN + contentWidth - label.getLineWidth(0), top)
    }

    private fun shade(level: ReportDayLevelDataModel) = when (level) {
        ReportDayLevelDataModel.Outside,
        ReportDayLevelDataModel.Nothing -> Color.WHITE
        ReportDayLevelDataModel.Clear -> Color.rgb(0xE6, 0xF0, 0xF8)
        ReportDayLevelDataModel.Mild -> Color.rgb(0xC8, 0xCC, 0xD2)
        ReportDayLevelDataModel.Moderate -> Color.rgb(0x86, 0x8D, 0x98)
        ReportDayLevelDataModel.Severe -> Color.rgb(0x2E, 0x33, 0x3B)
    }

    private fun ReportDayLevelDataModel.dark() =
        this == ReportDayLevelDataModel.Moderate || this == ReportDayLevelDataModel.Severe

    private fun layout(text: String, paint: TextPaint, width: Float): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setLineSpacing(0f, LINE_SPACING)
            .setIncludePad(false)
            .setMaxLines(MAXIMUM_LINES)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

    private fun draw(canvas: Canvas, layout: StaticLayout, x: Float, y: Float) {
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun paint(size: Float, bold: Boolean = false, color: Int = Color.BLACK) =
        reportTextPaint(size, bold, color)
}

/**
 * Text for a PDF page, whose unit is a point. Hinted metrics would round each glyph's advance to a whole point: at
 * 10 pt a space, 2.48 pt in Roboto, became 2 while "m", "n" and "h" rounded up, so words ran together ("a month",
 * "at the"). Linear metrics, subpixel positions and no hinting keep the font's own advances (store polish).
 */
internal fun reportTextPaint(size: Float, bold: Boolean, color: Int): TextPaint =
    TextPaint().apply {
        isAntiAlias = true
        isLinearText = true
        isSubpixelText = true
        hinting = Paint.HINTING_OFF
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
