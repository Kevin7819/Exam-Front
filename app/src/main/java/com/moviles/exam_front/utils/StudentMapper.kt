package com.moviles.exam_front.utils

import com.moviles.exam_front.models.Student
import com.moviles.exam_front.models.StudentEntity

fun Student.toEntity(): StudentEntity = StudentEntity(
    id = id ?: 0,
    name = name,
    email = email,
    phone = phone,
    courseId = courseId
)

fun StudentEntity.toModel(): Student = Student(
    id = id,
    name = name,
    email = email,
    phone = phone,
    courseId = courseId
)
