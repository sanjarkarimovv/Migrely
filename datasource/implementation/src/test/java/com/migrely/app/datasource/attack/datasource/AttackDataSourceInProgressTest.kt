package com.migrely.app.datasource.attack.datasource

import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteFullException
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.mapper.AttackLocalMapper
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

/** Following whether an attack is in progress, for the app's theme (change add-settings-and-safety, BRD D94). */
class AttackDataSourceInProgressTest {
    private val attackDao: AttackDao = mock()
    private val classUnderTest = AttackDataSourceImpl(attackDao, AttackLocalMapper())

    @Test
    fun `When following the attack in progress then each change, once`() = runTest {
        given(attackDao.inProgressChanges()).willReturn(flowOf(false, true, true, false))

        assertEquals(listOf(false, true, false), classUnderTest.inProgressChanges().toList())
    }

    @Test
    fun `Given the database fails while following then storage full or a database failure`() {
        mapOf(
            SQLiteFullException() to StorageFullDataException::class.java,
            SQLiteException() to DatabaseDataException::class.java
        ).forEach { (failure, expected) ->
            given(attackDao.inProgressChanges()).willReturn(
                flow {
                    emit(true)
                    throw failure
                }
            )
            assertThrows(expected) {
                runBlocking { classUnderTest.inProgressChanges().toList() }
            }
        }
    }
}
