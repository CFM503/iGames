package com.igames.kids.games.trafficlight.model

data class TrafficLightConfig(
    val redDuration: Int = 30,       // Seconds (default 30s)
    val yellowDuration: Int = 3,     // Seconds (default 3s)
    val greenDuration: Int = 30,     // Seconds (default 30s)
    val style: TrafficLightStyle = TrafficLightStyle.CLASSIC_3_LAMP,
    val isSoundEnabled: Boolean = true,
    val isVoiceEnabled: Boolean = true,
    val isGreenBlinkEnabled: Boolean = true,
    val isTickSoundEnabled: Boolean = false
) {
    val totalCycleDuration: Int
        get() = redDuration + yellowDuration + greenDuration
}
