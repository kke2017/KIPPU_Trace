package com.kippu.trace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kippu.trace.ui.screens.HomeScreen
import com.kippu.trace.ui.theme.KIPPU_TraceTheme
import com.kippu.trace.viewmodel.EventViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KIPPU_TraceTheme {
                MainApp()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "日子", Icons.Default.DateRange)
    object Detail : Screen("detail/{eventId}", "详情", Icons.Default.Info) {
        fun createRoute(eventId: Long) = "detail/$eventId"
    }
    object Settings : Screen("settings", "我的", Icons.Default.Settings)
    object Editor : Screen("editor", "编辑", Icons.Default.Add)
}

@Composable
fun MainApp(eventViewModel: EventViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val events by eventViewModel.allEvents.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom bar on main screens
            if (currentDestination?.route == Screen.Home.route || currentDestination?.route == Screen.Settings.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val items = listOf(Screen.Home, Screen.Settings)
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    NavigationBarItem(
                        icon = { Icon(Screen.Detail.icon, contentDescription = Screen.Detail.label) },
                        label = { Text(Screen.Detail.label) },
                        selected = currentDestination?.route?.startsWith("detail") == true,
                        onClick = {
                            if (events.isNotEmpty()) {
                                navController.navigate(Screen.Detail.createRoute(events.first().id))
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(400)) }
        ) {
            composable(route = Screen.Home.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    HomeScreen(
                        events = events,
                        onAddClick = { navController.navigate(Screen.Editor.route) },
                        onEventClick = { event -> 
                            navController.navigate(Screen.Detail.createRoute(event.id))
                        }
                    )
                }
            }
            composable(
                route = Screen.Editor.route,
                enterTransition = { slideInVertically(initialOffsetY = { it }) + fadeIn() },
                exitTransition = { fadeOut() + slideOutVertically(targetOffsetY = { it }) }
            ) {
                com.kippu.trace.ui.screens.EditorScreen(
                    onDismiss = { navController.popBackStack() },
                    onSave = { newEvent ->
                        eventViewModel.addEvent(newEvent)
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(androidx.navigation.navArgument("eventId") { type = androidx.navigation.NavType.LongType }),
                enterTransition = { fadeIn(tween(500)) },
                exitTransition = { fadeOut(tween(500)) }
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getLong("eventId") ?: 0L
                com.kippu.trace.ui.screens.DetailScreen(
                    events = events,
                    initialEventId = eventId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(route = Screen.Settings.route) {
                Box(modifier = Modifier.padding(innerPadding)) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Text(text = "设置", modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    KIPPU_TraceTheme {
        MainApp()
    }
}
