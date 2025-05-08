package com.moviles.exam_front.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.exam_front.models.Course
import com.moviles.exam_front.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CourseViewModel : ViewModel() {

    // Internal mutable list of courses
    private val _courses = MutableStateFlow<List<Course>>(emptyList())

    // Exposed immutable StateFlow for UI observation
    val courses: StateFlow<List<Course>> get() = _courses

    // Fetch all courses from API, including their enrolled students
    fun fetchCourses() {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getCourses()
                _courses.value = result
                Log.i("CourseViewModel", "Courses loaded: ${result.size}")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Failed to fetch courses: ${e.message}", e)
            }
        }
    }

    // Create a new course via the API
    fun addCourse(course: Course) {
        viewModelScope.launch {
            try {
                Log.i("CourseViewModel", "Saving course: $course")
                val created = RetrofitInstance.api.addCourse(course)
                _courses.value = _courses.value + created // append to current list
                Log.i("CourseViewModel", "Course created: $created")
            } catch (e: HttpException) {
                Log.e("CourseViewModel", "HTTP error: ${e.message()}")
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CourseViewModel", "HTTP error: ${e.code()} - $errorBody")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error creating course: ${e.message}", e)
            }
        }
    }

    // Update an existing course
    fun updateCourse(course: Course) {
        viewModelScope.launch {
            try {
                val updated = RetrofitInstance.api.updateCourse(course.id!!, course)
                _courses.value = _courses.value.map {
                    if (it.id == updated.id) updated else it
                }
                Log.i("CourseViewModel", "Course updated: $updated")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error updating course: ${e.message}")
            }
        }
    }

    // Delete a course by ID
    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteCourse(courseId)
                _courses.value = _courses.value.filter { it.id != courseId }
                Log.i("CourseViewModel", "Course deleted: $courseId")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error deleting course: ${e.message}")
            }
        }
    }
}
