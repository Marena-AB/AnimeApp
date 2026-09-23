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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

data class Post(val id: Int, val title: String, val description: String, val image: ImageBitmap?)
private const val ROUTE_WELCOME = "welcome"
private const val ROUTE_STORIES = "stories"
private const val ROUTE_UPLOAD = "upload"

private val AnimeDarkColors = darkColorScheme(
    primary = Color(0xFFFF4FA3),
    onPrimary = Color(0xFF1A0011),
    secondary = Color(0xFFFF85C0),
    background = Color(0xFF0D0D0D),
    onBackground = Color(0xFFF5F5F5),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFDDDDDD)
)

@Composable
fun App() {
    MaterialTheme(colorScheme = AnimeDarkColors) {
        val posts = remember { mutableStateListOf<Post>() }
        val navController = rememberNavController()
        val currentRoute by navController.currentBackStackEntryAsState()
        var nextId by remember { mutableIntStateOf(0) }
        var editingPost by remember { mutableStateOf<Post?>(null) }

        Scaffold(
            bottomBar = {
                if (currentRoute?.destination?.route != ROUTE_WELCOME) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute?.destination?.route == ROUTE_STORIES,
                            onClick = { navController.navigate(ROUTE_STORIES) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Stories") }
                        )
                        NavigationBarItem(
                            selected = currentRoute?.destination?.route == ROUTE_UPLOAD,
                            onClick = {
                                editingPost = null
                                navController.navigate(ROUTE_UPLOAD) { launchSingleTop = true }
                            },
                            icon = { Icon(Icons.Default.Add, contentDescription = null) },
                            label = { Text("Upload") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).safeContentPadding()) {
                NavHost(navController = navController, startDestination = ROUTE_WELCOME) {
                    composable(ROUTE_WELCOME) {
                        WelcomeTab(
                            onEnter = { navController.navigate(ROUTE_STORIES) { launchSingleTop = true } }
                        )
                    }
                    composable(ROUTE_STORIES) {
                        StoriesTab(
                            posts = posts,
                            onEdit = { post ->
                                editingPost = post
                                navController.navigate(ROUTE_UPLOAD) { launchSingleTop = true }
                            },
                            onDelete = { post -> posts.remove(post) }
                        )
                    }
                    composable(ROUTE_UPLOAD) {
                        UploadTab(
                            editingPost = editingPost,
                            onSave = { title, description, image ->
                                val current = editingPost
                                if (current != null) {
                                    val index = posts.indexOfFirst { it.id == current.id }
                                    if (index != -1) {
                                        posts[index] = current.copy(
                                            title = title,
                                            description = description,
                                            image = image
                                        )
                                    }
                                } else {
                                    posts.add(0, Post(nextId++, title, description, image))
                                }
                                editingPost = null
                                navController.navigate(ROUTE_STORIES) { launchSingleTop = true }
                            }
                        )
                    }
                }
            }
        }
    }
}