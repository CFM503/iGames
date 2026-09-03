package com.igames.kids.core.audio

import android.content.Context
import android.media.AudioAttributes
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
    private var pendingSpeech: String? = null

    var isSoundEffectsEnabled = true
    var isVoiceEnabled = true

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 90)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to init ToneGenerator", e)
        }

        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val localesToTry = listOf(
                        Locale.CHINA,
                        Locale.SIMPLIFIED_CHINESE,
                        Locale.CHINESE,
                        Locale.getDefault()
                    )
                    var matched = false
                    for (loc in localesToTry) {
                        val res = tts?.setLanguage(loc)
                        if (res != TextToSpeech.LANG_MISSING_DATA && res != TextToSpeech.LANG_NOT_SUPPORTED) {
                            matched = true
                            Log.i("SoundManager", "TTS language successfully configured to: $loc")
                            break
                        }
                    }
                    if (!matched) {
                        Log.w("SoundManager", "No specific Chinese locale found, falling back to default locale")
                        tts?.setLanguage(Locale.getDefault())
                    }

                    try {
                        val audioAttrs = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                        tts?.setAudioAttributes(audioAttrs)
                    } catch (e: Exception) {
                        Log.w("SoundManager", "Could not set AudioAttributes: ${e.message}")
                    }

                    // Cheerful, friendly voice pitch for kids
                    tts?.setPitch(1.2f)
                    tts?.setSpeechRate(0.92f)
                    isTtsReady = true
                    Log.i("SoundManager", "TextToSpeech engine ready")

                    // Deliver pending speech immediately once ready
                    pendingSpeech?.let { text ->
                        pendingSpeech = null
                        speak(text)
                    }
                } else {
                    Log.e("SoundManager", "TTS initialization failed with status code: $status")
                }
            }
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize TTS", e)
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
            speak("绿灯亮啦！小朋友，先左右看一看，确认没有车，再安全通行哦！")
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
            speak("黄灯亮啦，等一等，千万不要着急哦！")
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
            speak("小朋友，红灯亮啦，快快停下来！做个遵守规则的好宝宝！")
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
            speak("太棒啦！安全通过马路，奖励你一颗闪亮的小星星！")
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
            speak("哎呀，红灯不能闯哦！红灯停绿灯行，安全第一名！")
        }
    }

    fun playManualPoliceSpeech(colorName: String) {
        if (!isVoiceEnabled) return
        when (colorName) {
            "红" -> speak("小交警指挥：红灯亮，车子停，行人止步！")
            "黄" -> speak("小交警指挥：黄灯亮，请减速，耐心等一等！")
            "绿" -> speak("小交警放行：绿灯亮，请大家注意安全，有序通行！")
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
        if (!isVoiceEnabled) return
        if (!isTtsReady) {
            pendingSpeech = text
            Log.i("SoundManager", "TTS initializing, queued utterance: $text")
            return
        }
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_${System.currentTimeMillis()}")
            Log.d("SoundManager", "Speaking: $text")
        } catch (e: Exception) {
            Log.e("SoundManager", "Error in speech: $text", e)
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
