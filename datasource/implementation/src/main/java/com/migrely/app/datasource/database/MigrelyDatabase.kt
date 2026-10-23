package com.migrely.app.datasource.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.migrely.app.datasource.attack.dao.AttackDao
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel

@Database(
    entities = [AttackLocalModel::class, IntensityPointLocalModel::class],
    version = 1,
    exportSchema = true
)
abstract class MigrelyDatabase : RoomDatabase() {
    abstract fun attackDao(): AttackDao

    companion object {
        const val NAME = "migrely.db"
    }
}
