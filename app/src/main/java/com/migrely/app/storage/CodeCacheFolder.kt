package com.migrely.app.storage

/** The app's `code_cache` folder, which [CodeCacheMark] keeps in step with installd's mark (BRD D170). */
interface CodeCacheFolder {
    /** Creates the folder when it is missing, together with its mark; does nothing when it exists. */
    fun ensure()
}
