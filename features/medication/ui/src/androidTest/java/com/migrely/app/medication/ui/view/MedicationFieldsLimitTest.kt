package com.migrely.app.medication.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.mapper.MedicationTimeUiFormatter
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.MedicationLogUiModel
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val NAME_CAP = 60
private const val DOSE_CAP = 20
private const val THUMBS_UP = "👍"

/** The medication name and dose keep their end, cursor and whole characters at the cap (#91). */
@RunWith(AndroidJUnit4::class)
class MedicationFieldsLimitTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var formChanges = 0
    private var form: MedicationFormUiModel? = null
    private var addedName: String? = null

    private val editName get() = composeTestRule.onAllNodes(hasSetTextAction())[0]
    private val editDose get() = composeTestRule.onAllNodes(hasSetTextAction())[1]
    private val logName get() = composeTestRule.onAllNodes(hasSetTextAction())[0]

    @Composable
    private fun EditForm(initial: MedicationFormUiModel = MedicationFormUiModel()) = MigrelyTheme {
        var state by rememberSaveable {
            mutableStateOf(initial.copy(kind = MedicationKindUiModel.Preventive))
        }
        form = state
        EditMedicationContents(
            adding = true,
            notFound = false,
            form = state,
            problem = null,
            pending = false,
            onFormChange = {
                formChanges++
                state = it
            },
            onSave = {},
            onBack = {},
            messages = {}
        )
    }

    @Composable
    private fun LogForm() = MigrelyTheme {
        LogMedicationContents(
            log = MedicationLogUiModel(
                defaultTimeEpochMillis = 1_792_829_200_000L,
                zoneId = "Asia/Tashkent",
                attackId = "a",
                medications = emptyList()
            ),
            shownTime = 1_792_829_200_000L,
            logged = null,
            problem = null,
            pending = false,
            darkRoom = false,
            timeUiFormatter = MedicationTimeUiFormatter(
                locale = { Locale.US },
                is24HourFormat = { true },
                deviceZone = { ZoneId.of("Asia/Tashkent") },
                bestDateTimePattern = { _, pattern -> pattern }
            ),
            actions = LogMedicationActions(
                onBack = {},
                onTimePicked = {},
                onMedication = {},
                onAddAndLog = { name, _ -> addedName = name },
                onUndo = {},
                onDone = {},
                onManage = {}
            ),
            messages = {}
        )
    }

    @Test
    fun editNameKeepsEndAndCursorWhenFull() {
        composeTestRule.setContent { EditForm() }
        assertFullFieldKeepsEndAndCursor({ editName }, NAME_CAP)
        assertEquals(editName.text(), form?.name)
    }

    @Test
    fun editNameNeverKeepsHalfAnEmoji() {
        composeTestRule.setContent { EditForm() }
        assertEmojiAcrossTheCapIsRefused({ editName }, NAME_CAP)
        assertEquals(editName.text(), form?.name)
    }

    @Test
    fun editNameCutsAPasteAfterAWholeCharacter() {
        composeTestRule.setContent { EditForm() }
        assertPasteIsCutAfterAWholeCharacter({ editName }, NAME_CAP)
        assertEquals(editName.text(), form?.name)
    }

    @Test
    fun editDoseKeepsEndAndCursorWhenFull() {
        composeTestRule.setContent { EditForm() }
        assertFullFieldKeepsEndAndCursor({ editDose }, DOSE_CAP)
        assertEquals(editDose.text(), form?.dose)
    }

    @Test
    fun editDoseNeverKeepsHalfAnEmoji() {
        composeTestRule.setContent { EditForm() }
        assertEmojiAcrossTheCapIsRefused({ editDose }, DOSE_CAP)
        assertEquals(editDose.text(), form?.dose)
    }

    @Test
    fun editDoseCutsAPasteAfterAWholeCharacter() {
        composeTestRule.setContent { EditForm() }
        assertPasteIsCutAfterAWholeCharacter({ editDose }, DOSE_CAP)
        assertEquals(editDose.text(), form?.dose)
    }

    @Test
    fun editFormOpensWithTheStoredNameAndDose() {
        composeTestRule.setContent {
            EditForm(MedicationFormUiModel(name = "Ibuprofen", dose = "400 mg"))
        }

        assertEquals("Ibuprofen", editName.text())
        assertEquals("400 mg", editDose.text())
    }

    @Test
    fun movingTheCursorOrARefusedEditLeavesTheFormAlone() {
        // onFormChange clears a shown save problem, so only a real change of the text may call it.
        composeTestRule.setContent { EditForm() }
        val full = "a".repeat(NAME_CAP)
        editName.performTextInput(full)
        val changesWhenFull = formChanges

        editName.performTextInputSelection(TextRange(3))
        editName.performTextInput("Q")

        assertEquals(changesWhenFull, formChanges)
        assertEquals(full, form?.name)
    }

    @Test
    fun recreatingTheEditFormRestoresTextAndCursorOfBothFields() {
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent { EditForm() }
        editName.performTextInput("Ibuprofen")
        editName.performTextInputSelection(TextRange(3))
        editDose.performTextInput("400 mg")
        editDose.performTextInputSelection(TextRange(2))

        restoration.emulateSavedInstanceStateRestore()

        assertEquals("Ibuprofen", editName.text())
        assertEquals(TextRange(3), editName.selection())
        assertEquals("400 mg", editDose.text())
        assertEquals(TextRange(2), editDose.selection())
    }

    @Test
    fun logNameKeepsEndAndCursorWhenFull() {
        composeTestRule.setContent { LogForm() }
        assertFullFieldKeepsEndAndCursor({ logName }, NAME_CAP)
    }

    @Test
    fun logNameNeverKeepsHalfAnEmoji() {
        composeTestRule.setContent { LogForm() }
        assertEmojiAcrossTheCapIsRefused({ logName }, NAME_CAP)
    }

    @Test
    fun logNameCutsAPasteAfterAWholeCharacter() {
        composeTestRule.setContent { LogForm() }
        assertPasteIsCutAfterAWholeCharacter({ logName }, NAME_CAP)
    }

    @Test
    fun recreatingTheLogFormRestoresTextAndCursor() {
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent { LogForm() }
        logName.performTextInput("Ibuprofen")
        logName.performTextInputSelection(TextRange(3))

        restoration.emulateSavedInstanceStateRestore()

        assertEquals("Ibuprofen", logName.text())
        assertEquals(TextRange(3), logName.selection())
    }

    @Test
    fun addAndLogPassesTheTypedName() {
        composeTestRule.setContent { LogForm() }
        logName.performTextInput("Ibuprofen")

        composeTestRule.onNodeWithText("Add and log", substring = true, ignoreCase = true)
            .performScrollTo()
            .performClick()

        assertEquals("Ibuprofen", addedName)
    }

    private fun assertFullFieldKeepsEndAndCursor(field: () -> SemanticsNodeInteraction, cap: Int) {
        val full = "a".repeat(cap - 1) + "z"
        field().performTextInput(full)

        field().performTextInputSelection(TextRange(cap / 2))
        field().performTextInput("Q")

        assertEquals(full, field().text())
        assertEquals(TextRange(cap / 2), field().selection())
    }

    private fun assertEmojiAcrossTheCapIsRefused(field: () -> SemanticsNodeInteraction, cap: Int) {
        val almostFull = "a".repeat(cap - 1)
        field().performTextInput(almostFull)

        field().performTextInput(THUMBS_UP)

        assertEquals(almostFull, field().text())
    }

    private fun assertPasteIsCutAfterAWholeCharacter(
        field: () -> SemanticsNodeInteraction,
        cap: Int
    ) {
        val start = "a".repeat(cap - 4)
        field().performTextInput(start)

        field().performTextInput("abc$THUMBS_UP$THUMBS_UP")

        assertEquals(start + "abc", field().text())
        assertEquals(TextRange(cap - 1), field().selection())
    }
}

private fun SemanticsNodeInteraction.text(): String =
    fetchSemanticsNode().config[SemanticsProperties.EditableText].text

private fun SemanticsNodeInteraction.selection(): TextRange =
    fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
