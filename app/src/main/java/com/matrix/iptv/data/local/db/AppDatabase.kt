package com.matrix.iptv.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProfileEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao

    companion object {
        const val DATABASE_NAME = "matrix_iptv.db"
    }
}
