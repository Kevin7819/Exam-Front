package com.moviles.exam_front

import android.app.Application
import com.moviles.exam_front.network.RetrofitInstance

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitInstance.init(applicationContext)
    }
}
