package com.migrely.app.reminders.domain.model

/** Whether a period holds an attack, an intake or a pain-free mark (BRD D81's rule). */
enum class AnythingLoggedDomainModel {
    Logged,
    NothingLogged,
    Failed
}
