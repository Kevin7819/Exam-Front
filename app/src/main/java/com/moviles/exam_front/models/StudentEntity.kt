package com.moviles.exam_front.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Students")
data class StudentEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val courseId: Int
)
