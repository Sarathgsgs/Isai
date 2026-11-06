package com.isai.player

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IsaiApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
