package com.example.mypianoapp.components.cards

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppCard(
    content: @Composable () -> Unit
) {

    Card(

        modifier = Modifier.fillMaxWidth(),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),

        shape = MaterialTheme.shapes.large

    ) {

        androidx.compose.foundation.layout.Column(

            modifier = Modifier,

            content = {

                content()

            }

        )
    }
}