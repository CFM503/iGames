package com.igames.kids.navigation

sealed class Screen(val route: String) {
    data object Hub : Screen("hub")
    data object TrafficLightSim : Screen("traffic_light_sim")
    data object TrafficLightGame : Screen("traffic_light_game")
    data object TrafficLightSettings : Screen("traffic_light_settings")
}
