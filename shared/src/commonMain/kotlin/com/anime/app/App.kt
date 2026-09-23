package com.anime.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.setValue
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

data class Post(val id: Int, val title: String, val description: String, val image: ImageBitmap?)
private const val ROUTE_STORIES = "stories"
private const val ROUTE_UPLOAD = "upload"

    private val AnimeColorScheme = darkColorScheme(
        primary = Color(0xFFFF4081),
        secondary = Color(0xFF7C4DFF),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E)
    )

    @Composable
    fun App() {
    MaterialTheme(colorScheme = AnimeColorScheme) {
        val posts = remember { mutableStateListOf<Post>() }
        val navController = rememberNavController()
        val currentRoute by navController.currentBackStackEntryAsState()
        var nextId by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute?.destination?.route == ROUTE_STORIES,
                        onClick = { navController.navigate(ROUTE_STORIES) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Stories") }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.destination?.route == ROUTE_UPLOAD,
                        onClick = { navController.navigate(ROUTE_UPLOAD) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        label = { Text("Upload") }
                    )
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).safeContentPadding()) {
                NavHost(navController = navController, startDestination = ROUTE_STORIES) {
                    composable(ROUTE_STORIES) {
                        StoriesTab(
                            posts = posts,
                            onDelete = { post -> posts.remove(post) }
                        )
                    }
                        composable(ROUTE_UPLOAD) {
                            UploadTab(
                                onPost = { title, description, image ->
                                    posts.add(0, Post(nextId++, title, description, image))
                                    navController.navigate(ROUTE_STORIES) { launchSingleTop = true }
                                }
                            )
                        }
                }
            }
        }
    }
}