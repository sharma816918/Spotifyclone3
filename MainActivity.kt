package com.example.jiosaavn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jiosaavn.playback.MusicViewModel
import com.example.jiosaavn.ui.components.MiniPlayer
import com.example.jiosaavn.ui.screens.HomeScreen
import com.example.jiosaavn.ui.screens.LoginScreen
import com.example.jiosaavn.ui.screens.PlayerScreen
import com.example.jiosaavn.ui.screens.ProfileScreen
import com.example.jiosaavn.ui.screens.SearchScreen
import com.example.jiosaavn.ui.theme.JiosaavnTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                viewModel.initController(context)
            }
            
            JiosaavnTheme {
                val navController = rememberNavController()
                val currentSong by viewModel.currentSong.collectAsState()
                val isPlaying by viewModel.isPlaying.collectAsState()
                val playbackPosition by viewModel.playbackPosition.collectAsState()
                val duration by viewModel.duration.collectAsState()
                
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val showBottomBar = currentDestination?.route in listOf("home", "search", "profile")
                        if (showBottomBar) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    label = { Text("Search") },
                                    selected = currentDestination?.hierarchy?.any { it.route == "search" } == true,
                                    onClick = {
                                        navController.navigate("search") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") },
                                    selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                                    onClick = {
                                        navController.navigate("profile") {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                })
                            }
                            composable("home") {
                                HomeScreen(
                                    onSongClick = { song ->
                                        viewModel.playSong(context, song)
                                    },
                                    onSearchClick = { navController.navigate("search") }
                                )
                            }
                            composable("search") {
                                SearchScreen(
                                    onSongClick = { song ->
                                        viewModel.playSong(context, song)
                                    },
                                    onBackClick = { navController.popBackStack() }
                                )
                            }
                            composable("profile") {
                                ProfileScreen()
                            }
                            composable("player") {
                                currentSong?.let { song ->
                                    PlayerScreen(
                                        song = song,
                                        isPlaying = isPlaying,
                                        playbackPosition = playbackPosition,
                                        duration = duration,
                                        onBackClick = { navController.popBackStack() },
                                        onTogglePlayPause = { viewModel.togglePlayPause() },
                                        onNext = { viewModel.nextTrack(context) },
                                        onPrevious = { viewModel.previousTrack(context) },
                                        onSeek = { viewModel.seekTo(it) }
                                    )
                                }
                            }
                        }

                        val hideMiniPlayer = currentDestination?.route in listOf("player", "login")
                        if (currentSong != null && !hideMiniPlayer) {
                            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                                MiniPlayer(
                                    song = currentSong!!,
                                    isPlaying = isPlaying,
                                    onTogglePlayPause = { viewModel.togglePlayPause() },
                                    onClick = { navController.navigate("player") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
