package com.migrely.app.settings.domain.model

enum class DeleteAllDataResultDomainModel {
    /** Everything is gone; the app starts again as on a new phone. */
    Deleted,

    /** A step failed and the rest did not run. Trying again is safe. */
    Failed
}
