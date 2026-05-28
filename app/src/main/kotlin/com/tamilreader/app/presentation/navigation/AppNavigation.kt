package com.tamilbookreader.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tamilbookreader.app.domain.ReadingTheme
import com.tamilbookreader.app.presentation.screen.index.IndexScreen
import com.tamilbookreader.app.presentation.screen.reader.ReaderScreen
import com.tamilbookreader.app.presentation.screen.splash.SplashScreen
import com.tamilbookreader.app.presentation.theme.TamilReaderTheme

@Composable
fun AppNavigation(theme: ReadingTheme, onThemeCycle: () -> Unit) {
    val nav = rememberNavController()

    TamilReaderTheme(theme) {
        NavHost(nav, startDestination = "splash") {

            composable("splash") {
                SplashScreen(onStart = {
                    nav.navigate("index") {
                        popUpTo("splash") { inclusive = true }
                    }
                })
            }

            composable("index") {
                IndexScreen(
                    onChapterClick = { nav.navigate("reader/$it") },
                    onThemeCycle   = onThemeCycle
                )
            }

            composable(
                "reader/{idx}",
                arguments = listOf(navArgument("idx") { type = NavType.IntType })
            ) { back ->
                ReaderScreen(
                    chapterIndex = back.arguments?.getInt("idx") ?: 0,
                    onBack       = { nav.popBackStack() },
                    onOpenIndex  = {
                        nav.navigate("index") { popUpTo("index") { inclusive = true } }
                    },
                    onThemeCycle = onThemeCycle
                )
            }
        }
    }
}
