package com.example.mypianoapp.components.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ProgressSection() {

    Column {

        Text("Progression globale")

        LinearProgressIndicator(

            progress = { 0.12f }

        )

        Text("12 %")

    }
}