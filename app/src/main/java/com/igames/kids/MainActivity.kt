package com.igames.kids

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.igames.kids.core.audio.SoundManager
import com.igames.kids.core.preferences.TrafficLightPreferences
import com.igames.kids.core.theme.IGamesTheme
import com.igames.kids.core.theme.KidBackground
import com.igames.kids.core.update.UpdateChannel
import com.igames.kids.core.update.UpdateManager
import com.igames.kids.games.trafficlight.interactive.CrossRoadGameScreen
import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.settings.TrafficLightSettingsScreen
import com.igames.kids.games.trafficlight.ui.TrafficLightScreen
import com.igames.kids.navigation.HubScreen
import com.igames.kids.navigation.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundManager = SoundManager(this)
        updateManager = UpdateManager(this)
        val preferences = TrafficLightPreferences(this)

        setContent {
            IGamesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KidBackground
                ) {
                    val navController = rememberNavController()
                    val coroutineScope = rememberCoroutineScope()
                    val config by preferences.configFlow.collectAsState(initial = TrafficLightConfig())
                    val updateChannel by preferences.updateChannelFlow.collectAsState(initial = UpdateChannel.AUTO)
                    val customProxy by preferences.customProxyFlow.collectAsState(initial = "https://ghproxy.net/")

                    // Synchronize acceleration preferences
                    LaunchedEffect(updateChannel, customProxy) {
                        updateManager.currentChannel = updateChannel
                        updateManager.customProxyPrefix = customProxy
                    }

                    // Silently check for updates via jsDelivr CDN on startup
                    LaunchedEffect(Unit) {
                        updateManager.checkForUpdates(isManualCheck = false)
                    }

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Hub.route
                    ) {
                        // 1. Hub Screen
                        composable(Screen.Hub.route) {
                            HubScreen(
                                soundManager = soundManager,
                                updateManager = updateManager,
                                onOpenTrafficLight = {
                                    navController.navigate(Screen.TrafficLightSim.route)
                                },
                                onOpenSettings = {
                                    navController.navigate(Screen.TrafficLightSettings.route)
                                }
                            )
                        }

                        // 2. Traffic Light Simulation Screen
                        composable(Screen.TrafficLightSim.route) {
                            TrafficLightScreen(
                                soundManager = soundManager,
                                currentConfig = config,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToSettings = {
                                    navController.navigate(Screen.TrafficLightSettings.route)
                                },
                                onNavigateToGame = {
                                    navController.navigate(Screen.TrafficLightGame.route)
                                },
                                onStyleChange = { newStyle ->
                                    coroutineScope.launch {
                                        preferences.updateConfig(config.copy(style = newStyle))
                                    }
                                }
                            )
                        }

                        // 3. Interactive Cross-Road Game
                        composable(Screen.TrafficLightGame.route) {
                            CrossRoadGameScreen(
                                soundManager = soundManager,
                                config = config,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 4. Parental Settings Screen
                        composable(Screen.TrafficLightSettings.route) {
                            TrafficLightSettingsScreen(
                                currentConfig = config,
                                soundManager = soundManager,
                                updateManager = updateManager,
                                initialChannel = updateChannel,
                                initialCustomProxy = customProxy,
                                onSaveChannel = { channel, proxy ->
                                    coroutineScope.launch {
                                        preferences.setUpdateChannel(channel)
                                        preferences.setCustomProxy(proxy)
                                    }
                                },
                                onSaveConfig = { newConfig ->
                                    coroutineScope.launch {
                                        preferences.updateConfig(newConfig)
                                    }
                                },
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
