package com.migrely.app.datasource.backupfile.model

import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import java.time.Instant

/** An automatic backup kept inside the app (BRD D73, D98): when it was made, and the records it holds. */
data class AutomaticCopyDataModel(val madeAt: Instant, val records: BackupRecordsDataModel)
