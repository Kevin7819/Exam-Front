package com.moviles.exam_front.dao

import androidx.room.*
import com.moviles.exam_front.models.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM Courses")
    fun getAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM Courses WHERE id = :courseId")
    suspend fun getById(courseId: Int): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<CourseEntity>)

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("DELETE FROM Courses")
    suspend fun clear()

    @Query("DELETE FROM Courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Int)
}