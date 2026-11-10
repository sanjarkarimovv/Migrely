package com.migrely.app.storage

import android.content.Context

/**
 * Android's own path: `getCodeCacheDir()` creates a missing folder and writes its inode into installd's
 * `user.inode_code_cache` mark, and leaves an existing folder and its contents as they are (BRD D170).
 */
class ContextCodeCacheFolder(private val context: Context) : CodeCacheFolder {
    override fun ensure() {
        context.codeCacheDir
    }
}
