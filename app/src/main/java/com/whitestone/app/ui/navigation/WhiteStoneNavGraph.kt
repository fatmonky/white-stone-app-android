package com.whitestone.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.whitestone.app.data.OnboardingStep
import com.whitestone.app.ui.about.AboutScreen
import com.whitestone.app.ui.daydetail.DayDetailScreen
import com.whitestone.app.ui.onboarding.FirstStoneSuccessSheet
import com.whitestone.app.ui.onboarding.OnboardingViewModel
import com.whitestone.app.ui.onboarding.WelcomeOnboardingSheet
import com.whitestone.app.ui.reflection.ReflectionDetailScreen
import com.whitestone.app.ui.reflection.ReflectionScreen
import com.whitestone.app.ui.review.ReviewScreen
import com.whitestone.app.ui.splash.SplashScreen
import com.whitestone.app.ui.stonedetail.StoneDetailScreen
import com.whitestone.app.ui.today.TodayScreen

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val bottomNavItems = listOf(
    BottomNavItem("Today", Icons.Filled.Circle, Screen.Today.route),
    BottomNavItem("Review", Icons.Filled.CalendarMonth, Screen.Review.route),
    BottomNavItem("Reflections", Icons.AutoMirrored.Filled.MenuBook, Screen.Reflections.route),
    BottomNavItem("About", Icons.Filled.Info, Screen.About.route),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhiteStoneNavGraph(
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onboardingState by onboardingViewModel.uiState.collectAsState()

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    LaunchedEffect(onboardingState.step, currentRoute) {
        when (onboardingState.step) {
            OnboardingStep.REVIEW_TOUR -> {
                if (currentRoute != Screen.Review.route) {
                    navController.navigate(Screen.Review.route) {
                        launchSingleTop = true
                    }
                }
            }
            OnboardingStep.REFLECTIONS_TOUR -> {
                if (currentRoute != Screen.Reflections.route) {
                    navController.navigate(Screen.Reflections.route) {
                        launchSingleTop = true
                    }
                }
            }
            else -> Unit
        }
    }

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
                    },
                    showOnboardingCoach = onboardingState.step == OnboardingStep.TODAY_COACH &&
                        currentRoute == Screen.Today.route,
                    onCompleteCoach = onboardingViewModel::completeTodayCoach,
                    onDismissCoach = onboardingViewModel::dismissOnboarding,
                    onStoneSaved = onboardingViewModel::onStoneSaved
                )
            }
            composable(Screen.Review.route) {
                ReviewScreen(
                    onNavigateToStoneDetail = { stoneId ->
                        navController.navigate(Screen.StoneDetail.createRoute(stoneId))
                    },
                    onNavigateToReflectionDetail = { dayKey ->
                        navController.navigate(Screen.ReflectionDetail.createRoute(dayKey))
                    },
                    showTourOverlay = onboardingState.step == OnboardingStep.REVIEW_TOUR &&
                        currentRoute == Screen.Review.route,
                    onContinueToReflections = onboardingViewModel::continueToReflections,
                    onSkipTour = onboardingViewModel::dismissOnboarding
                )
            }
            composable(Screen.Reflections.route) {
                ReflectionScreen(
                    onNavigateToReflectionDetail = { dayKey ->
                        navController.navigate(Screen.ReflectionDetail.createRoute(dayKey))
                    },
                    showTourOverlay = onboardingState.step == OnboardingStep.REFLECTIONS_TOUR &&
                        currentRoute == Screen.Reflections.route,
                    onFinishTour = onboardingViewModel::dismissOnboarding,
                    onSkipTour = onboardingViewModel::dismissOnboarding
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
                    onNavigateToReflectionDetail = {
                        navController.navigate(Screen.ReflectionDetail.createRoute(dayKey))
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.ReflectionDetail.route,
                arguments = listOf(navArgument("dayKey") { type = NavType.StringType })
            ) { backStackEntry ->
                val dayKey = backStackEntry.arguments?.getString("dayKey") ?: return@composable
                ReflectionDetailScreen(
                    dayKey = dayKey,
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

    if (onboardingState.step == OnboardingStep.WELCOME && currentRoute != Screen.Splash.route) {
        WelcomeOnboardingSheet(
            onStartTour = {
                onboardingViewModel.startTour()
                if (currentRoute != Screen.Today.route) {
                    navController.navigate(Screen.Today.route) {
                        launchSingleTop = true
                    }
                }
            },
            onSkip = onboardingViewModel::dismissOnboarding
        )
    }

    if (onboardingState.showPostFirstEntry) {
        FirstStoneSuccessSheet(
            onContinueTour = onboardingViewModel::continueToReview,
            onFinishWithoutTour = onboardingViewModel::dismissOnboarding
        )
    }
}
