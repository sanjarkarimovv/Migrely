package com.migrely.app.datasource.appinfo.datasource

/** Facts about the installed app (BRD D97). */
interface AppInfoDataSource {
    /** The installed version as people see it, such as "1.2.0". Never fails: empty when Android does not say. */
    fun versionName(): String

    /**
     * Whether the app's previous run ended with a crash, a native crash or an ANR (BRD D130). Never fails: false when
     * Android does not say, as before Android 11.
     */
    fun previousExitCrashed(): Boolean
}
