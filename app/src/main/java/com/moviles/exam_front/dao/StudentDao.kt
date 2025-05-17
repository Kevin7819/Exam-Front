package com.moviles.exam_front.dao

import androidx.room.*
import com.moviles.exam_front.models.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students WHERE courseId = :courseId")
    fun getByCourse(courseId: Int): Flow<List<StudentEntity>>

    @Query("SELECT * FROM Students WHERE id = :studentId")
    suspend fun getById(studentId: Int): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)

    @Query("DELETE FROM Students WHERE courseId = :courseId")
    suspend fun clearByCourse(courseId: Int)

    @Query("DELETE FROM Students WHERE id = :studentId")
    suspend fun deleteById(studentId: Int)
}