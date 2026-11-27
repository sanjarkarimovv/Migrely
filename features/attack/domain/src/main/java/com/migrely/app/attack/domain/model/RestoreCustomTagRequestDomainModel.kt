package com.migrely.app.attack.domain.model

/** The tag to bring back to the user's list, and its kind, whose list must have room (BRD D199, EC-ATK-26). */
data class RestoreCustomTagRequestDomainModel(val tagId: String, val kind: TagKindDomainModel)
