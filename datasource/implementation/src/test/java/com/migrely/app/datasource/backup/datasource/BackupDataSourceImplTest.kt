package com.migrely.app.datasource.backup.datasource

import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.backup.dao.BackupDao
import com.migrely.app.datasource.backup.mapper.BackupLocalMapper
import com.migrely.app.datasource.backup.model.AddedLocalModel
import com.migrely.app.datasource.backup.model.AddedRecordsDataModel
import com.migrely.app.datasource.backupfile.RECORDS
import com.migrely.app.datasource.daymark.mapper.DayMarkLocalMapper
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class BackupDataSourceImplTest {
    private val backupDao: BackupDao = mock()
    private val backupLocalMapper = BackupLocalMapper(
        AttackTagLocalMapper(),
        CustomTagLocalMapper(),
        MedicationLocalMapper(),
        DayMarkLocalMapper()
    )
    private var tablesCleared = 0
    private var clearFailure: RuntimeException? = null
    private val classUnderTest = BackupDataSourceImpl(backupDao, backupLocalMapper) {
        clearFailure?.let { throw it }
        tablesCleared++
    }

    @Test
    fun `When reading then every record, and whether there are any`() {
        given(backupDao.records()).willReturn(backupLocalMapper.toLocal(RECORDS))
        given(backupDao.hasRecords()).willReturn(true)

        assertEquals(RECORDS, classUnderTest.records())
        assertEquals(true, classUnderTest.hasRecords())
    }

    @Test
    fun `When replacing or adding then the rows go to the database in one call`() {
        given(
            backupDao.add(any())
        ).willReturn(AddedLocalModel(addedAttacks = 1, skippedAttacks = 1, addedOthers = 2))

        classUnderTest.replaceAll(RECORDS)
        assertEquals(
            AddedRecordsDataModel(addedAttacks = 1, skippedAttacks = 1, addedOthers = 2),
            classUnderTest.add(RECORDS)
        )

        verify(backupDao).replaceAll(backupLocalMapper.toLocal(RECORDS))
        verify(backupDao).add(backupLocalMapper.toLocal(RECORDS))
    }

    @Test
    fun `Given the database fails when writing then storage full or a database failure`() {
        given(backupDao.replaceAll(any())).willThrow(SQLiteFullException())
        given(backupDao.add(any())).willThrow(SQLiteConstraintException())

        assertThrows(StorageFullDataException::class.java) { classUnderTest.replaceAll(RECORDS) }
        assertThrows(DatabaseDataException::class.java) { classUnderTest.add(RECORDS) }
    }

    @Test
    fun `When deleting everything then every table is cleared at once`() {
        classUnderTest.deleteAll()

        assertEquals(1, tablesCleared)
    }

    @Test
    fun `Given the database fails when deleting everything then a database failure`() {
        clearFailure = SQLiteException()

        assertThrows(DatabaseDataException::class.java) { classUnderTest.deleteAll() }
        assertEquals(0, tablesCleared)
    }
}
