package com.chumakov123.wavetablesynthesizer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.ln
import kotlin.time.Duration.Companion.milliseconds

data class NoteEvent(
    val frequency: Float,
    val isNoteOn: Boolean,
    val timestampMs: Long
)

class WavetableSynthesizerViewModel : ViewModel() {
    var wavetableSynthesizer: WavetableSynthesizer? = null
        set (value) {
            field = value
            applyParameters()
        }

    private val _frequency = MutableLiveData(300f)
    val frequency: LiveData<Float> = _frequency

//    val frequency: LiveData<Float>
//        get() {
//            return _frequency
//        }
    fun setFrequencySliderPosition(frequencySliderPosition: Float) {
        val frequencyInHz = frequencyInHzFromSliderPosition(frequencySliderPosition)
        _frequency.value = frequencyInHz
        viewModelScope.launch {
            wavetableSynthesizer?.setFrequency(frequencyInHz)
        }
    }

    private val frequencyRange = 40f..3000f

    private fun frequencyInHzFromSliderPosition(sliderPosition: Float): Float {
        val rangePostion = linearToExponential(sliderPosition)
        return valueFromRangePosition(frequencyRange, rangePostion)
    }

    fun sliderPositionFromFrequencyInHz(frequencyInHz: Float): Float {
        val rangePosition = rangePositionFromValue(frequencyRange, frequencyInHz)
        return exponentionalToLinear(rangePosition)
    }

    companion object LinearToExponentialConverter {
        private const val MINIMUM_VALUE = 0.0001f

        fun linearToExponential(value: Float): Float {
            assert(value in 0f..1f)

            if (value < MINIMUM_VALUE) {
                return 0f
            }

            return exp(ln(MINIMUM_VALUE) * ( 1 - value))
        }

        fun valueFromRangePosition(range: ClosedFloatingPointRange<Float>, rangePosition: Float) =
            range.start + (range.endInclusive - range.start) * rangePosition

        fun rangePositionFromValue(range: ClosedFloatingPointRange<Float>, value: Float): Float {
            assert(value in range)

            return (value - range.start) / (range.endInclusive - range.start)
        }

        fun exponentionalToLinear(rangePosition: Float): Float {
            assert(rangePosition in 0f..1f)

            if (rangePosition < MINIMUM_VALUE) {
                return rangePosition
            }

            return 1 - ln(rangePosition) / ln(MINIMUM_VALUE)
        }
    }

    private val _volume = MutableLiveData(-24f)
    val volume: LiveData<Float> = _volume

    val volumeRange = (-60f)..0f

    fun setVolume(volumeInDb: Float) {
        _volume.value = volumeInDb
        viewModelScope.launch {
            wavetableSynthesizer?.setVolume(volumeInDb)
        }
    }

    private val _wavetable = MutableLiveData(Wavetable.SINE)
    val wavetable: LiveData<Wavetable> = _wavetable

    fun setWavetable(newWavetable: Wavetable) {
        _wavetable.value = newWavetable
        viewModelScope.launch {
            wavetableSynthesizer?.setWavetable(newWavetable)
        }
    }

    private val _isKeyboardMode = MutableLiveData(true)
    val isKeyboardMode: LiveData<Boolean> = _isKeyboardMode

    enum class ControlPanelMode { WAVE, ADSR }
    private val _controlPanelMode = MutableLiveData(ControlPanelMode.WAVE)
    val controlPanelMode: LiveData<ControlPanelMode> = _controlPanelMode

    private val _octave = MutableLiveData(0)
    val octave: LiveData<Int> = _octave

    private val _attack = MutableLiveData(0.01f)
    val attack: LiveData<Float> = _attack

    private val _decay = MutableLiveData(0.1f)
    val decay: LiveData<Float> = _decay

    private val _sustain = MutableLiveData(0.7f)
    val sustain: LiveData<Float> = _sustain

    private val _release = MutableLiveData(0.3f)
    val release: LiveData<Float> = _release

    private val _activeNotes = MutableLiveData<Set<Float>>(emptySet())
    val activeNotes: LiveData<Set<Float>> = _activeNotes

    fun setKeyboardMode(enabled: Boolean) {
        _isKeyboardMode.value = enabled
    }

    fun setControlPanelMode(mode: ControlPanelMode) {
        _controlPanelMode.value = mode
    }

    fun setOctave(octave: Int) {
        _octave.value = octave
    }

    fun setAttack(time: Float) {
        _attack.value = time
        viewModelScope.launch {
            wavetableSynthesizer?.setAttackTime(time)
        }
    }

    fun setDecay(time: Float) {
        _decay.value = time
        viewModelScope.launch {
            wavetableSynthesizer?.setDecayTime(time)
        }
    }

    fun setSustain(level: Float) {
        _sustain.value = level
        viewModelScope.launch {
            wavetableSynthesizer?.setSustainLevel(level)
        }
    }

    fun setRelease(time: Float) {
        _release.value = time
        viewModelScope.launch {
            wavetableSynthesizer?.setReleaseTime(time)
        }
    }

    fun noteOn(frequencyInHz: Float) {
        _activeNotes.value = _activeNotes.value?.plus(frequencyInHz)
        if (_isRecording.value == true) {
            recordEvent(frequencyInHz, true)
        }
        viewModelScope.launch {
            wavetableSynthesizer?.noteOn(frequencyInHz)
        }
    }

    fun noteOff(frequencyInHz: Float) {
        _activeNotes.value = _activeNotes.value?.minus(frequencyInHz)
        if (_isRecording.value == true) {
            recordEvent(frequencyInHz, false)
        }
        viewModelScope.launch {
            wavetableSynthesizer?.noteOff(frequencyInHz)
        }
    }

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    private val _recordedEvents = mutableListOf<NoteEvent>()
    private var recordingStartTime: Long = 0

    fun toggleRecording() {
        if (_isRecording.value == true) {
            _isRecording.value = false
        } else {
            _recordedEvents.clear()
            recordingStartTime = System.currentTimeMillis()
            _isRecording.value = true
        }
    }

    private fun recordEvent(frequency: Float, isNoteOn: Boolean) {
        val timestamp = System.currentTimeMillis() - recordingStartTime
        _recordedEvents.add(NoteEvent(frequency, isNoteOn, timestamp))
    }

    private val _isPlayingRecording = MutableLiveData(false)
    val isPlayingRecording: LiveData<Boolean> = _isPlayingRecording

    fun playRecording() {
        if (_recordedEvents.isEmpty() || _isPlayingRecording.value == true) return

        viewModelScope.launch {
            _isPlayingRecording.value = true
            val startTime = System.currentTimeMillis()
            
            _recordedEvents.forEach { event ->
                val delayTime = event.timestampMs - (System.currentTimeMillis() - startTime)
                if (delayTime > 0) {
                    delay(delayTime.milliseconds)
                }
                if (event.isNoteOn) {
                    noteOn(event.frequency)
                } else {
                    noteOff(event.frequency)
                }
            }
            _isPlayingRecording.value = false
        }
    }

    private val _playButtonLabel = MutableLiveData(R.string.play)
    val playButtonLabel: LiveData<Int> = _playButtonLabel

    fun playClicked() {
        viewModelScope.launch {
            if (wavetableSynthesizer?.isPlaying() == true)
            {
                wavetableSynthesizer?.stop()
            }
            else {
                wavetableSynthesizer?.play()
            }
            updatePlayLabel()
        }
    }

    private fun updatePlayLabel() {
        viewModelScope.launch {
            if (wavetableSynthesizer?.isPlaying() == true)
            {
                _playButtonLabel.value = R.string.stop
            }
            else {
                _playButtonLabel.value = R.string.play
            }
        }
    }

    fun applyParameters() {
        viewModelScope.launch {
            wavetableSynthesizer?.setFrequency(frequency.value!!)
            wavetableSynthesizer?.setVolume(volume.value!!)
            wavetableSynthesizer?.setWavetable(wavetable.value!!)
            wavetableSynthesizer?.setAttackTime(attack.value!!)
            wavetableSynthesizer?.setDecayTime(decay.value!!)
            wavetableSynthesizer?.setSustainLevel(sustain.value!!)
            wavetableSynthesizer?.setReleaseTime(release.value!!)
            updatePlayLabel()
        }
    }
}