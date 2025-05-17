package com.moviles.exam_front.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Courses")
data class CourseEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val schedule: String,
    val professor: String
)
