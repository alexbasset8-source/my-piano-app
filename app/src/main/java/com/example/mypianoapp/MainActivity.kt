package com.example.mypianoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mypianoapp.navigation.AppNavigation
import com.example.mypianoapp.ui.theme.MyPianoAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            MyPianoAppTheme {

                AppNavigation()

            }
        }
    }
}