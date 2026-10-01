package com.renatizzi.photovideomanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.renatizzi.photovideomanager.ui.navigation.PvmApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as PvmApplication
        setContent {
            PvmApp(catalogFacade = app.container.catalogFacade)
        }
    }
}
