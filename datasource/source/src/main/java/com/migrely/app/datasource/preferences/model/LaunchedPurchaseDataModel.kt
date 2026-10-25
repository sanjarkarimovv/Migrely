package com.migrely.app.datasource.preferences.model

/**
 * What was asked of Google Play's purchase sheet, kept before it opens (change add-subscription,
 * review M5). [freeTrialPeriod] is Play's ISO 8601 period, such as `P7D`; times are epoch millis.
 */
data class LaunchedPurchaseDataModel(
    val productId: String,
    val basePlanId: String?,
    val freeTrialPeriod: String?,
    val launchedAtMillis: Long
)
