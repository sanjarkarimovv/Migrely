package com.migrely.app.datasource.preferences.datasource

import android.content.SharedPreferences

/**
 * SharedPreferences as Android's own implementation behaves (#278): a commit puts the change into memory first and only
 * then writes the whole map to the file, so a commit that returns false leaves the change in memory. [diskWorks] says
 * whether the file write succeeds; [disk] is the file as last written.
 */
class MemoryFirstPreferences(initial: Map<String, Any> = emptyMap()) : SharedPreferences {
    private val memory = initial.toMutableMap()
    var disk: Map<String, Any> = initial.toMap()
        private set
    var diskWorks = true

    /** Runs on every read of the whole map, as a snapshot is taken. */
    var onGetAll: () -> Unit = {}

    /** Runs in a commit after memory changed and before the file is written. */
    var afterMemoryChange: () -> Unit = {}
    private val listeners = mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

    override fun getAll(): Map<String, *> {
        onGetAll()
        return synchronized(memory) { memory.toMap() }
    }

    override fun getString(key: String, defValue: String?): String? =
        memory[key] as String? ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? =
        memory[key] as Set<String>? ?: defValues

    override fun getInt(key: String, defValue: Int): Int = memory[key] as Int? ?: defValue

    override fun getLong(key: String, defValue: Long): Long = memory[key] as Long? ?: defValue

    override fun getFloat(key: String, defValue: Float): Float = memory[key] as Float? ?: defValue

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        memory[key] as Boolean? ?: defValue

    override fun contains(key: String): Boolean = key in memory

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        listeners += listener
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        listeners -= listener
    }

    private inner class Editor : SharedPreferences.Editor {
        private val changes = mutableMapOf<String, Any?>()
        private var clearFirst = false

        override fun putString(key: String, value: String?) = also { changes[key] = value }

        override fun putStringSet(key: String, values: Set<String>?) =
            also { changes[key] = values?.toSet() }

        override fun putInt(key: String, value: Int) = also { changes[key] = value }

        override fun putLong(key: String, value: Long) = also { changes[key] = value }

        override fun putFloat(key: String, value: Float) = also { changes[key] = value }

        override fun putBoolean(key: String, value: Boolean) = also { changes[key] = value }

        override fun remove(key: String) = also { changes[key] = null }

        override fun clear() = also { clearFirst = true }

        // As SharedPreferencesImpl: memory first, then the file, and the listeners either way.
        override fun commit(): Boolean {
            synchronized(memory) {
                if (clearFirst) memory.clear()
                changes.forEach { (key, value) ->
                    if (value == null) memory.remove(key) else memory[key] = value
                }
            }
            afterMemoryChange()
            if (diskWorks) disk = synchronized(memory) { memory.toMap() }
            changes.keys.forEach { key ->
                listeners.forEach { it.onSharedPreferenceChanged(this@MemoryFirstPreferences, key) }
            }
            return diskWorks
        }

        override fun apply() {
            commit()
        }
    }
}
