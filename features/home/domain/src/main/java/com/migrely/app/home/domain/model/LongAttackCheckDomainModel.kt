package com.migrely.app.home.domain.model

import java.time.Instant

data class LongAttackCheckDomainModel(val confirmedAt: Instant?, val disabled: Boolean)
