package com.igames.kids.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.igames.kids.core.update.UpdateChannel
import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "traffic_light_settings")

class TrafficLightPreferences(private val context: Context) {

    companion object {
        val KEY_RED_SECONDS = intPreferencesKey("red_seconds")
        val KEY_YELLOW_SECONDS = intPreferencesKey("yellow_seconds")
        val KEY_GREEN_SECONDS = intPreferencesKey("green_seconds")
        val KEY_STYLE = stringPreferencesKey("light_style")
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val KEY_BLINK_GREEN = booleanPreferencesKey("blink_green")
        val KEY_TICK_SOUND = booleanPreferencesKey("tick_sound")
        val KEY_UPDATE_CHANNEL = stringPreferencesKey("update_channel")
        val KEY_CUSTOM_PROXY = stringPreferencesKey("custom_proxy")
    }

    val configFlow: Flow<TrafficLightConfig> = context.dataStore.data.map { prefs ->
        val redSec = prefs[KEY_RED_SECONDS] ?: 10
        val yellowSec = prefs[KEY_YELLOW_SECONDS] ?: 3
        val greenSec = prefs[KEY_GREEN_SECONDS] ?: 10
        val styleName = prefs[KEY_STYLE] ?: TrafficLightStyle.CLASSIC_3_LAMP.name
        val style = try {
            TrafficLightStyle.valueOf(styleName)
        } catch (e: Exception) {
            TrafficLightStyle.CLASSIC_3_LAMP
        }
        val sound = prefs[KEY_SOUND_ENABLED] ?: true
        val voice = prefs[KEY_VOICE_ENABLED] ?: true
        val blinkGreen = prefs[KEY_BLINK_GREEN] ?: true
        val tickSound = prefs[KEY_TICK_SOUND] ?: true

        TrafficLightConfig(
            redDuration = redSec,
            yellowDuration = yellowSec,
            greenDuration = greenSec,
            style = style,
            isSoundEnabled = sound,
            isVoiceEnabled = voice,
            isGreenBlinkEnabled = blinkGreen,
            isTickSoundEnabled = tickSound
        )
    }

    val updateChannelFlow: Flow<UpdateChannel> = context.dataStore.data.map { prefs ->
        val channelName = prefs[KEY_UPDATE_CHANNEL] ?: UpdateChannel.AUTO.name
        try {
            UpdateChannel.valueOf(channelName)
        } catch (e: Exception) {
            UpdateChannel.AUTO
        }
    }

    val customProxyFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_PROXY] ?: "https://ghproxy.net/"
    }

    suspend fun updateConfig(config: TrafficLightConfig) {
        context.dataStore.edit { prefs ->
            prefs[KEY_RED_SECONDS] = config.redDuration
            prefs[KEY_YELLOW_SECONDS] = config.yellowDuration
            prefs[KEY_GREEN_SECONDS] = config.greenDuration
            prefs[KEY_STYLE] = config.style.name
            prefs[KEY_SOUND_ENABLED] = config.isSoundEnabled
            prefs[KEY_VOICE_ENABLED] = config.isVoiceEnabled
            prefs[KEY_BLINK_GREEN] = config.isGreenBlinkEnabled
            prefs[KEY_TICK_SOUND] = config.isTickSoundEnabled
        }
    }

    suspend fun setUpdateChannel(channel: UpdateChannel) {
        context.dataStore.edit { prefs ->
            prefs[KEY_UPDATE_CHANNEL] = channel.name
        }
    }

    suspend fun setCustomProxy(prefix: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_PROXY] = prefix
        }
    }
}
