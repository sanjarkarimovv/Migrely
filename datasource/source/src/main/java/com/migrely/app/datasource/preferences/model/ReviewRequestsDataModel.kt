package com.migrely.app.datasource.preferences.model

/** How many times the app has asked for a rating, and when it last did, in epoch milliseconds (BRD D131). */
data class ReviewRequestsDataModel(val count: Int, val lastMillis: Long?)
