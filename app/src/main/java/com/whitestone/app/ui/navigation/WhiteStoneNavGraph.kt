package com.whitestone.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.whitestone.app.ui.about.AboutScreen
import com.whitestone.app.ui.calendar.CalendarScreen
import com.whitestone.app.ui.daydetail.DayDetailScreen
import com.whitestone.app.ui.splash.SplashScreen
import com.whitestone.app.ui.stonedetail.StoneDetailScreen
import com.whitestone.app.ui.today.TodayScreen
import com.whitestone.app.ui.trends.TrendsScreen

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val bottomNavItems = listOf(
    BottomNavItem("Today", Icons.Filled.Circle, Screen.Today.route),
    BottomNavItem("Calendar", Icons.Filled.CalendarMonth, Screen.Calendar.route),
    BottomNavItem("Trends", Icons.Filled.ShowChart, Screen.Trends.route),
    BottomNavItem("About", Icons.Filled.Info, Screen.About.route),
)

@Composable
fun WhiteStoneNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy?.any {
                            it.route == item.route
                        } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onFinished = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Today.route) {
                TodayScreen(
                    onNavigateToStoneDetail = { stoneId ->
                        navController.navigate(Screen.StoneDetail.createRoute(stoneId))
                    }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onNavigateToStoneDetail = { stoneId ->
                        navController.navigate(Screen.StoneDetail.createRoute(stoneId))
                    }
                )
            }
            composable(Screen.Trends.route) {
                TrendsScreen(
                    onNavigateToStoneDetail = { stoneId ->
                        navController.navigate(Screen.StoneDetail.createRoute(stoneId))
                    }
                )
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
            composable(
                route = Screen.DayDetail.route,
                arguments = listOf(navArgument("dayKey") { type = NavType.StringType })
            ) { backStackEntry ->
                val dayKey = backStackEntry.arguments?.getString("dayKey") ?: return@composable
                DayDetailScreen(
                    dayKey = dayKey,
                    onNavigateToStoneDetail = { stoneId ->
                        navController.navigate(Screen.StoneDetail.createRoute(stoneId))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.StoneDetail.route,
                arguments = listOf(navArgument("stoneId") { type = NavType.LongType })
            ) { backStackEntry ->
                val stoneId = backStackEntry.arguments?.getLong("stoneId") ?: return@composable
                StoneDetailScreen(
                    stoneId = stoneId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
