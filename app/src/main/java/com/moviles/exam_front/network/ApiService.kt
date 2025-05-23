package com.moviles.exam_front.network

import com.moviles.exam_front.models.Course
import com.moviles.exam_front.models.Student
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    // ------- courses -------

    @GET("api/courses")
    suspend fun getCourses(): List<Course>

    @GET("api/courses/{id}")
    suspend fun getCourseById(@Path("id") id: Int): Course

    @Multipart
    @POST("api/courses")
    suspend fun addCourse(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("schedule") schedule: RequestBody,
        @Part("professor") professor: RequestBody,
        @Part imageUrl: MultipartBody.Part
    ): Course

    @Multipart
    @PUT("api/courses/{id}")
    suspend fun updateCourse(
        @Path("id") id: Int,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("schedule") schedule: RequestBody,
        @Part("professor") professor: RequestBody,
        @Part imageUrl: MultipartBody.Part
    ): Course

    @DELETE("api/courses/{id}")
    suspend fun deleteCourse(@Path("id") id: Int)


    // ------- students -------

    @POST("api/students")
    suspend fun addStudent(@Body student: Student): Student

    @GET("api/students/byCourse/{courseId}")
    suspend fun getStudentsByCourse(@Path("courseId") courseId: Int): List<Student>

    @PUT("api/students/{id}")
    suspend fun updateStudent(@Path("id") id: Int, @Body student: Student): Student

    @DELETE("api/students/{id}")
    suspend fun deleteStudent(@Path("id") id: Int)


}