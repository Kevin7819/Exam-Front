package com.moviles.exam_front.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.exam_front.models.Student
import com.moviles.exam_front.network.RetrofitInstance
import com.moviles.exam_front.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> get() = _students

    private val _dataOrigin = MutableStateFlow("API")
    val dataOrigin: StateFlow<String> get() = _dataOrigin

    private var currentCourseId: Int = -1

    fun fetchStudentsByCourse(courseId: Int) {
        currentCourseId = courseId
        Log.e("StudentViewModel", "Course id : $courseId")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val networkAvailable = NetworkUtils.isNetworkAvailable(context)
                Log.e("StudentViewModel", "Network available: $networkAvailable")

                if (networkAvailable) {
                    try {
                        val apiStudents = RetrofitInstance.getApi().getStudentsByCourse(courseId)
                        Log.e("StudentViewModel", "API students fetched: ${apiStudents.size}")
                        _students.value = apiStudents
                        _dataOrigin.value = "API"
                    } catch (e: Exception) {
                        Log.e("StudentViewModel", "API Error: ${e.message}", e)
                        _dataOrigin.value = "API Error"
                    }
                } else {
                    _dataOrigin.value = "Sin conexión"
                    _students.value = emptyList()
                }

            } catch (e: Exception) {
                Log.e("StudentViewModel", "General Error: ${e.message}", e)
                _students.value = emptyList()
            }
        }
    }

    fun addStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val created = RetrofitInstance.getApi().addStudent(student)
                fetchStudentsByCourse(created.courseId)
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("StudentViewModel", "HTTP error: ${e.message()} - $errorBody")
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error adding student: ${e.message}", e)
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val updated = RetrofitInstance.getApi().updateStudent(student.id!!, student)
                fetchStudentsByCourse(updated.courseId)
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error updating student: ${e.message}", e)
            }
        }
    }

    fun deleteStudent(studentId: Int, courseId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                RetrofitInstance.getApi().deleteStudent(studentId)
                fetchStudentsByCourse(courseId)
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error deleting student", e)
            }
        }
    }
}
