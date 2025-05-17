package com.moviles.exam_front.network

import android.annotation.SuppressLint
import android.content.Context
import com.moviles.exam_front.common.Constants.API_BASE_URL
import com.moviles.exam_front.utils.NetworkUtils
import com.moviles.exam_front.network.ApiService
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

@SuppressLint("StaticFieldLeak")
object RetrofitInstance {
    private const val CACHE_SIZE = 10 * 1024 * 1024 // 10 MB

    private lateinit var apiService: ApiService

    fun init(context: Context) {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDir, CACHE_SIZE.toLong())

        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain ->
                val request = if (NetworkUtils.isNetworkAvailable(context.applicationContext)) {
                    chain.request().newBuilder()
                        .header("Cache-Control", "public, max-age=60").build()
                } else {
                    chain.request().newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=604800").build()
                }
                chain.proceed(request)
            }
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getApi(): ApiService {
        if (!::apiService.isInitialized) {
            throw IllegalStateException("RetrofitInstance.init(context) must be called before accessing API")
        }
        return apiService
    }
}
