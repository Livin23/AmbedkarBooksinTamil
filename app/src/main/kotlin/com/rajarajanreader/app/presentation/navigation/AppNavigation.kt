package com.livin.ambedkarindhiavilsathigal.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.livin.ambedkarindhiavilsathigal.domain.ReadingTheme
import com.livin.ambedkarindhiavilsathigal.presentation.screen.index.IndexScreen
import com.livin.ambedkarindhiavilsathigal.presentation.screen.onboarding.OnboardingScreen
import com.livin.ambedkarindhiavilsathigal.presentation.screen.reader.ReaderScreen
import com.livin.ambedkarindhiavilsathigal.presentation.screen.search.SearchScreen
import com.livin.ambedkarindhiavilsathigal.presentation.screen.splash.SplashScreen
import com.livin.ambedkarindhiavilsathigal.presentation.theme.TamilReaderTheme

@Composable
fun AppNavigation(theme: ReadingTheme, onThemeCycle: () -> Unit) {
    val nav = rememberNavController()

    TamilReaderTheme(theme) {
        NavHost(nav, startDestination = "splash") {

            composable("splash") {
                SplashScreen(onStart = { isFirstLaunch ->
                    val dest = if (isFirstLaunch) "onboarding" else "index"
                    nav.navigate(dest) { popUpTo("splash") { inclusive = true } }
                })
            }

            composable("onboarding") {
                OnboardingScreen(onDone = {
                    nav.navigate("index") { popUpTo("onboarding") { inclusive = true } }
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
