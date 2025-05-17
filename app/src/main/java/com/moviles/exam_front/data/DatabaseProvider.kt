package com.moviles.exam_front.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.moviles.exam_front.common.Constants.DATA_BASE

object DatabaseProvider {
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATA_BASE
            )
                .allowMainThreadQueries()
                .build()
        }
        return db!!
    }
}
