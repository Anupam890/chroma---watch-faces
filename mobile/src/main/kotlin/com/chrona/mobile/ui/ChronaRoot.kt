package com.chrona.mobile.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chrona.mobile.sync.WatchConnection
import com.chrona.mobile.ui.screens.DiscoverScreen
import com.chrona.mobile.ui.screens.FaceDetailScreen
import com.chrona.mobile.ui.screens.FavouritesScreen
import com.chrona.mobile.ui.screens.MyWatchScreen
import com.chrona.mobile.ui.screens.SettingsScreen
import com.chrona.mobile.ui.theme.ChronaTheme

sealed class Dest(val route: String, val label: String) {
    data object Discover : Dest("discover", "Discover")
    data object MyWatch : Dest("my_watch", "My Watch")
    data object Favourites : Dest("favourites", "Favourites")
    data object Settings : Dest("settings", "Settings")
    data object Detail : Dest("face/{id}", "Face") { fun of(id: String) = "face/$id" }
}

@Composable
fun ChronaRoot(startFaceId: String? = null, vm: ChronaViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        startFaceId?.let { nav.navigate(Dest.Detail.of(it)) }
        vm.events.collect { e ->
            when (e) {
                is WatchConnection.SyncEvent.Installed -> snack.showSnackbar("Installed ${vm.state.value.faces.firstOrNull { it.id == e.faceId }?.name ?: "watch face"} ✓")
                is WatchConnection.SyncEvent.Failed -> snack.showSnackbar(e.reason)
            }
        }
    }

    ChronaTheme(state.prefs.themeMode) {
        val backStack by nav.currentBackStackEntryAsState()
        val tabs = listOf(Dest.Discover to Icons.Filled.Explore, Dest.MyWatch to Icons.Filled.Watch, Dest.Favourites to Icons.Filled.Favorite, Dest.Settings to Icons.Filled.Settings)
        val showBar = tabs.any { (d, _) -> backStack?.destination?.hierarchy?.any { it.route == d.route } == true }

        Scaffold(
            snackbarHost = { SnackbarHost(snack) },
            bottomBar = {
                if (showBar) NavigationBar {
                    tabs.forEach { (d, icon) ->
                        val selected = backStack?.destination?.hierarchy?.any { it.route == d.route } == true
                        NavigationBarItem(selected, onClick = { nav.navigate(d.route) { popUpTo(Dest.Discover.route) { saveState = true }; launchSingleTop = true; restoreState = true } },
                            icon = { Icon(icon, d.label) }, label = { Text(d.label) })
                    }
                }
            }
        ) { pad ->
            NavHost(nav, Dest.Discover.route, Modifier.padding(pad)) {
                composable(Dest.Discover.route) { DiscoverScreen(state, vm, onOpen = { nav.navigate(Dest.Detail.of(it)) }) }
                composable(Dest.MyWatch.route) { MyWatchScreen(state, vm, onOpen = { nav.navigate(Dest.Detail.of(it)) }) }
                composable(Dest.Favourites.route) { FavouritesScreen(state, vm, onOpen = { nav.navigate(Dest.Detail.of(it)) }) }
                composable(Dest.Settings.route) { SettingsScreen(state, vm) }
                composable(Dest.Detail.route, listOf(navArgument("id") { })) { entry ->
                    val id = entry.arguments?.getString("id") ?: return@composable
                    FaceDetailScreen(id, state, vm, onBack = { nav.popBackStack() })
                }
            }
        }
    }
}
