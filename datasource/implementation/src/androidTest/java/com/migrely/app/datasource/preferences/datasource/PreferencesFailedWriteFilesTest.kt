package com.migrely.app.datasource.preferences.datasource

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.preferences.model.ThemeDataModel
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val SETTINGS_FILE = "t278_settings"
private const val ENTITLEMENT_FILE = "t278_entitlement"

/**
 * On Android's own SharedPreferences, whose commit changes memory before the file (#278, BRD EC-STO-13): with the
 * folder made read-only the commit fails, and the setting must still read as stored, and stay so in the file after a
 * later write succeeds.
 */
@RunWith(AndroidJUnit4::class)
class PreferencesFailedWriteFilesTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val folder = File(context.dataDir, "shared_prefs")
    private val settings = context.getSharedPreferences(SETTINGS_FILE, Context.MODE_PRIVATE)
    private val entitlement = context.getSharedPreferences(ENTITLEMENT_FILE, Context.MODE_PRIVATE)
    private val classUnderTest = PreferencesDataSourceImpl(settings, entitlement)

    private fun fileText() = File(folder, "$SETTINGS_FILE.xml").readText()

    @Before
    fun setUp() {
        settings.edit().clear().commit()
        classUnderTest.setPeriodTracking(true)
        classUnderTest.setTheme(ThemeDataModel.Light)
        assertTrue(fileText().contains("""<boolean name="period_tracking" value="true" />"""))
    }

    @After
    fun tearDown() {
        folder.setWritable(true, true)
        settings.edit().clear().commit()
        entitlement.edit().clear().commit()
    }

    @Test
    fun givenTheFolderIsReadOnly_whenTrackPeriodsIsTurnedOff_thenItStillReadsAsStored() {
        assertTrue(folder.setWritable(false, false))

        assertThrows(FileDataException::class.java) { classUnderTest.setPeriodTracking(false) }

        assertEquals(true, classUnderTest.periodTracking())
        assertEquals(ThemeDataModel.Light, classUnderTest.theme())
    }

    @Test
    fun givenAFailedWrite_whenALaterWriteSucceeds_thenTheFailedChangeIsNotInTheFile() {
        assertTrue(folder.setWritable(false, false))
        assertThrows(FileDataException::class.java) { classUnderTest.setPeriodTracking(false) }

        assertTrue(folder.setWritable(true, true))
        classUnderTest.setTheme(ThemeDataModel.Dark)

        val text = fileText()
        assertTrue(text, text.contains("""<boolean name="period_tracking" value="true" />"""))
        assertTrue(text, text.contains("""<string name="theme">dark</string>"""))
    }
}
