package com.whitestone.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Today : Screen("today")
    data object Review : Screen("review")
    data object Reflections : Screen("reflections")
    data object About : Screen("about")
    data object DayDetail : Screen("day_detail/{dayKey}") {
        fun createRoute(dayKey: String) = "day_detail/$dayKey"
    }
    data object StoneDetail : Screen("stone_detail/{stoneId}") {
        fun createRoute(stoneId: Long) = "stone_detail/$stoneId"
    }
}
