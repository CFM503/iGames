package com.igames.kids.games.trafficlight.engine

import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrafficLightController(
    private val scope: CoroutineScope,
    initialConfig: TrafficLightConfig = TrafficLightConfig(),
    private val onStateChanged: ((TrafficLightState) -> Unit)? = null,
    private val onTick: ((Int, TrafficLightState) -> Unit)? = null
) {
    var config: TrafficLightConfig = initialConfig
        set(value) {
            field = value
            // If current remaining exceeds new duration, clamp it
            val maxCurrent = when (_currentState.value) {
                TrafficLightState.RED -> value.redDuration
                TrafficLightState.YELLOW -> value.yellowDuration
                TrafficLightState.GREEN, TrafficLightState.GREEN_BLINK -> value.greenDuration
            }
            if (_remainingSeconds.value > maxCurrent) {
                _remainingSeconds.value = maxCurrent
            }
        }

    private val _currentState = MutableStateFlow(TrafficLightState.GREEN)
    val currentState: StateFlow<TrafficLightState> = _currentState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(initialConfig.greenDuration)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isBlinkPhaseVisible = MutableStateFlow(true)
    val isBlinkPhaseVisible: StateFlow<Boolean> = _isBlinkPhaseVisible.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isManualMode = MutableStateFlow(false)
    val isManualMode: StateFlow<Boolean> = _isManualMode.asStateFlow()

    private var loopJob: Job? = null

    fun start() {
        if (loopJob?.isActive == true) return
        _isPaused.value = false
        loopJob = scope.launch {
            runLoop()
        }
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
    }

    fun setManualState(state: TrafficLightState) {
        _isManualMode.value = true
        _currentState.value = state
        _remainingSeconds.value = when (state) {
            TrafficLightState.RED -> config.redDuration
            TrafficLightState.YELLOW -> config.yellowDuration
            TrafficLightState.GREEN, TrafficLightState.GREEN_BLINK -> config.greenDuration
        }
        onStateChanged?.invoke(state)
    }

    fun switchToAutoMode() {
        _isManualMode.value = false
    }

    private suspend fun CoroutineScope.runLoop() {
        while (isActive) {
            if (_isPaused.value || _isManualMode.value) {
                delay(100)
                continue
            }

            val state = _currentState.value
            val sec = _remainingSeconds.value

            onTick?.invoke(sec, state)

            // Handle sub-second blink animation if in blink mode
            if (state == TrafficLightState.GREEN_BLINK) {
                for (i in 0 until 2) {
                    _isBlinkPhaseVisible.value = (i % 2 == 0)
                    delay(500)
                    if (_isPaused.value || _isManualMode.value) break
                }
            } else {
                _isBlinkPhaseVisible.value = true
                delay(1000)
            }

            if (_isPaused.value || _isManualMode.value) continue

            val nextSec = sec - 1
            if (nextSec > 0) {
                // Check if we should switch to green blink in last 3 seconds
                if (state == TrafficLightState.GREEN && config.isGreenBlinkEnabled && nextSec <= 3) {
                    _currentState.value = TrafficLightState.GREEN_BLINK
                    onStateChanged?.invoke(TrafficLightState.GREEN_BLINK)
                }
                _remainingSeconds.value = nextSec
            } else {
                // Time up! Transition to next state
                transitionToNextState()
            }
        }
    }

    private fun transitionToNextState() {
        when (_currentState.value) {
            TrafficLightState.GREEN, TrafficLightState.GREEN_BLINK -> {
                _currentState.value = TrafficLightState.YELLOW
                _remainingSeconds.value = config.yellowDuration
                onStateChanged?.invoke(TrafficLightState.YELLOW)
            }
            TrafficLightState.YELLOW -> {
                _currentState.value = TrafficLightState.RED
                _remainingSeconds.value = config.redDuration
                onStateChanged?.invoke(TrafficLightState.RED)
            }
            TrafficLightState.RED -> {
                _currentState.value = TrafficLightState.GREEN
                _remainingSeconds.value = config.greenDuration
                onStateChanged?.invoke(TrafficLightState.GREEN)
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }
}
