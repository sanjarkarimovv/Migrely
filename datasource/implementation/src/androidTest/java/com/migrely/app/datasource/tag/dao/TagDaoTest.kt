package com.migrely.app.datasource.tag.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.database.MigrelyDatabase
import com.migrely.app.datasource.tag.model.CustomTagLocalModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagDaoTest {
    private lateinit var database: MigrelyDatabase
    private lateinit var classUnderTest: TagDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MigrelyDatabase::class.java
        ).build()
        classUnderTest = database.tagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test(expected = SQLiteConstraintException::class)
    fun givenATag_whenInsertingTheSameNameKeyAndKind_thenRefused() {
        classUnderTest.insert(tag("1", "SYMPTOM", "Jaw pain"))
        classUnderTest.insert(tag("2", "SYMPTOM", "jaw PAIN"))
    }

    @Test
    fun givenTheSameNameOfAnotherKind_whenInserting_thenAllowed() {
        classUnderTest.insert(tag("1", "SYMPTOM", "Smell"))
        classUnderTest.insert(tag("2", "TRIGGER", "Smell"))

        assertEquals(1, classUnderTest.countActive("TRIGGER"))
    }

    @Test
    fun givenAnArchivedTag_whenListingActive_thenItIsLeftOutButStillFoundByName() {
        classUnderTest.insert(tag("1", "SYMPTOM", "Jaw pain"))
        classUnderTest.insert(tag("2", "SYMPTOM", "Ear pain"))
        classUnderTest.setArchived("1", true)

        assertEquals(listOf("Ear pain"), classUnderTest.activeByKind("SYMPTOM").map { it.name })
        assertEquals("1", classUnderTest.byNameKey("SYMPTOM", "jaw pain")?.id)
    }

    private fun tag(id: String, kind: String, name: String) = CustomTagLocalModel(
        id,
        kind,
        name,
        name.trim().lowercase(),
        archived = false,
        createdEpochMillis = 1L
    )
}
