package com.migrely.app.reminders.domain.repository

import com.migrely.app.reminders.domain.model.AttackInProgressDomainModel

interface GetAttackInProgressRepository {
    fun attackInProgress(): AttackInProgressDomainModel
}
