package com.igames.kids.games.trafficlight.model

data class TrafficLightConfig(
    val redDuration: Int = 10,       // Seconds
    val yellowDuration: Int = 3,     // Seconds
    val greenDuration: Int = 10,     // Seconds
    val style: TrafficLightStyle = TrafficLightStyle.CLASSIC_3_LAMP,
    val isSoundEnabled: Boolean = true,
    val isVoiceEnabled: Boolean = true,
    val isGreenBlinkEnabled: Boolean = true,
    val isTickSoundEnabled: Boolean = true
) {
    val totalCycleDuration: Int
        get() = redDuration + yellowDuration + greenDuration
}
