package com.moviles.exam_front.utils

import com.moviles.exam_front.models.Course
import com.moviles.exam_front.models.CourseEntity

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id ?: 0,
    name = name,
    description = description,
    imageUrl = imageUrl ?: "",
    schedule = schedule,
    professor = professor
)

fun CourseEntity.toModel(): Course = Course(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    schedule = schedule,
    professor = professor,
    students = emptyList() // Puedes cargar los estudiantes luego si los necesitas
)
