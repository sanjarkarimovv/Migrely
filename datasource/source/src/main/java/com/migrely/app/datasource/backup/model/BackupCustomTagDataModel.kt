package com.migrely.app.datasource.backup.model

import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import java.time.Instant

data class BackupCustomTagDataModel(
    val id: String,
    val kind: CustomTagKindDataModel,
    val name: String,
    val archived: Boolean,
    val createdAt: Instant
)
