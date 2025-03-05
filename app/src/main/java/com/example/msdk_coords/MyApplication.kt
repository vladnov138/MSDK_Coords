package com.example.msdk_coords

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.secneo.sdk.Helper

class MyApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        try {
            // Явная загрузка Helper
            Helper.install(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}