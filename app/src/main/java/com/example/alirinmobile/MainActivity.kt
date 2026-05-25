package com.example.alirinmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.alirinmobile.nav.AlirinNavHost
import com.example.alirinmobile.ui.theme.ALIRINMOBILETheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen() must run BEFORE super.onCreate() so the system
        // splash hands off cleanly to our post-splash theme without a white flash.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ALIRINMOBILETheme {
                AlirinNavHost()
            }
        }
    }
}
