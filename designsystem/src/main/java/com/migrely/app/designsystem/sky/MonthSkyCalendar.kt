package com.migrely.app.designsystem.sky

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.designsystem.theme.OnSurface
import com.migrely.app.designsystem.theme.OnSurfaceVariant
import com.migrely.app.designsystem.theme.Primary
import com.migrely.app.designsystem.theme.Surface

private const val DAYS_PER_WEEK = 7
private val CellSpacing = 6.dp
private val CellShape = RoundedCornerShape(10.dp)
private val ChosenRingWidth = 2.5.dp
private val FocusRingWidth = 2.5.dp

/** The colours drawn on a day cell: its rings and its number. */
private data class DayCellColors(
    val focusOuter: Color,
    val focusInner: Color,
    val chosen: Color,
    val today: Color,
    val number: Color
)

/** A sky is dark in every theme (BRD D94), so what is drawn on one takes the dark theme's colours. */
private val OnSkyColors = DayCellColors(
    focusOuter = OnSurface,
    focusInner = Surface,
    chosen = Primary,
    today = OnSurfaceVariant,
    number = OnSurface
)

@Composable
private fun dayCellColors(hasSky: Boolean) = if (hasSky) {
    OnSkyColors
} else {
    DayCellColors(
        focusOuter = MaterialTheme.colorScheme.onSurface,
        focusInner = MaterialTheme.colorScheme.surface,
        chosen = MaterialTheme.colorScheme.primary,
        today = MaterialTheme.colorScheme.onSurfaceVariant,
        number = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** What one calendar day shows (BRD EC-UX-09). Each differs by shape as well as colour (BRD EC-UX-15). */
enum class DaySkyAppearance {
    /** A past day with nothing logged: a dashed outline, never a clear sky (BRD G6). */
    NothingLogged,

    /**
     * A past day with attack medication but no migraine: logged, so a solid outline of the same
     * width as the dashed one, with no sky (BRD D100).
     */
    MedicationOnly,

    /** A day that has not come yet: a faint outline, not selectable. */
    Future,

    /** A day the user marked pain-free, with no attack: a clear, starry sky (BRD EC-UX-09, D49). */
    Clear,
    Hazy,
    Cloudy,
    Storm,

    /** An attack in progress, drawn still (BRD HIS-F1, EC-UX-07). */
    Rain
}

/**
 * One day of [MonthSkyCalendar]. [description] is what a screen reader says for it (BRD EC-UX-15). [isSelectable] is
 * false for days after today, even when an attack logged in a zone ahead gives such a day a sky. [hasPeriodMark] draws
 * the period drop in the bottom end corner (BRD D49).
 */
@Immutable
data class CalendarDay(
    val dayOfMonth: Int,
    val appearance: DaySkyAppearance,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isSelectable: Boolean,
    val description: String,
    val hasPeriodMark: Boolean = false
)

/** One entry of [MonthSkyLegend]; with [periodMark] it shows only the period drop. */
@Immutable
data class LegendEntry(
    val appearance: DaySkyAppearance,
    val label: String,
    val periodMark: Boolean = false
)

/**
 * A month of small skies (BRD 18.3 A, HIS-F1). [leadingEmptyDays] blank cells come before the first day so weeks start
 * on the locale's first day (BRD EC-TIM-05). Days that are not selectable have no action. The weekday initials are
 * hidden from screen readers, because every day says its own weekday. Screen readers hear [chosenStateDescription]
 * after the chosen day only: the `selected` property would make every day a "not checked" checkbox.
 *
 * With a keyboard the month is one Tab stop (BRD D38, EC-UX-19), as [MonthKeyboard] describes; Enter chooses the
 * focused day. Accessibility services still reach every day.
 */
@Composable
fun MonthSkyCalendar(
    weekdayInitials: List<String>,
    leadingEmptyDays: Int,
    days: List<CalendarDay>,
    chosenStateDescription: String,
    onDayClick: (dayOfMonth: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val requesters = remember(days.map(CalendarDay::dayOfMonth)) {
        days.associate { it.dayOfMonth to FocusRequester() }
    }
    val focusedDay = remember { FocusedDay() }
    val selectable = days.filter(CalendarDay::isSelectable)
    val anchor = selectable.firstOrNull(CalendarDay::isSelected)
        ?: selectable.firstOrNull(CalendarDay::isToday)
        ?: selectable.firstOrNull()
    val keyboard = MonthKeyboard(
        requesters = requesters,
        selectable = selectable.map(CalendarDay::dayOfMonth),
        anchor = anchor?.dayOfMonth,
        focusedDay = focusedDay,
        rightToLeft = LocalLayoutDirection.current == LayoutDirection.Rtl,
        focusManager = LocalFocusManager.current
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(CellSpacing),
        modifier = modifier
            .onPreviewKeyEvent(keyboard::onKey)
            .focusProperties { onEnter = { keyboard.onEnter(requestedFocusDirection) } }
            .focusGroup()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CellSpacing),
            modifier = Modifier.clearAndSetSemantics { }
        ) {
            weekdayInitials.forEach { initial ->
                Text(
                    text = initial,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        val cells: List<CalendarDay?> = List(leadingEmptyDays) { null } + days
        cells.chunked(DAYS_PER_WEEK).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(CellSpacing)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        DayCell(
                            day = day,
                            chosenStateDescription = chosenStateDescription,
                            focusRequester = requesters.getValue(day.dayOfMonth),
                            onFocused = { focusedDay.day = day.dayOfMonth },
                            onClick = { onDayClick(day.dayOfMonth) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                repeat(DAYS_PER_WEEK - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * The month's keyboard (BRD D38, EC-UX-19), made on each composition.
 * - Tab or Shift+Tab entering the grid lands on [anchor]: the chosen day, otherwise today, otherwise the first
 *   selectable day.
 * - Leaving, they go from the last or first selectable day straight past the grid, so the other days are skipped.
 * - The arrow keys go a day or a week on or back, with left and right mirrored in right-to-left layouts. Off the
 *   selectable days they do nothing, so focus stays in the month, as in a date picker.
 */
private class MonthKeyboard(
    private val requesters: Map<Int, FocusRequester>,
    private val selectable: List<Int>,
    private val anchor: Int?,
    private val focusedDay: FocusedDay,
    private val rightToLeft: Boolean,
    private val focusManager: FocusManager
) {
    fun onKey(event: KeyEvent): Boolean = event.type == KeyEventType.KeyDown &&
        if (event.key == Key.Tab) leave(backwards = event.isShiftPressed) else move(event.key)

    fun onEnter(direction: FocusDirection) {
        if (direction == FocusDirection.Next || direction == FocusDirection.Previous) {
            anchor?.let { requesters.getValue(it).requestFocus() }
        }
    }

    private fun leave(backwards: Boolean): Boolean {
        val edge = if (backwards) selectable.firstOrNull() else selectable.lastOrNull()
        edge?.let {
            requesters.getValue(it).requestFocus()
            focusManager.moveFocus(if (backwards) FocusDirection.Previous else FocusDirection.Next)
        }
        return edge != null
    }

    private fun move(key: Key): Boolean {
        val step = when (key) {
            Key.DirectionRight -> if (rightToLeft) -1 else 1
            Key.DirectionLeft -> if (rightToLeft) 1 else -1
            Key.DirectionDown -> DAYS_PER_WEEK
            Key.DirectionUp -> -DAYS_PER_WEEK
            else -> return false
        }
        focusedDay.day?.plus(step)
            ?.takeIf { it in selectable }
            ?.let { requesters.getValue(it).requestFocus() }
        return true
    }
}

/** The day with keyboard focus. It is read when a key is pressed, not at composition, so quick presses never lag. */
private class FocusedDay {
    var day: Int? = null
}

@Composable
private fun DayCell(
    day: CalendarDay,
    chosenStateDescription: String,
    focusRequester: FocusRequester,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectable = day.isSelectable
    var focused by remember { mutableStateOf(false) }
    val colors = dayCellColors(day.appearance.hasSky())
    val ring = when {
        // A two-tone ring shows on every sky (WCAG 2.4.7); inside it, the chosen day keeps its colour.
        focused ->
            Modifier
                .border(FocusRingWidth, colors.focusOuter, CellShape)
                .border(
                    FocusRingWidth * 2,
                    if (day.isSelected) colors.chosen else colors.focusInner,
                    CellShape
                )
        day.isSelected -> Modifier.border(ChosenRingWidth, colors.chosen, CellShape)
        day.isToday -> Modifier.border(1.5.dp, colors.today, CellShape)
        else -> Modifier
    }
    // Both outlines stand apart from the page at 3:1 or more on light (WCAG 1.4.11); the nothing-logged one is dashed.
    val outlines = DayOutlines(
        notLogged = MaterialTheme.colorScheme.outline,
        logged = MaterialTheme.colorScheme.outline
    )
    Box(
        // Merged into the clickable rather than replacing it, so a day stays focusable for keyboards and switch
        // access (BRD EC-UX-19); the day number inside is cleared so the description is read once.
        modifier = modifier
            .aspectRatio(1f)
            .clip(CellShape)
            .semantics {
                contentDescription = day.description
                if (day.isSelected) stateDescription = chosenStateDescription
            }
            .then(
                if (selectable) {
                    Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            focused = it.isFocused
                            if (it.isFocused) onFocused()
                        }
                        .clickable(
                            role = Role.Button,
                            onClick = onClick
                        )
                } else {
                    Modifier
                }
            )
            .then(ring)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawDaySky(day.appearance, outlines)
            if (day.hasPeriodMark) drawPeriodMark()
        }
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
            fontWeight = if (day.isToday) FontWeight.Bold else null,
            color = colors.number,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 5.dp, top = 3.dp)
                .clearAndSetSemantics { }
        )
    }
}

/** The legend under a calendar: one small sky and a label per entry, wrapping at large font sizes (BRD EC-UX-03). */
@Composable
fun MonthSkyLegend(entries: List<LegendEntry>, modifier: Modifier = Modifier) {
    // Both outlines stand apart from the page at 3:1 or more on light (WCAG 1.4.11); the nothing-logged one is dashed.
    val outlines = DayOutlines(
        notLogged = MaterialTheme.colorScheme.outline,
        logged = MaterialTheme.colorScheme.outline
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        entries.forEach { entry ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(
                    Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    if (entry.periodMark) {
                        drawPeriodMark(centered = true)
                    } else {
                        drawDaySky(entry.appearance, outlines)
                    }
                }
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }
    }
}

private fun DaySkyAppearance.hasSky() = this != DaySkyAppearance.NothingLogged &&
    this != DaySkyAppearance.MedicationOnly &&
    this != DaySkyAppearance.Future

/**
 * The outline colours of days without a sky: [notLogged] for the dashed and future outlines, and
 * the slightly brighter [logged] for a day with attack medication only (BRD D100).
 */
private class DayOutlines(val notLogged: Color, val logged: Color)

// The four-point stars of a pain-free day, as fractions of the cell: below the day number and clear of the period drop
// in the bottom end corner.
private val CLEAR_DAY_STARS = listOf(
    Triple(0.40f, 0.58f, 9f),
    Triple(0.68f, 0.44f, 6f),
    Triple(0.22f, 0.82f, 5f)
)

/** Draws a day's sky in a small square. Glyphs sit in the lower part, below the day number. */
private fun DrawScope.drawDaySky(appearance: DaySkyAppearance, outlines: DayOutlines) {
    val u = size.minDimension / 100f
    when (appearance) {
        // Outlines keep a minimum width and dash length, so they stay visible in the small legend squares too.
        DaySkyAppearance.NothingLogged -> drawRoundRect(
            color = outlines.notLogged,
            cornerRadius = CornerRadius(22f * u),
            style = Stroke(
                width = maxOf(2.5f * u, 1.dp.toPx()),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(maxOf(7f * u, 3.dp.toPx()), maxOf(5f * u, 2.dp.toPx()))
                )
            )
        )

        // The same width as the dashed outline, but solid: logged, with no migraine (BRD D100).
        DaySkyAppearance.MedicationOnly -> drawRoundRect(
            color = outlines.logged,
            cornerRadius = CornerRadius(22f * u),
            style = Stroke(width = maxOf(2.5f * u, 1.dp.toPx()))
        )

        DaySkyAppearance.Future -> drawRoundRect(
            color = outlines.notLogged.copy(alpha = 0.45f),
            cornerRadius = CornerRadius(22f * u),
            style = Stroke(width = maxOf(1.5f * u, 1.dp.toPx()))
        )

        DaySkyAppearance.Clear -> {
            drawRect(Brush.verticalGradient(SkyPalette.gradient(SkyAppearance.Clear)))
            // Mirrored in right-to-left layouts, like the day number and the period drop; a minimum size keeps the
            // stars visible in the small legend square too (BRD EC-UX-15).
            CLEAR_DAY_STARS.forEach { (x, y, r) ->
                val startX = if (layoutDirection == LayoutDirection.Rtl) 1f - x else x
                dayStar(
                    Offset(size.width * startX, size.height * y),
                    maxOf(r * u, 1.5.dp.toPx())
                )
            }
        }

        DaySkyAppearance.Hazy -> {
            drawRect(Brush.verticalGradient(SkyPalette.gradient(SkyAppearance.Hazy)))
            dayWave(y = 0.60f, from = 0.20f, to = 0.82f, color = SkyPalette.Wave)
            dayWave(y = 0.78f, from = 0.32f, to = 0.74f, color = SkyPalette.WaveFaint)
        }

        DaySkyAppearance.Cloudy -> {
            drawRect(Brush.verticalGradient(SkyPalette.gradient(SkyAppearance.Cloudy)))
            dayCloud(center = Offset(size.width * 0.55f, size.height * 0.62f), SkyPalette.Cloud)
        }

        DaySkyAppearance.Storm -> {
            drawRect(Brush.verticalGradient(SkyPalette.gradient(SkyAppearance.Storm)))
            dayCloud(
                center = Offset(size.width * 0.55f, size.height * 0.50f),
                SkyPalette.StormCloud
            )
            dayBolt(x = size.width * 0.58f, y = size.height * 0.64f)
        }

        DaySkyAppearance.Rain -> {
            drawRect(Brush.verticalGradient(SkyPalette.gradient(SkyAppearance.Rain)))
            dayCloud(
                center = Offset(size.width * 0.55f, size.height * 0.50f),
                SkyPalette.RainCloudFront
            )
            listOf(0.40f, 0.56f, 0.72f).forEach { x ->
                drawLine(
                    SkyPalette.Wave,
                    start = Offset(size.width * x, size.height * 0.74f),
                    end = Offset(size.width * x - 4f * u, size.height * 0.88f),
                    strokeWidth = 3.5f * u,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

private fun DrawScope.dayStar(center: Offset, radius: Float) {
    val waist = radius * 0.28f
    val star = Path().apply {
        moveTo(center.x, center.y - radius)
        lineTo(center.x + waist, center.y - waist)
        lineTo(center.x + radius, center.y)
        lineTo(center.x + waist, center.y + waist)
        lineTo(center.x, center.y + radius)
        lineTo(center.x - waist, center.y + waist)
        lineTo(center.x - radius, center.y)
        lineTo(center.x - waist, center.y - waist)
        close()
    }
    drawPath(star, SkyPalette.Moon)
}

/**
 * The period drop in the bottom end corner, or in the middle with [centered] (the legend): a rounded drop with a dark
 * edge, so it reads on every sky and by shape alone (BRD D49, EC-UX-15). It mirrors in right-to-left layouts.
 */
private fun DrawScope.drawPeriodMark(centered: Boolean = false) {
    val u = size.minDimension / 100f
    val radius = maxOf(9f * u, 3.dp.toPx())
    val center = when {
        centered -> Offset(size.width / 2f, size.height / 2f + radius * 0.35f)
        layoutDirection == LayoutDirection.Rtl -> Offset(radius * 2.2f, size.height - radius * 2f)
        else -> Offset(size.width - radius * 2.2f, size.height - radius * 2f)
    }
    val drop = Path().apply {
        moveTo(center.x, center.y - radius * 1.7f)
        quadraticTo(center.x + radius, center.y - radius * 0.4f, center.x + radius, center.y)
        quadraticTo(center.x + radius, center.y + radius, center.x, center.y + radius)
        quadraticTo(center.x - radius, center.y + radius, center.x - radius, center.y)
        quadraticTo(center.x - radius, center.y - radius * 0.4f, center.x, center.y - radius * 1.7f)
        close()
    }
    // The edge goes over the fill, so its whole width shows.
    drawPath(drop, SkyPalette.PeriodMark)
    drawPath(drop, SkyPalette.Outline, style = Stroke(width = maxOf(1.5f * u, 1.dp.toPx())))
}

private fun DrawScope.dayCloud(center: Offset, color: Color) {
    // drawCloud scales with the area it is given; 95% of the cell keeps the cloud clear of the day number.
    drawCloud(area = Size(size.width * 0.95f, size.height * 0.95f), center = center, color = color)
}

private fun DrawScope.dayWave(y: Float, from: Float, to: Float, color: Color) {
    val u = size.minDimension / 100f
    val top = size.height * y
    val start = size.width * from
    val end = size.width * to
    val step = (end - start) / 3f
    val amplitude = 5f * u
    val path = Path().apply {
        moveTo(start, top)
        quadraticTo(start + step * 0.5f, top - amplitude, start + step, top)
        quadraticTo(start + step * 1.5f, top + amplitude, start + step * 2f, top)
        quadraticTo(start + step * 2.5f, top - amplitude, end, top)
    }
    drawPath(path, color, style = Stroke(width = 5f * u, cap = StrokeCap.Round))
}

private fun DrawScope.dayBolt(x: Float, y: Float) {
    val u = size.minDimension / 100f
    val bolt = Path().apply {
        moveTo(x, y)
        lineTo(x - 7f * u, y + 11f * u)
        lineTo(x, y + 11f * u)
        lineTo(x - 5f * u, y + 21f * u)
    }
    drawPath(
        bolt,
        SkyPalette.Lightning,
        style = Stroke(width = 4f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

@Preview(name = "Month sky calendar", widthDp = 360)
@Composable
private fun MonthSkyCalendarPreview() {
    val skies = listOf(
        DaySkyAppearance.NothingLogged,
        DaySkyAppearance.MedicationOnly,
        DaySkyAppearance.Clear,
        DaySkyAppearance.Hazy,
        DaySkyAppearance.Cloudy,
        DaySkyAppearance.Storm,
        DaySkyAppearance.NothingLogged,
        DaySkyAppearance.Rain
    )
    MigrelyTheme {
        Column(Modifier.padding(16.dp)) {
            MonthSkyCalendar(
                weekdayInitials = listOf("M", "T", "W", "T", "F", "S", "S"),
                leadingEmptyDays = 3,
                days = (1..31).map { day ->
                    CalendarDay(
                        dayOfMonth = day,
                        appearance = if (day > 23) {
                            DaySkyAppearance.Future
                        } else {
                            skies[day % skies.size]
                        },
                        isToday = day == 23,
                        isSelected = day == 14,
                        isSelectable = day <= 23,
                        description = "$day October",
                        hasPeriodMark = day in 12..16
                    )
                },
                chosenStateDescription = "Chosen",
                onDayClick = {},
                modifier = Modifier.fillMaxWidth()
            )
            MonthSkyLegend(
                entries = listOf(
                    LegendEntry(DaySkyAppearance.Clear, "Pain-free"),
                    LegendEntry(DaySkyAppearance.Hazy, "Mild"),
                    LegendEntry(DaySkyAppearance.Cloudy, "Moderate"),
                    LegendEntry(DaySkyAppearance.Storm, "Severe"),
                    LegendEntry(DaySkyAppearance.MedicationOnly, "Attack medication only"),
                    LegendEntry(DaySkyAppearance.NothingLogged, "Not logged"),
                    LegendEntry(DaySkyAppearance.NothingLogged, "Period", periodMark = true)
                ),
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
