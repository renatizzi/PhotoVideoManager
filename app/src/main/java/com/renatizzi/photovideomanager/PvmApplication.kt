package com.renatizzi.photovideomanager

import android.app.Application
import com.renatizzi.photovideomanager.di.AppContainer

class PvmApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
