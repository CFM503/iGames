package com.igames.kids.games.trafficlight.model

enum class TrafficLightState {
    RED,
    YELLOW,
    GREEN,
    GREEN_BLINK;

    val isRed: Boolean get() = this == RED
    val isYellow: Boolean get() = this == YELLOW
    val isGreen: Boolean get() = this == GREEN || this == GREEN_BLINK
}
