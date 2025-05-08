package com.moviles.exam_front.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.exam_front.models.Student
import com.moviles.exam_front.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentViewModel : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> get() = _students

    // Fetch students for a specific course
    fun fetchStudentsByCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getStudentsByCourse(courseId)
                _students.value = result
                Log.i("StudentViewModel", "Loaded ${result.size} students for course $courseId")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Failed to fetch students: ${e.message}", e)
            }
        }
    }

    // Add new student
    fun addStudent(student: Student) {
        viewModelScope.launch {
            try {
                Log.i("StudentViewModel", "Saving student: $student")
                val created = RetrofitInstance.api.addStudent(student)
                _students.value = _students.value + created
                Log.i("StudentViewModel", "Student created: $created")
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("StudentViewModel", "HTTP error: ${e.message()}")
                Log.e("StudentViewModel", "HTTP error: ${e.code()} - $errorBody")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error adding student: ${e.message}", e)
            }
        }
    }

    // Update existing student
    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                val updated = RetrofitInstance.api.updateStudent(student.id!!, student)
                _students.value = _students.value.map {
                    if (it.id == updated.id) updated else it
                }
                Log.i("StudentViewModel", "Student updated: $updated")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error updating student: ${e.message}", e)
            }
        }
    }

    // Delete student
    fun deleteStudent(studentId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.deleteStudent(studentId)
                _students.value = _students.value.filter { it.id != studentId }
                Log.i("StudentViewModel", "Student deleted: $studentId")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error deleting student: ${e.message}", e)
            }
        }
    }
}
