package com.rajarajanreader.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rajarajanreader.app.domain.ReadingTheme
import com.rajarajanreader.app.presentation.screen.index.IndexScreen
import com.rajarajanreader.app.presentation.screen.reader.ReaderScreen
import com.rajarajanreader.app.presentation.screen.search.SearchScreen
import com.rajarajanreader.app.presentation.screen.splash.SplashScreen
import com.rajarajanreader.app.presentation.theme.TamilReaderTheme

@Composable
fun AppNavigation(theme: ReadingTheme, onThemeCycle: () -> Unit) {
    val nav = rememberNavController()

    TamilReaderTheme(theme) {
        NavHost(nav, startDestination = "splash") {

            composable("splash") {
                SplashScreen(onStart = {
                    nav.navigate("index") { popUpTo("splash") { inclusive = true } }
                })
            }

            composable("index") {
                IndexScreen(
                    onChapterClick = { nav.navigate("reader/$it") },
                    onThemeCycle   = onThemeCycle,
                    onOpenSearch   = { nav.navigate("search") }
                )
            }

            composable("search") {
                SearchScreen(
                    onBack         = { nav.popBackStack() },
                    onChapterClick = { idx ->
                        nav.navigate("reader/$idx") {
                            popUpTo("index")
                        }
                    }
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
                    onOpenSearch = { nav.navigate("search") },
                    onThemeCycle = onThemeCycle
                )
            }
        }
    }
}
