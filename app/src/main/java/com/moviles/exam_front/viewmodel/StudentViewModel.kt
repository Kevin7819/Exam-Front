package com.moviles.exam_front.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.exam_front.data.DatabaseProvider
import com.moviles.exam_front.models.Student
import com.moviles.exam_front.network.RetrofitInstance
import com.moviles.exam_front.utils.NetworkUtils
import com.moviles.exam_front.utils.toEntity
import com.moviles.exam_front.utils.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = DatabaseProvider.getDatabase(context)
    private val studentDao = db.studentDao()

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> get() = _students

    private val _dataOrigin = MutableStateFlow("LOCAL")
    val dataOrigin: StateFlow<String> get() = _dataOrigin

    // Modificar fetchStudentsByCourse
    fun fetchStudentsByCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    val apiStudents = RetrofitInstance.getApi().getStudentsByCourse(courseId)
                    studentDao.clearByCourse(courseId)
                    studentDao.insertAll(apiStudents.map { it.toEntity() })
                    _dataOrigin.value = "API"
                } else {
                    _dataOrigin.value = "LOCAL"
                }

                // Observar el Flow del DAO
                studentDao.getByCourse(courseId).collect { entities ->
                    _students.value = entities.map { it.toModel() }
                }

            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error: ${e.message}", e)
            }
        }
    }


    fun addStudent(student: Student) {
        viewModelScope.launch {
            try {
                val created = RetrofitInstance.getApi().addStudent(student)
                studentDao.insertAll(listOf(created.toEntity()))
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
        viewModelScope.launch {
            try {
                val updated = RetrofitInstance.getApi().updateStudent(student.id!!, student)
                studentDao.insertAll(listOf(updated.toEntity()))
                fetchStudentsByCourse(updated.courseId)
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error updating student: ${e.message}", e)
            }
        }
    }
    fun deleteStudent(studentId: Int, courseId: Int) {
        viewModelScope.launch {
            try {
                RetrofitInstance.getApi().deleteStudent(studentId)
                // Implementar este método en el DAO
                studentDao.deleteById(studentId)
                fetchStudentsByCourse(courseId)
            } catch (e: Exception) {
                Log.e("StudentViewModel", "Error deleting student", e)
            }
        }
    }

}
