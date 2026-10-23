package com.migrely.app.attack.domain.model

import java.time.Instant

/**
 * Times to merge with the attacks they overlap. [attackId] is the attack being edited, or null for a new past attack,
 * which always has an [end]. The end of an attack in progress is ignored. [editedNote] is the edited attack's note as
 * the screen shows it, which may not be stored yet; it is stored first and joined in place of the stored note.
 */
data class MergeAttacksRequestDomainModel(
    val attackId: String?,
    val start: Instant,
    val end: Instant?,
    val editedNote: String?
)
