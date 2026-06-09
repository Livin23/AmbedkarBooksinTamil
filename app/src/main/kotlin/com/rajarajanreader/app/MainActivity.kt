package com.rajarajanreader.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.rajarajanreader.app.domain.ReadingTheme
import com.rajarajanreader.app.presentation.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var theme by remember { mutableStateOf(ReadingTheme.DARK) }
            AppNavigation(
                theme       = theme,
                onThemeCycle = {
                    theme = when (theme) {
                        ReadingTheme.LIGHT -> ReadingTheme.SEPIA
                        ReadingTheme.SEPIA -> ReadingTheme.DARK
                        ReadingTheme.DARK  -> ReadingTheme.LIGHT
                    }
                }
            )
        }
    }
}
