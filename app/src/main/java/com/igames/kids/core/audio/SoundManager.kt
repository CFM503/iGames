package com.igames.kids.core.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private var toneGenerator: ToneGenerator? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    var isSoundEffectsEnabled = true
    var isVoiceEnabled = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to init ToneGenerator", e)
        }

        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.CHINESE)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.getDefault())
                    }
                    // Child-friendly cheerful voice pitch
                    tts?.setPitch(1.25f)
                    tts?.setSpeechRate(0.95f)
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to init TTS", e)
        }
    }

    fun playTick() {
        if (!isSoundEffectsEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playPedestrianBeep() {
        if (!isSoundEffectsEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_PIP, 80)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun playGreenAlert() {
        if (isSoundEffectsEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 150)
            } catch (e: Exception) {
                // ignore
            }
        }
        if (isVoiceEnabled) {
            speak("绿灯亮，左右看一看，确认没有车，再通行！")
        }
    }

    fun playYellowAlert() {
        if (isSoundEffectsEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 120)
            } catch (e: Exception) {
                // ignore
            }
        }
        if (isVoiceEnabled) {
            speak("黄灯亮，等一等！")
        }
    }

    fun playRedAlert() {
        if (isSoundEffectsEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            } catch (e: Exception) {
                // ignore
            }
        }
        if (isVoiceEnabled) {
            speak("红灯停，快停下！")
        }
    }

    fun playSuccess() {
        if (isSoundEffectsEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 250)
            } catch (e: Exception) {
                // ignore
            }
        }
        if (isVoiceEnabled) {
            speak("好棒呀！安全通过！")
        }
    }

    fun playWarning() {
        if (isSoundEffectsEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
            } catch (e: Exception) {
                // ignore
            }
        }
        if (isVoiceEnabled) {
            speak("红灯危险，不可以走哦！")
        }
    }

    fun playButtonTap() {
        if (!isSoundEffectsEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun speak(text: String) {
        if (!isVoiceEnabled || !isTtsReady) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_utterance")
        } catch (e: Exception) {
            Log.e("SoundManager", "Error in speech", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
