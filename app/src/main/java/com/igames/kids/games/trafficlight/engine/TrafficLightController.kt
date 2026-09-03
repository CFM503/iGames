package com.igames.kids.games.trafficlight.engine

import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightState
import com.igames.kids.games.trafficlight.model.TrafficLightStyle
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
            // If switched to pedestrian mode while in yellow, immediately switch to red
            if (value.style == TrafficLightStyle.PEDESTRIAN && _currentState.value == TrafficLightState.YELLOW) {
                _currentState.value = TrafficLightState.RED
                _remainingSeconds.value = value.redDuration
                onStateChanged?.invoke(TrafficLightState.RED)
                return
            }
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

    // Always start with RED light
    private val _currentState = MutableStateFlow(TrafficLightState.RED)
    val currentState: StateFlow<TrafficLightState> = _currentState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(initialConfig.redDuration)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isBlinkPhaseVisible = MutableStateFlow(true)
    val isBlinkPhaseVisible: StateFlow<Boolean> = _isBlinkPhaseVisible.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isManualMode = MutableStateFlow(false)
    val isManualMode: StateFlow<Boolean> = _isManualMode.asStateFlow()

    private var loopJob: Job? = null

    fun start(forceRed: Boolean = true) {
        if (loopJob?.isActive == true) {
            if (forceRed) {
                resetToRed()
            }
            return
        }
        _isPaused.value = false
        if (forceRed) {
            _isManualMode.value = false
            _currentState.value = TrafficLightState.RED
            _remainingSeconds.value = config.redDuration
            _isBlinkPhaseVisible.value = true
        }
        // Trigger initial state broadcast so voice alert plays immediately on start!
        onStateChanged?.invoke(_currentState.value)
        loopJob = scope.launch {
            runLoop()
        }
    }

    fun resetToRed() {
        _isManualMode.value = false
        _currentState.value = TrafficLightState.RED
        _remainingSeconds.value = config.redDuration
        _isBlinkPhaseVisible.value = true
        onStateChanged?.invoke(TrafficLightState.RED)
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

    /**
     * Cycles to the next light in manual mode upon screen click:
     * - In pedestrian mode: RED <-> GREEN (no yellow light)
     * - In vehicle mode: RED -> GREEN -> YELLOW -> RED
     */
    fun cycleNextManualState() {
        _isManualMode.value = true
        val nextState = when (config.style) {
            TrafficLightStyle.PEDESTRIAN -> {
                if (_currentState.value == TrafficLightState.RED) {
                    TrafficLightState.GREEN
                } else {
                    TrafficLightState.RED
                }
            }
            else -> {
                when (_currentState.value) {
                    TrafficLightState.RED -> TrafficLightState.GREEN
                    TrafficLightState.GREEN, TrafficLightState.GREEN_BLINK -> TrafficLightState.YELLOW
                    TrafficLightState.YELLOW -> TrafficLightState.RED
                }
            }
        }
        _currentState.value = nextState
        _remainingSeconds.value = when (nextState) {
            TrafficLightState.RED -> config.redDuration
            TrafficLightState.YELLOW -> config.yellowDuration
            TrafficLightState.GREEN, TrafficLightState.GREEN_BLINK -> config.greenDuration
        }
        _isBlinkPhaseVisible.value = true
        onStateChanged?.invoke(nextState)
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
        val isPedestrian = (config.style == TrafficLightStyle.PEDESTRIAN)

        when (_currentState.value) {
            TrafficLightState.GREEN, TrafficLightState.GREEN_BLINK -> {
                if (isPedestrian) {
                    // Pedestrian mode has no yellow light: transitions directly Green -> Red
                    _currentState.value = TrafficLightState.RED
                    _remainingSeconds.value = config.redDuration
                    onStateChanged?.invoke(TrafficLightState.RED)
                } else {
                    // Motor vehicle mode: transitions Green -> Yellow -> Red
                    _currentState.value = TrafficLightState.YELLOW
                    _remainingSeconds.value = config.yellowDuration
                    onStateChanged?.invoke(TrafficLightState.YELLOW)
                }
            }
            TrafficLightState.YELLOW -> {
                _currentState.value = TrafficLightState.RED
                _remainingSeconds.value = config.redDuration
                onStateChanged?.invoke(TrafficLightState.RED)
            }
            TrafficLightState.RED -> {
                // Red -> Green (both vehicle and pedestrian modes go directly Red -> Green)
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
