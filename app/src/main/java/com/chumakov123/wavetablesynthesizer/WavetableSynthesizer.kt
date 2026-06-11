package com.chumakov123.wavetablesynthesizer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class Wavetable {
    SINE {
        override fun toResourceString(): Int = R.string.sine
        override fun toResourceImage(): Int = R.drawable.sine_wave
    },
    TRIANGLE {
        override fun toResourceString(): Int = R.string.triangle
        override fun toResourceImage(): Int = R.drawable.triangle_wave
    },
    SQUARE {
        override fun toResourceString(): Int = R.string.square
        override fun toResourceImage(): Int = R.drawable.square_wave
    },
    SAW {
        override fun toResourceString(): Int = R.string.saw
        override fun toResourceImage(): Int = R.drawable.sawtooth_wave
    };


    @StringRes
    abstract fun toResourceString(): Int

    @DrawableRes
    abstract fun toResourceImage(): Int
}

interface WavetableSynthesizer {
    //suspend - приостановленный. Это означает, что их можно вызывать в контексте корутин
    suspend fun play()
    suspend fun stop()
    suspend fun isPlaying(): Boolean
    suspend fun setFrequency(frequencyInHz: Float)
    suspend fun setVolume(volumeInDb: Float)
    suspend fun setWavetable(wavetable: Wavetable)
    suspend fun noteOn(frequencyInHz: Float)
    suspend fun noteOff(frequencyInHz: Float)

    suspend fun setAttackTime(time: Float)
    suspend fun setDecayTime(time: Float)
    suspend fun setSustainLevel(level: Float)
    suspend fun setReleaseTime(time: Float)

    suspend fun setLfoRate(rate: Float)
    suspend fun setLfoDepth(depth: Float)
    suspend fun setTremoloDepth(depth: Float)

    suspend fun setDelayTime(seconds: Float)
    suspend fun setDelayFeedback(feedback: Float)
    suspend fun setDelayWet(wet: Float)

    suspend fun setMetronomeEnabled(enabled: Boolean)
    suspend fun setBpm(bpm: Float)

    suspend fun setRecording(enabled: Boolean)
    suspend fun setPlayback(enabled: Boolean)
    suspend fun clearSequence()
    suspend fun clearActiveTrack()
    suspend fun setQuantizationMode(mode: Int)
    suspend fun setActiveTrack(trackId: Int)

    // Arrangement Mode
    suspend fun setArrangementMode(enabled: Boolean)
    suspend fun addPatternToPlaylist(patternId: Int)
    suspend fun clearPlaylist()
    suspend fun setActivePattern(patternId: Int)
    suspend fun copyPattern(sourceId: Int, targetId: Int)
    suspend fun removePattern(patternId: Int)

    suspend fun triggerKick()
    suspend fun triggerSnare()
    suspend fun triggerHat()
    suspend fun setDrumVolume(volumeInDb: Float)
    suspend fun clearDrums()
}
