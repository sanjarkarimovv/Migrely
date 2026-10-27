package com.migrely.app.datasource.preferences.model

/** The app's look, as chosen in Settings (BRD SET-F1, D94). [key] is what is stored and must never change. */
enum class ThemeDataModel(val key: String) {
    Dark("dark"),
    Light("light"),

    /** Follows the phone's dark theme setting. */
    System("system")
}
