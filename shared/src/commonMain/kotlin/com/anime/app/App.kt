package com.anime.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.anime.app.data.ContentRepository
import com.anime.app.data.FakeContentRepository
import com.anime.app.data.SettingsSocialRepository
import com.anime.app.data.SettingsWatchProgressRepository
import com.anime.app.data.SocialRepository
import com.anime.app.data.WatchProgressRepository
import com.anime.app.navigation.CreatorStudioRoute
import com.anime.app.navigation.EpisodeEditorRoute
import com.anime.app.navigation.ComponentGalleryRoute
import com.anime.app.navigation.FilmDetailRoute
import com.anime.app.navigation.FilmEditorRoute
import com.anime.app.navigation.HomeRoute
import com.anime.app.navigation.PlayerRoute
import com.anime.app.navigation.SeriesDetailRoute
import com.anime.app.navigation.SeriesEditorRoute
import com.anime.app.navigation.WelcomeRoute
import com.anime.app.theme.AnimeAppTheme
import com.anime.app.theme.ThemeController
import com.anime.app.theme.ThemeTextureOverlay
import com.anime.app.theme.rememberReducedMotionPreference
import com.anime.app.ui.admin.EpisodeEditorScreen
import com.anime.app.ui.admin.EpisodeEditorViewModel
import com.anime.app.ui.admin.FilmEditorScreen
import com.anime.app.ui.admin.FilmEditorViewModel
import com.anime.app.ui.admin.SeriesEditorScreen
import com.anime.app.ui.admin.SeriesEditorViewModel
import com.anime.app.ui.admin.rememberMediaInspector
import com.anime.app.ui.debug.ComponentGalleryScreen
import com.anime.app.ui.film.FilmDetailScreen
import com.anime.app.ui.film.FilmDetailViewModel
import com.anime.app.ui.home.HomeScreen
import com.anime.app.ui.home.HomeViewModel
import com.anime.app.ui.player.PlayerScreen
import com.anime.app.ui.player.PlayerViewModel
import com.anime.app.ui.series.SeriesDetailScreen
import com.anime.app.ui.series.SeriesDetailViewModel
import com.anime.app.ui.studio.CreatorStudioScreen
import com.anime.app.ui.studio.CreatorStudioViewModel
import com.anime.app.ui.welcome.WelcomeScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

    val themeController = remember { ThemeController() }
    val direction by themeController.direction.collectAsStateWithLifecycle()
    val reducedMotion = rememberReducedMotionPreference()

    AnimeAppTheme(direction = direction, reducedMotion = reducedMotion) {
        val repository = rememberContentRepository()
        val progressRepository = remember<WatchProgressRepository> { SettingsWatchProgressRepository() }
        val socialRepository = remember<SocialRepository> { SettingsSocialRepository() }

        Box(modifier = Modifier.fillMaxSize()) {
            Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()

            SharedTransitionLayout {
                NavHost(
                    navController = navController,
                    startDestination = WelcomeRoute,
                    modifier = Modifier.fillMaxSize(),
                ) {
                composable<WelcomeRoute> {
                    SafeArea {
                        WelcomeScreen(
                            onEnter = {
                                navController.navigate(HomeRoute) {
                                    popUpTo(WelcomeRoute) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
                composable<HomeRoute> {
                    val viewModel = viewModel { HomeViewModel(repository, progressRepository, socialRepository) }
                    HomeScreen(
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onSeriesClick = { series ->
                                navController.navigate(SeriesDetailRoute(seriesId = series.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onFilmClick = { film ->
                                navController.navigate(FilmDetailRoute(filmId = film.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onPlayFilm = { film ->
                                navController.navigate(PlayerRoute(filmId = film.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenStudio = {
                                navController.navigate(CreatorStudioRoute) { launchSingleTop = true }
                            },
                            onOpenDesignLab = {
                                navController.navigate(ComponentGalleryRoute) { launchSingleTop = true }
                            },
                    )
                }
                composable<ComponentGalleryRoute> {
                    SafeArea {
                        ComponentGalleryScreen(
                            direction = direction,
                            onDirectionSelected = themeController::select,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable<SeriesDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<SeriesDetailRoute>()
                    val viewModel = viewModel(key = route.seriesId) {
                        SeriesDetailViewModel(repository, progressRepository, socialRepository, route.seriesId)
                    }
                    SafeArea {
                        SeriesDetailScreen(
                            viewModel = viewModel,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedVisibilityScope = this,
                            onBack = { navController.popBackStack() },
                            onFilmClick = { film ->
                                navController.navigate(PlayerRoute(filmId = film.id)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
                composable<CreatorStudioRoute> {
                    val viewModel = viewModel { CreatorStudioViewModel(repository) }
                    SafeArea {
                        CreatorStudioScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNewFilm = {
                                navController.navigate(FilmEditorRoute()) { launchSingleTop = true }
                            },
                            onNewSeries = {
                                navController.navigate(SeriesEditorRoute()) { launchSingleTop = true }
                            },
                            onEditSeries = { series ->
                                navController.navigate(SeriesEditorRoute(seriesId = series.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onAddEpisode = { series ->
                                navController.navigate(EpisodeEditorRoute(seriesId = series.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onEditFilm = { film ->
                                navController.navigate(FilmEditorRoute(filmId = film.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenSeries = { series ->
                                navController.navigate(SeriesDetailRoute(seriesId = series.id)) {
                                    launchSingleTop = true
                                }
                            },
                            onOpenFilm = { film ->
                                navController.navigate(FilmDetailRoute(filmId = film.id)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
                composable<SeriesEditorRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<SeriesEditorRoute>()
                    val viewModel = viewModel(key = "series-editor-${route.seriesId}") {
                        SeriesEditorViewModel(repository, route.seriesId)
                    }
                    SafeArea {
                        SeriesEditorScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onSaved = { _, _ -> navController.popBackStack() },
                        )
                    }
                }
                composable<EpisodeEditorRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<EpisodeEditorRoute>()
                    val mediaInspector = rememberMediaInspector()
                    val viewModel = viewModel(key = "episode-editor-${route.episodeId}") {
                        EpisodeEditorViewModel(repository, mediaInspector, route.seriesId, route.episodeId)
                    }
                    SafeArea {
                        EpisodeEditorScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onSaved = { navController.popBackStack() },
                        )
                    }
                }
                composable<FilmEditorRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<FilmEditorRoute>()
                    val mediaInspector = rememberMediaInspector()
                    val viewModel = viewModel(key = "film-editor-${route.filmId}") {
                        FilmEditorViewModel(repository, mediaInspector, route.filmId)
                    }
                    SafeArea {
                        FilmEditorScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onSaved = { navController.popBackStack() },
                        )
                    }
                }
                composable<FilmDetailRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<FilmDetailRoute>()
                    val viewModel = viewModel(key = route.filmId) {
                        FilmDetailViewModel(repository, socialRepository, route.filmId)
                    }
                    SafeArea {
                        FilmDetailScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onPlay = { film ->
                                navController.navigate(PlayerRoute(filmId = film.id)) {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }
                }
                // Handles its own insets so it can go edge to edge in fullscreen.
                composable<PlayerRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<PlayerRoute>()
                    val viewModel = viewModel(key = route.filmId) {
                        PlayerViewModel(repository, progressRepository, route.filmId)
                    }
                    PlayerScreen(
                        viewModel = viewModel,
                        onBack = {
                            val player = PlayerRoute(filmId = route.filmId)
                            if (!navController.popBackStack(player, inclusive = true)) {
                                navController.popBackStack()
                            }
                        },
                    )
                }
            }
            }
            }
            ThemeTextureOverlay()
        }
    }
}

@Composable
private fun SafeArea(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        content()
    }
}

@Composable
private fun rememberContentRepository(): ContentRepository =
    remember { FakeContentRepository() }
