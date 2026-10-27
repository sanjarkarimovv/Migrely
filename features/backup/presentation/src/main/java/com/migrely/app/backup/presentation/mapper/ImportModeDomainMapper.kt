package com.migrely.app.backup.presentation.mapper

import com.migrely.app.backup.domain.model.ImportModeDomainModel
import com.migrely.app.backup.presentation.model.ImportModePresentationModel

class ImportModeDomainMapper {
    fun toDomain(mode: ImportModePresentationModel) = when (mode) {
        ImportModePresentationModel.Replace -> ImportModeDomainModel.Replace
        ImportModePresentationModel.Add -> ImportModeDomainModel.Add
    }
}
