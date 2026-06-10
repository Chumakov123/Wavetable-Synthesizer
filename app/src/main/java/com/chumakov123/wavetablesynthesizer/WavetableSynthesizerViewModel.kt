package com.chumakov123.wavetablesynthesizer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.ln

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

    private val _presets = MutableLiveData(Preset.defaultPresets)
    val presets: LiveData<List<Preset>> = _presets

    private val _selectedPresetIndex = MutableLiveData(0)
    val selectedPresetIndex: LiveData<Int> = _selectedPresetIndex

    enum class ControlPanelMode { WAVE, ADSR, LFO }
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

    private val _lfoRate = MutableLiveData(5.0f)
    val lfoRate: LiveData<Float> = _lfoRate

    private val _lfoDepth = MutableLiveData(0.0f)
    val lfoDepth: LiveData<Float> = _lfoDepth

    private val _tremoloDepth = MutableLiveData(0.0f)
    val tremoloDepth: LiveData<Float> = _tremoloDepth

    private val _bpm = MutableLiveData(120f)
    val bpm: LiveData<Float> = _bpm

    private val _isMetronomeEnabled = MutableLiveData(false)
    val isMetronomeEnabled: LiveData<Boolean> = _isMetronomeEnabled

    enum class Quantization(val label: String) {
        OFF("OFF"),
        GRID_1_16("1/16"),
        GRID_1_32("1/32")
    }
    private val _quantization = MutableLiveData(Quantization.OFF)
    val quantization: LiveData<Quantization> = _quantization

    private val _activeNotes = MutableLiveData<Set<Float>>(emptySet())
    val activeNotes: LiveData<Set<Float>> = _activeNotes

    fun setKeyboardMode(enabled: Boolean) {
        _isKeyboardMode.value = enabled
    }

    fun setControlPanelMode(mode: ControlPanelMode) {
        _controlPanelMode.value = mode
    }

    fun loadPreset(index: Int) {
        val preset = _presets.value?.getOrNull(index) ?: return
        _selectedPresetIndex.value = index
        
        _wavetable.value = preset.wavetable
        _attack.value = preset.attack
        _decay.value = preset.decay
        _sustain.value = preset.sustain
        _release.value = preset.release
        _lfoRate.value = preset.lfoRate
        _lfoDepth.value = preset.lfoDepth
        _tremoloDepth.value = preset.tremoloDepth
        
        applyParameters()
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

    fun setLfoRate(rate: Float) {
        _lfoRate.value = rate
        viewModelScope.launch {
            wavetableSynthesizer?.setLfoRate(rate)
        }
    }

    fun setLfoDepth(depth: Float) {
        _lfoDepth.value = depth
        viewModelScope.launch {
            wavetableSynthesizer?.setLfoDepth(depth)
        }
    }

    fun setTremoloDepth(depth: Float) {
        _tremoloDepth.value = depth
        viewModelScope.launch {
            wavetableSynthesizer?.setTremoloDepth(depth)
        }
    }

    fun setBpm(bpm: Float) {
        _bpm.value = bpm
        viewModelScope.launch {
            wavetableSynthesizer?.setBpm(bpm)
        }
    }

    fun setMetronomeEnabled(enabled: Boolean) {
        _isMetronomeEnabled.value = enabled
        viewModelScope.launch {
            wavetableSynthesizer?.setMetronomeEnabled(enabled)
        }
    }

    fun setQuantization(mode: Quantization) {
        _quantization.value = mode
        viewModelScope.launch {
            wavetableSynthesizer?.setQuantizationMode(mode.ordinal)
        }
    }

    fun noteOn(frequencyInHz: Float) {
        _activeNotes.value = _activeNotes.value?.plus(frequencyInHz)
        viewModelScope.launch {
            wavetableSynthesizer?.noteOn(frequencyInHz)
        }
    }

    fun noteOff(frequencyInHz: Float) {
        _activeNotes.value = _activeNotes.value?.minus(frequencyInHz)
        viewModelScope.launch {
            wavetableSynthesizer?.noteOff(frequencyInHz)
        }
    }

    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    fun toggleRecording() {
        val isCurrentlyRecording = _isRecording.value ?: false
        val nextRecordingState = !isCurrentlyRecording
        
        _isRecording.value = nextRecordingState
        
        // В нативной части при старте записи всегда включается playback
        // При остановке записи playback продолжается для зацикливания
        _isPlayingRecording.value = true
        
        viewModelScope.launch {
            wavetableSynthesizer?.setRecording(nextRecordingState)
        }
    }

    private val _isPlayingRecording = MutableLiveData(false)
    val isPlayingRecording: LiveData<Boolean> = _isPlayingRecording

    fun togglePlayback() {
        val newState = !(_isPlayingRecording.value ?: false)
        _isPlayingRecording.value = newState
        viewModelScope.launch {
            wavetableSynthesizer?.setPlayback(newState)
        }
    }

    fun clearSequence() {
        viewModelScope.launch {
            wavetableSynthesizer?.clearSequence()
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
            wavetableSynthesizer?.setLfoRate(lfoRate.value!!)
            wavetableSynthesizer?.setLfoDepth(lfoDepth.value!!)
            wavetableSynthesizer?.setTremoloDepth(tremoloDepth.value!!)
            wavetableSynthesizer?.setBpm(bpm.value!!)
            wavetableSynthesizer?.setMetronomeEnabled(isMetronomeEnabled.value!!)
            wavetableSynthesizer?.setQuantizationMode(quantization.value!!.ordinal)
            updatePlayLabel()
        }
    }
}