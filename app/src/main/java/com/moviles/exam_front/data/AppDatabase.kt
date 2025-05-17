package com.moviles.exam_front.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.moviles.exam_front.models.CourseEntity
import com.moviles.exam_front.models.StudentEntity
import com.moviles.exam_front.dao.CourseDao
import com.moviles.exam_front.dao.StudentDao

@Database(
    entities = [CourseEntity::class, StudentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun studentDao(): StudentDao
}
