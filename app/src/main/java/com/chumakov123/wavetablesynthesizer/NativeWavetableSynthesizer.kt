package com.chumakov123.wavetablesynthesizer

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NativeWavetableSynthesizer : WavetableSynthesizer, DefaultLifecycleObserver {
    private var synthesizerHandle: Long = 0
    private val synthesizerMutex = Any()
    private external fun create(): Long
    private external fun delete(synthesizerHandle: Long)
    private external fun play(synthesizerHandle: Long)
    private external fun stop(synthesizerHandle: Long)
    private external fun isPlaying(synthesizerHandle: Long): Boolean
    private external fun setFrequency(synthesizerHandle: Long, frequencyInHz: Float)
    private external fun setVolume(synthesizerHandle: Long, volumeInDb: Float)
    private external fun setWavetable(synthesizerHandle: Long, wavetable: Int)
    private external fun noteOn(synthesizerHandle: Long, frequencyInHz: Float)
    private external fun noteOff(synthesizerHandle: Long, frequencyInHz: Float)
    private external fun setAttackTime(synthesizerHandle: Long, time: Float)
    private external fun setDecayTime(synthesizerHandle: Long, time: Float)
    private external fun setSustainLevel(synthesizerHandle: Long, level: Float)
    private external fun setReleaseTime(synthesizerHandle: Long, time: Float)
    private external fun setLfoRate(synthesizerHandle: Long, rate: Float)
    private external fun setLfoDepth(synthesizerHandle: Long, depth: Float)
    private external fun setTremoloDepth(synthesizerHandle: Long, depth: Float)
    private external fun setDelayTime(synthesizerHandle: Long, seconds: Float)
    private external fun setDelayFeedback(synthesizerHandle: Long, feedback: Float)
    private external fun setDelayWet(synthesizerHandle: Long, wet: Float)
    private external fun setMetronomeEnabled(synthesizerHandle: Long, enabled: Boolean)
    private external fun setBpm(synthesizerHandle: Long, bpm: Float)
    private external fun setRecording(synthesizerHandle: Long, enabled: Boolean)
    private external fun setPlayback(synthesizerHandle: Long, enabled: Boolean)
    private external fun clearSequence(synthesizerHandle: Long)
    private external fun clearActiveTrack(synthesizerHandle: Long)
    private external fun setQuantizationMode(synthesizerHandle: Long, mode: Int)
    private external fun setActiveTrack(synthesizerHandle: Long, trackId: Int)

    companion object {
        init {
            System.loadLibrary("wavetablesynthesizer")
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)

        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)

        synchronized(synthesizerMutex) {
            if (synthesizerHandle == 0L) {
                return
            }

            stop(synthesizerHandle)
            delete(synthesizerHandle)
            synthesizerHandle = 0L
        }
    }

    private fun createNativeHandleIfNotExists() {
        if (synthesizerHandle != 0L) {
            return
        }

        synthesizerHandle = create()
    }

    override suspend fun play() = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            play(synthesizerHandle)
        }
    }

    override suspend fun stop() = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            stop(synthesizerHandle)
        }
    }

    override suspend fun isPlaying(): Boolean = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            return@withContext isPlaying(synthesizerHandle)
        }
    }

    override suspend fun setFrequency(frequencyInHz: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            return@withContext setFrequency(synthesizerHandle, frequencyInHz)
        }
    }

    override suspend fun setVolume(volumeInDb: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            return@withContext setVolume(synthesizerHandle, volumeInDb)
        }
    }

    override suspend fun setWavetable(wavetable: Wavetable) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            return@withContext setWavetable(synthesizerHandle, wavetable.ordinal) //ordinal - порядковый номер
        }
    }

    override suspend fun noteOn(frequencyInHz: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            noteOn(synthesizerHandle, frequencyInHz)
        }
    }

    override suspend fun noteOff(frequencyInHz: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            noteOff(synthesizerHandle, frequencyInHz)
        }
    }

    override suspend fun setAttackTime(time: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setAttackTime(synthesizerHandle, time)
        }
    }

    override suspend fun setDecayTime(time: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setDecayTime(synthesizerHandle, time)
        }
    }

    override suspend fun setSustainLevel(level: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setSustainLevel(synthesizerHandle, level)
        }
    }

    override suspend fun setReleaseTime(time: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setReleaseTime(synthesizerHandle, time)
        }
    }

    override suspend fun setLfoRate(rate: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setLfoRate(synthesizerHandle, rate)
        }
    }

    override suspend fun setLfoDepth(depth: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setLfoDepth(synthesizerHandle, depth)
        }
    }

    override suspend fun setTremoloDepth(depth: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setTremoloDepth(synthesizerHandle, depth)
        }
    }

    override suspend fun setDelayTime(seconds: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setDelayTime(synthesizerHandle, seconds)
        }
    }

    override suspend fun setDelayFeedback(feedback: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setDelayFeedback(synthesizerHandle, feedback)
        }
    }

    override suspend fun setDelayWet(wet: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setDelayWet(synthesizerHandle, wet)
        }
    }

    override suspend fun setMetronomeEnabled(enabled: Boolean) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setMetronomeEnabled(synthesizerHandle, enabled)
        }
    }

    override suspend fun setBpm(bpm: Float) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setBpm(synthesizerHandle, bpm)
        }
    }

    override suspend fun setRecording(enabled: Boolean) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setRecording(synthesizerHandle, enabled)
        }
    }

    override suspend fun setPlayback(enabled: Boolean) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setPlayback(synthesizerHandle, enabled)
        }
    }

    override suspend fun clearSequence() = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            clearSequence(synthesizerHandle)
        }
    }

    override suspend fun clearActiveTrack() = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            clearActiveTrack(synthesizerHandle)
        }
    }

    override suspend fun setQuantizationMode(mode: Int) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setQuantizationMode(synthesizerHandle, mode)
        }
    }

    override suspend fun setActiveTrack(trackId: Int) = withContext(Dispatchers.Default) {
        synchronized(synthesizerMutex) {
            createNativeHandleIfNotExists()
            setActiveTrack(synthesizerHandle, trackId)
        }
    }
}