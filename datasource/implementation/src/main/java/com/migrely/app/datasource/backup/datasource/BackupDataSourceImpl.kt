package com.migrely.app.datasource.backup.datasource

import com.migrely.app.datasource.backup.dao.BackupDao
import com.migrely.app.datasource.backup.mapper.BackupLocalMapper
import com.migrely.app.datasource.backup.model.AddedRecordsDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.database.database

/**
 * [deleteEverything] is `MigrelyDatabase.deleteEverything`: every table, including those added later, in one go, and the
 * AUTOINCREMENT counters.
 */
class BackupDataSourceImpl(
    private val backupDao: BackupDao,
    private val backupLocalMapper: BackupLocalMapper,
    private val deleteEverything: () -> Unit
) : BackupDataSource {
    override fun records(): BackupRecordsDataModel = database {
        backupLocalMapper.toData(backupDao.records())
    }

    override fun hasRecords(): Boolean = database { backupDao.hasRecords() }

    override fun replaceAll(records: BackupRecordsDataModel) = database {
        backupDao.replaceAll(backupLocalMapper.toLocal(records))
    }

    override fun add(records: BackupRecordsDataModel): AddedRecordsDataModel = database {
        val added = backupDao.add(backupLocalMapper.toLocal(records))
        AddedRecordsDataModel(
            addedAttacks = added.addedAttacks,
            skippedAttacks = added.skippedAttacks,
            addedOthers = added.addedOthers
        )
    }

    override fun deleteAll() = database { deleteEverything() }
}
