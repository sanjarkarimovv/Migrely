package com.migrely.app.datasource.preferences.model

/** What showed a paywall without a tap (BRD D110). [key] is stored and must never change. */
enum class PaywallTriggerDataModel(val key: String) {
    Onboarding("onboarding"),
    FifthMigraine("fifth_migraine")
}
