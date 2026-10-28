package com.example.safetile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.safetile.ui.screens.AboutScreen
import com.example.safetile.ui.screens.GameScreen
import com.example.safetile.ui.screens.HomeScreen
import com.example.safetile.ui.screens.ResultScreen
import com.example.safetile.ui.screens.SettingsScreen
import com.example.safetile.ui.screens.SplashScreen
import com.example.safetile.viewmodel.SafeTileViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: SafeTileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val settings by viewModel.settings.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                selectedDifficulty = uiState.difficulty,
                statistics = statistics,
                onDifficultySelected = { viewModel.selectDifficulty(it) },
                onPlayClicked = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClicked = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClicked = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                uiState = uiState,
                reducedMotion = settings.reducedMotion,
                onTileClick = { viewModel.onTileClick(it) },
                onPauseClick = { viewModel.pauseGame() },
                onResumeClick = { viewModel.resumeGame() },
                onRestartClick = { viewModel.restartGame() },
                onExitClick = {
                    viewModel.exitToHome()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onGameOverNavigate = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                result = uiState.lastResult,
                statistics = statistics,
                isTimeout = uiState.isTimeout,
                onTryAgainClicked = {
                    viewModel.startGame()
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClicked = {
                    viewModel.exitToHome()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                settings = settings,
                onThemeChanged = { viewModel.setThemeMode(it) },
                onSoundToggled = { viewModel.setSoundEnabled(it) },
                onHapticsToggled = { viewModel.setHapticsEnabled(it) },
                onReducedMotionToggled = { viewModel.setReducedMotion(it) },
                onResetStatistics = { viewModel.resetStatistics() },
                onBackClicked = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
