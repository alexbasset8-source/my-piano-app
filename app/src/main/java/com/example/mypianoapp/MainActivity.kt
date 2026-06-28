package com.example.mypianoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.mypianoapp.navigation.AppNavigation
import com.example.mypianoapp.ui.theme.MyPianoAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash screen natif Android 12+ — s'affiche pendant le cold start
        installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyPianoAppTheme {
                AppNavigation()
            }
        }
    }
}
