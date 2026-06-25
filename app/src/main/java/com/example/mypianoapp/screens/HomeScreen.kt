package com.example.mypianoapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mypianoapp.components.buttons.AppButton
import com.example.mypianoapp.components.cards.AppCard
import com.example.mypianoapp.components.progress.ProgressSection

@Composable
fun HomeScreen() {

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.spacedBy(24.dp)

    ) {

        Text(

            text = "🎹 My Piano App",

            style = MaterialTheme.typography.headlineLarge

        )

        Text(

            text = "10 minutes par jour",

            style = MaterialTheme.typography.bodyLarge

        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(

            text = "Bonjour 👋",

            style = MaterialTheme.typography.titleLarge

        )

        ProgressSection()

        AppCard {

            AppButton(

                text = "Continuer ma progression",

                onClick = {}

            )
        }

        AppCard {

            Text(

                text = "📚 Parcours d'apprentissage",

                modifier = Modifier.padding(20.dp)

            )
        }

        AppCard {

            Text(

                text = "🎵 Exercices du jour",

                modifier = Modifier.padding(20.dp)

            )
        }
        Card {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text("Progression")

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LinearProgressIndicator(

                    progress = { 0.12f },

                    modifier = Modifier.fillMaxWidth()

                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text("12 % terminé")
            }
        }
    }
}