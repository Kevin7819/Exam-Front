package com.moviles.exam_front.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moviles.exam_front.data.DatabaseProvider
import com.moviles.exam_front.models.Course
import com.moviles.exam_front.models.CourseEntity
import com.moviles.exam_front.network.RetrofitInstance
import com.moviles.exam_front.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

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

    private fun createMultipartRequestBody(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    fun fetchCourses() {
        viewModelScope.launch {
            try {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    Log.d("CourseViewModel", "Network available, fetching from API...")
                    val apiCourses = RetrofitInstance.getApi().getCourses()
                    Log.d("CourseViewModel", "Fetched ${apiCourses.size} courses from API")

                    // Clear and insert new data
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

    fun addCourse(course: Course, imageFile: File?) {
        viewModelScope.launch {
            try {
                Log.d("VIEW_MODEL", "Adding new course: ${course.name}")
                Log.d("VIEW_MODEL", "Image file provided: ${imageFile != null}")
                if (imageFile == null || !imageFile.exists()) {
                    Log.e("CourseViewModel", "El archivo de imagen no existe o es nulo")
                    return@launch
                }


                Log.d("CourseViewModel", "Agregando curso: ${course.name} con imagen: ${imageFile.name}")

                // 1. Crear las partes del formulario para los campos de texto
                val namePart = course.name.toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionPart = course.description.toRequestBody("text/plain".toMediaTypeOrNull())
                val schedulePart = course.schedule.toRequestBody("text/plain".toMediaTypeOrNull())
                val professorPart = course.professor.toRequestBody("text/plain".toMediaTypeOrNull())

                // 2. Preparar la imagen como MultipartBody.Part
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData(
                    "ImageFile",
                    imageFile.name,
                    requestFile
                )

                // 3. Llamar a la API
                val response = RetrofitInstance.getApi().addCourse(
                    name = namePart,
                    description = descriptionPart,
                    schedule = schedulePart,
                    professor = professorPart,
                    imageUrl = imagePart // Asegúrate que este nombre coincida con el parámetro en tu interfaz
                )

                // 4. Guardar en la base de datos local
                courseDao.insert(response.toEntity())
                Log.d("CourseViewModel", "Curso agregado exitosamente con ID: ${response.id}")

                // 5. Actualizar la lista
                fetchCourses()

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CourseViewModel", "Error HTTP ${e.code()}: ${e.message()}\nBody: $errorBody")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error al agregar curso: ${e.message}", e)
            }
        }
    }

    fun updateCourse(course: Course, imageFile: File?) {
        viewModelScope.launch {
            try {
                if (course.id == null) {
                    Log.e("CourseViewModel", "Cannot update course without ID")
                    return@launch
                }

                Log.d("CourseViewModel", "Updating course: $course")

                // Create multipart request parts
                val namePart = createMultipartRequestBody(course.name)
                val descriptionPart = createMultipartRequestBody(course.description)
                val schedulePart = createMultipartRequestBody(course.schedule)
                val professorPart = createMultipartRequestBody(course.professor)

                // Handle image - either use existing or upload new one
                val imagePart = if (imageFile != null) {
                    val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("ImageFile", imageFile.name, requestFile)
                } else {
                    // If no new image provided, we'll keep the existing one
                    MultipartBody.Part.createFormData("ImageFile", "", "".toRequestBody())
                }

                // Make API call
                val updated = RetrofitInstance.getApi().updateCourse(
                    id = course.id,
                    name = namePart,
                    description = descriptionPart,
                    schedule = schedulePart,
                    professor = professorPart,
                    imageUrl = imagePart
                )

                // Save to local database
                courseDao.insertAll(listOf(updated.toEntity()))
                Log.d("CourseViewModel", "Course updated: ${updated.id}")

                // Refresh data
                fetchCourses()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("CourseViewModel", "HTTP error: ${e.message()}, Body: $errorBody")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error updating course: ${e.message}", e)
            }
        }
    }

    fun deleteCourse(courseId: Int) {
        viewModelScope.launch {
            try {
                Log.d("CourseViewModel", "Deleting course with ID: $courseId")

                // Delete from API
                RetrofitInstance.getApi().deleteCourse(courseId)

                // Delete locally
                courseDao.deleteById(courseId)

                Log.d("CourseViewModel", "Course deleted from API and DB")

                // Refresh data
                fetchCourses()
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error deleting course: ${e.message}", e)
            }
        }
    }
}

// Extension function to convert Course to CourseEntity
private fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        imageUrl = this.imageUrl,
        schedule = this.schedule,
        professor = this.professor
    )
}

// Extension function to convert CourseEntity to Course
private fun CourseEntity.toModel(): Course {
    return Course(
        id = this.id,
        name = this.name,
        description = this.description,
        imageUrl = this.imageUrl,
        schedule = this.schedule,
        professor = this.professor
    )
}