package com.migrely.app.backup.ui.model

/** [working] while a file is being written or read; [automaticCopy] is null when there is none to restore. */
data class YourDataViewStateUiModel(val working: Boolean, val automaticCopy: AutomaticCopyUiModel?)
