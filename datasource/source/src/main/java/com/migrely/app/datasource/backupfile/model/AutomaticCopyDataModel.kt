package com.migrely.app.datasource.backupfile.model

import java.time.Instant

/** An automatic backup kept inside the app (BRD D73, D98): when it was made, and the file's text. */
data class AutomaticCopyDataModel(val madeAt: Instant, val text: String)
