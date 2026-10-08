package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.OTTBottomNavigation
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MovieDetailsScreen
import com.example.ui.screens.MoviesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SeriesDetailsScreen
import com.example.ui.screens.SeriesScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.TTMovieBoxTheme
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TTMovieBoxTheme {
                val mainViewModel: MainViewModel = viewModel()
                val adminViewModel: AdminViewModel = viewModel()
                TTMovieBoxApp(
                    mainViewModel = mainViewModel,
                    adminViewModel = adminViewModel
                )
            }
        }
    }
}

@Composable
fun TTMovieBoxApp(
    mainViewModel: MainViewModel,
    adminViewModel: AdminViewModel
) {
    val currentScreen by mainViewModel.currentScreen.collectAsState()

    val isTopLevelScreen = currentScreen is Screen.Home ||
            currentScreen is Screen.Movies ||
            currentScreen is Screen.Series ||
            currentScreen is Screen.Search ||
            currentScreen is Screen.Profile

    val isPlayerScreen = currentScreen is Screen.Player

    // Global back handling for top-level tabs
    BackHandler(enabled = !isTopLevelScreen && !isPlayerScreen) {
        mainViewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = if (isPlayerScreen) WindowInsets(0, 0, 0, 0) else WindowInsets.safeDrawing,
        bottomBar = {
            if (isTopLevelScreen) {
                OTTBottomNavigation(
                    currentScreen = currentScreen,
                    onTabSelected = { tabScreen ->
                        mainViewModel.switchTab(tabScreen)
                    },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isPlayerScreen) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
                .background(DarkBackground)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> HomeScreen(viewModel = mainViewModel)
                is Screen.Movies -> MoviesScreen(viewModel = mainViewModel)
                is Screen.Series -> SeriesScreen(viewModel = mainViewModel)
                is Screen.Search -> SearchScreen(viewModel = mainViewModel)
                is Screen.Profile -> ProfileScreen(viewModel = mainViewModel)
                is Screen.Auth -> AuthScreen(viewModel = mainViewModel)
                is Screen.MovieDetails -> MovieDetailsScreen(viewModel = mainViewModel)
                is Screen.SeriesDetails -> SeriesDetailsScreen(viewModel = mainViewModel)
                is Screen.Player -> VideoPlayerScreen(args = screen.args, viewModel = mainViewModel)
                is Screen.Admin -> AdminScreen(adminViewModel = adminViewModel, mainViewModel = mainViewModel)
            }
        }
    }
}
