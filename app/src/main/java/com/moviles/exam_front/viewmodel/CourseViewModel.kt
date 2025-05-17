package com.moviles.exam_front.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.exam_front.data.DatabaseProvider
import com.moviles.exam_front.models.Course
import com.moviles.exam_front.network.RetrofitInstance
import com.moviles.exam_front.utils.NetworkUtils
import com.moviles.exam_front.utils.toEntity
import com.moviles.exam_front.utils.toModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val db = DatabaseProvider.getDatabase(context)
    private val courseDao = db.courseDao()

    // StateFlow para exponer la lista de cursos a la UI
    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> get() = _courses

    // Para mostrar el origen de los datos (API o LOCAL)
    private val _dataOrigin = MutableStateFlow("LOCAL")
    val dataOrigin: StateFlow<String> get() = _dataOrigin

    init {
        // Recolectamos datos desde Room y los transformamos a modelo
        viewModelScope.launch {
            courseDao.getAll().collect { entityList ->
                _courses.value = entityList.map { it.toModel() }
                Log.d("CourseViewModel", "Loaded ${entityList.size} courses from DB")
            }
        }
    }

    fun fetchCourses() {
        viewModelScope.launch {
            try {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    Log.d("CourseViewModel", "Network available, fetching from API...")
                    val apiCourses = RetrofitInstance.getApi().getCourses()
                    Log.d("CourseViewModel", "Fetched ${apiCourses.size} courses from API")

                    courseDao.clear()
                    courseDao.insertAll(apiCourses.map { it.toEntity() })
                    _dataOrigin.value = "API"
                } else {
                    Log.d("CourseViewModel", "No internet, using local data")
                    _dataOrigin.value = "LOCAL"
                }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error in fetchCourses: ${e.message}", e)
            }
        }
    }

    fun addCourse(course: Course) {
        viewModelScope.launch {
            try {
                Log.d("CourseViewModel", "Adding course: $course")
                val created = RetrofitInstance.getApi().addCourse(course)
                courseDao.insertAll(listOf(created.toEntity()))
                Log.d("CourseViewModel", "Course added with ID: ${created.id}")
                fetchCourses()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CourseViewModel", "HTTP error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error adding course: ${e.message}", e)
            }
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            try {
                Log.d("CourseViewModel", "Updating course: $course")
                val updated = RetrofitInstance.getApi().updateCourse(course.id!!, course)
                courseDao.insertAll(listOf(updated.toEntity()))
                Log.d("CourseViewModel", "Course updated: ${updated.id}")
                fetchCourses()
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error updating course: ${e.message}", e)
            }
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                Log.d("CourseViewModel", "Deleting course with ID: $courseId")

                // Eliminar en API
                RetrofitInstance.getApi().deleteCourse(courseId)

                // Eliminar localmente
                courseDao.deleteById(courseId)

                Log.d("CourseViewModel", "Course deleted from API and DB")

                // Actualizar listado
                fetchCourses()
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error deleting course: ${e.message}", e)
            }
        }
    }
}
