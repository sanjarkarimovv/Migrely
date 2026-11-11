package com.migrely.app.storage

/**
 * Keeps installd's `code_cache` mark true, so an app update clears only `code_cache` (BRD D170, EC-STO-06…09).
 *
 * installd keeps the inode of the app's `code_cache` folder as the `user.inode_code_cache` mark on the app's data folder
 * and, at an update, clears "code_cache" by that number. On Android 8.0 "Clear storage" deletes the folder but keeps
 * the mark, so a folder made later can take the freed number, and the next update empties it: `databases/` (the diary,
 * #193) or `no_backup/` (WorkManager's reminders, #189). When the folder is missing, ensuring it makes Android create
 * it and write its real inode into the mark (`ContextImpl.ensurePrivateDirExists`, API 26 to 36).
 *
 * [MigrelyApplication][com.migrely.app.MigrelyApplication] calls [keep] at every process start, whichever component
 * starts it. It never stops the start: a failure is caught and only its kind is logged, never a path or a message,
 * which could hold diary content (EC-STO-07).
 */
class CodeCacheMark(private val folder: CodeCacheFolder, private val warn: (String) -> Unit) {
    @Suppress("TooGenericExceptionCaught")
    fun keep() {
        try {
            folder.ensure()
        } catch (e: Exception) {
            warn("code_cache check failed: ${e.javaClass.simpleName}")
        }
    }
}
