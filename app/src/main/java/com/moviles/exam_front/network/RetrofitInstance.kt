package com.moviles.exam_front.network

import android.content.Context
import com.moviles.exam_front.common.Constants.API_BASE_URL
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.net.ConnectivityManager
import android.net.NetworkInfo

object RetrofitInstance {
    private var retrofit: Retrofit? = null
    lateinit var api: ApiService

    fun init(context: Context) {
        if (retrofit == null) {
            val cacheSize = 5 * 1024 * 1024L
            val cache = Cache(context.applicationContext.cacheDir, cacheSize)

            val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("Cache-Control", if (hasNetwork(context)) "public, max-age=5" else "public, only-if-cached, max-stale=86400")
                        .build()
                    chain.proceed(request)
                }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            api = retrofit!!.create(ApiService::class.java)
        }
    }

    private fun hasNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}