package com.chumakov123.wavetablesynthesizer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.ln
import kotlin.time.Duration.Companion.milliseconds

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

    private val _volume = MutableLiveData(-12f)
    val volume: LiveData<Float> = _volume

    val volumeRange = (-60f)..0f

    fun setVolume(volumeInDb: Float) {
        _volume.value = volumeInDb
        if (_isDrumsMode.value == true) {
            _drumVolume.value = volumeInDb
            viewModelScope.launch {
                wavetableSynthesizer?.setDrumVolume(volumeInDb)
            }
        } else {
            trackStates[_selectedTrack.value!!].volume = volumeInDb
            viewModelScope.launch {
                wavetableSynthesizer?.setVolume(volumeInDb)
            }
        }
    }

    private val _wavetable = MutableLiveData(Wavetable.SINE)
    val wavetable: LiveData<Wavetable> = _wavetable

    fun setWavetable(newWavetable: Wavetable) {
        _wavetable.value = newWavetable
        trackStates[_selectedTrack.value!!].wavetable = newWavetable
        viewModelScope.launch {
            wavetableSynthesizer?.setWavetable(newWavetable)
        }
    }

    private val _isKeyboardMode = MutableLiveData(true)
    val isKeyboardMode: LiveData<Boolean> = _isKeyboardMode

    private val _isDrumsMode = MutableLiveData(false)
    val isDrumsMode: LiveData<Boolean> = _isDrumsMode

    private val _drumVolume = MutableLiveData(-12f)
    val drumVolume: LiveData<Float> = _drumVolume

    private val _presets = MutableLiveData(Preset.defaultPresets)
    val presets: LiveData<List<Preset>> = _presets

    private val _selectedPresetIndex = MutableLiveData(0)
    val selectedPresetIndex: LiveData<Int> = _selectedPresetIndex

    enum class ControlPanelMode { WAVE, ADSR, LFO, FX, GRID }
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

    private val _delayTime = MutableLiveData(0.5f)
    val delayTime: LiveData<Float> = _delayTime

    private val _delayFeedback = MutableLiveData(0.5f)
    val delayFeedback: LiveData<Float> = _delayFeedback

    private val _delayWet = MutableLiveData(0.0f)
    val delayWet: LiveData<Float> = _delayWet

    private val _bpm = MutableLiveData(120f)
    val bpm: LiveData<Float> = _bpm

    private val _isMetronomeEnabled = MutableLiveData(false)
    val isMetronomeEnabled: LiveData<Boolean> = _isMetronomeEnabled

    private val _selectedTrack = MutableLiveData(0)
    val selectedTrack: LiveData<Int> = _selectedTrack

    data class TrackState(
        var wavetable: Wavetable = Wavetable.SINE,
        var attack: Float = 0.01f,
        var decay: Float = 0.1f,
        var sustain: Float = 0.7f,
        var release: Float = 0.3f,
        var lfoRate: Float = 5.0f,
        var lfoDepth: Float = 0.0f,
        var tremoloDepth: Float = 0.0f,
        var delayTime: Float = 0.5f,
        var delayFeedback: Float = 0.5f,
        var delayWet: Float = 0.0f,
        var volume: Float = -12f
    )
    
    private val trackStates = Array(4) { TrackState() }

    enum class Quantization(val label: String) {
        OFF("OFF"),
        GRID_1_16("1/16"),
        GRID_1_32("1/32")
    }
    private val _quantization = MutableLiveData(Quantization.OFF)
    val quantization: LiveData<Quantization> = _quantization

    private val _activeNotes = MutableLiveData<Set<Float>>(emptySet())
    val activeNotes: LiveData<Set<Float>> = _activeNotes

    private val _isArrangementExpanded = MutableLiveData(false)
    val isArrangementExpanded: LiveData<Boolean> = _isArrangementExpanded

    fun toggleArrangementExpanded() {
        _isArrangementExpanded.value = !(_isArrangementExpanded.value ?: false)
    }

    private val _isArrangementMode = MutableLiveData(false)
    val isArrangementMode: LiveData<Boolean> = _isArrangementMode

    private val _activePattern = MutableLiveData(0)
    val activePattern: LiveData<Int> = _activePattern

    private val _playlist = MutableLiveData<List<Int>>(emptyList())
    val playlist: LiveData<List<Int>> = _playlist

    private val _currentPlaylistIndex = MutableLiveData(0)
    val currentPlaylistIndex: LiveData<Int> = _currentPlaylistIndex

    private val _patternEvents = MutableLiveData<List<MidiEventData>>(emptyList())
    val patternEvents: LiveData<List<MidiEventData>> = _patternEvents

    enum class GridEditMode { DRAG, STRETCH, PAINT }
    private val _gridEditMode = MutableLiveData(GridEditMode.DRAG)
    val gridEditMode: LiveData<GridEditMode> = _gridEditMode

    fun setGridEditMode(mode: GridEditMode) {
        _gridEditMode.value = mode
    }

    fun setKeyboardMode(enabled: Boolean) {
        _isKeyboardMode.value = enabled
        if (enabled) _isDrumsMode.value = false
    }

    fun setDrumsMode(enabled: Boolean) {
        _isDrumsMode.value = enabled
        if (enabled) {
            _isKeyboardMode.value = false
            _volume.value = _drumVolume.value
        }
    }

    fun setControlPanelMode(mode: ControlPanelMode) {
        _controlPanelMode.value = mode
    }

    fun loadPreset(index: Int) {
        val preset = _presets.value?.getOrNull(index) ?: return
        _selectedPresetIndex.value = index

        val currentTrack = _selectedTrack.value!!
        trackStates[currentTrack].apply {
            wavetable = preset.wavetable
            attack = preset.attack
            decay = preset.decay
            sustain = preset.sustain
            release = preset.release
            lfoRate = preset.lfoRate
            lfoDepth = preset.lfoDepth
            tremoloDepth = preset.tremoloDepth
        }

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
        trackStates[_selectedTrack.value!!].attack = time
        viewModelScope.launch {
            wavetableSynthesizer?.setAttackTime(time)
        }
    }

    fun setDecay(time: Float) {
        _decay.value = time
        trackStates[_selectedTrack.value!!].decay = time
        viewModelScope.launch {
            wavetableSynthesizer?.setDecayTime(time)
        }
    }

    fun setSustain(level: Float) {
        _sustain.value = level
        trackStates[_selectedTrack.value!!].sustain = level
        viewModelScope.launch {
            wavetableSynthesizer?.setSustainLevel(level)
        }
    }

    fun setRelease(time: Float) {
        _release.value = time
        trackStates[_selectedTrack.value!!].release = time
        viewModelScope.launch {
            wavetableSynthesizer?.setReleaseTime(time)
        }
    }

    fun setLfoRate(rate: Float) {
        _lfoRate.value = rate
        trackStates[_selectedTrack.value!!].lfoRate = rate
        viewModelScope.launch {
            wavetableSynthesizer?.setLfoRate(rate)
        }
    }

    fun setLfoDepth(depth: Float) {
        _lfoDepth.value = depth
        trackStates[_selectedTrack.value!!].lfoDepth = depth
        viewModelScope.launch {
            wavetableSynthesizer?.setLfoDepth(depth)
        }
    }

    fun setTremoloDepth(depth: Float) {
        _tremoloDepth.value = depth
        trackStates[_selectedTrack.value!!].tremoloDepth = depth
        viewModelScope.launch {
            wavetableSynthesizer?.setTremoloDepth(depth)
        }
    }

    fun setDelayTime(seconds: Float) {
        _delayTime.value = seconds
        trackStates[_selectedTrack.value!!].delayTime = seconds
        viewModelScope.launch {
            wavetableSynthesizer?.setDelayTime(seconds)
        }
    }

    fun setDelayFeedback(feedback: Float) {
        _delayFeedback.value = feedback
        trackStates[_selectedTrack.value!!].delayFeedback = feedback
        viewModelScope.launch {
            wavetableSynthesizer?.setDelayFeedback(feedback)
        }
    }

    fun setDelayWet(wet: Float) {
        _delayWet.value = wet
        trackStates[_selectedTrack.value!!].delayWet = wet
        viewModelScope.launch {
            wavetableSynthesizer?.setDelayWet(wet)
        }
    }

    fun setSelectedTrack(trackId: Int) {
        _selectedTrack.value = trackId
        if (_isDrumsMode.value == true) return

        val state = trackStates[trackId]
        _wavetable.value = state.wavetable
        _attack.value = state.attack
        _decay.value = state.decay
        _sustain.value = state.sustain
        _release.value = state.release
        _lfoRate.value = state.lfoRate
        _lfoDepth.value = state.lfoDepth
        _tremoloDepth.value = state.tremoloDepth
        _delayTime.value = state.delayTime
        _delayFeedback.value = state.delayFeedback
        _delayWet.value = state.delayWet
        _volume.value = state.volume

        viewModelScope.launch {
            wavetableSynthesizer?.setActiveTrack(trackId)
            
            wavetableSynthesizer?.apply {
                setWavetable(state.wavetable)
                setAttackTime(state.attack)
                setDecayTime(state.decay)
                setSustainLevel(state.sustain)
                setReleaseTime(state.release)
                setLfoRate(state.lfoRate)
                setLfoDepth(state.lfoDepth)
                setTremoloDepth(state.tremoloDepth)
                setDelayTime(state.delayTime)
                setDelayFeedback(state.delayFeedback)
                setDelayWet(state.delayWet)
                setVolume(state.volume)
            }
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

    fun toggleArrangementMode() {
        val newState = !(_isArrangementMode.value ?: false)
        _isArrangementMode.value = newState
        viewModelScope.launch {
            wavetableSynthesizer?.setArrangementMode(newState)
        }
    }

    fun setActivePattern(patternId: Int) {
        _activePattern.value = patternId
        viewModelScope.launch {
            wavetableSynthesizer?.setActivePattern(patternId)
            refreshEvents()
        }
    }

    fun copyActivePatternTo(targetId: Int) {
        val sourceId = _activePattern.value ?: 0
        viewModelScope.launch {
            wavetableSynthesizer?.copyPattern(sourceId, targetId)
        }
    }

    fun removePattern(patternId: Int) {
        viewModelScope.launch {
            wavetableSynthesizer?.removePattern(patternId)
        }
    }

    fun addPatternToPlaylist(patternId: Int) {
        val currentPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
        currentPlaylist.add(patternId)
        _playlist.value = currentPlaylist
        viewModelScope.launch {
            wavetableSynthesizer?.addPatternToPlaylist(patternId)
        }
    }

    fun clearPlaylist() {
        _playlist.value = emptyList()
        viewModelScope.launch {
            wavetableSynthesizer?.clearPlaylist()
        }
    }

    fun refreshEvents() {
        viewModelScope.launch {
            val events = wavetableSynthesizer?.getEvents(_activePattern.value ?: 0) ?: emptyList()
            _patternEvents.postValue(events)
        }
    }

    fun updateEventTimestamp(index: Int, newTimestamp: Long) {
        viewModelScope.launch {
            wavetableSynthesizer?.updateEventTimestamp(_activePattern.value ?: 0, index, newTimestamp)
            refreshEvents()
        }
    }

    fun updateEventFrequency(index: Int, newFrequency: Float) {
        viewModelScope.launch {
            wavetableSynthesizer?.updateEventFrequency(_activePattern.value ?: 0, index, newFrequency)
            refreshEvents()
        }
    }

    fun addNote(timestamp: Long, frequency: Float, duration: Long) {
        viewModelScope.launch {
            val patternId = _activePattern.value ?: 0
            val isDrum = _isDrumsMode.value ?: false
            val trackId = if (isDrum) -1 else (_selectedTrack.value ?: 0)
            
            // Добавляем Note On
            wavetableSynthesizer?.addEvent(patternId, timestamp, frequency, true, trackId, isDrum)
            // Добавляем Note Off
            if (!isDrum) {
                wavetableSynthesizer?.addEvent(patternId, timestamp + duration, frequency, false, trackId, isDrum)
            }
            refreshEvents()
        }
    }

    fun moveNote(noteOnIndex: Int, noteOffIndex: Int, newNoteOnTimestamp: Long, newNoteOffTimestamp: Long) {
        viewModelScope.launch {
            val patternId = _activePattern.value ?: 0
            val currentEvents = _patternEvents.value ?: return@launch
            val oldNoteOnTimestamp = currentEvents.getOrNull(noteOnIndex)?.timestamp ?: 0
            
            if (newNoteOnTimestamp > oldNoteOnTimestamp) {
                if (noteOffIndex != -1) wavetableSynthesizer?.updateEventTimestamp(patternId, noteOffIndex, newNoteOffTimestamp)
                wavetableSynthesizer?.updateEventTimestamp(patternId, noteOnIndex, newNoteOnTimestamp)
            } else {
                wavetableSynthesizer?.updateEventTimestamp(patternId, noteOnIndex, newNoteOnTimestamp)
                if (noteOffIndex != -1) wavetableSynthesizer?.updateEventTimestamp(patternId, noteOffIndex, newNoteOffTimestamp)
            }
            refreshEvents()
        }
    }

    fun deleteEvent(index: Int) {
        viewModelScope.launch {
            wavetableSynthesizer?.deleteEvent(_activePattern.value ?: 0, index)
            refreshEvents()
        }
    }

    fun quantizeActivePattern() {
        viewModelScope.launch {
            val mode = if (_quantization.value == Quantization.OFF) {
                Quantization.GRID_1_16.ordinal
            } else {
                _quantization.value!!.ordinal
            }
            wavetableSynthesizer?.quantizePattern(_activePattern.value ?: 0, mode)
            refreshEvents()
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

    fun playPreviewNote(frequencyInHz: Float) {
        viewModelScope.launch {
            wavetableSynthesizer?.noteOn(frequencyInHz)
            delay(200.milliseconds)
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
            if (!nextRecordingState) {
                refreshEvents() // Обновляем список нот после окончания записи
            }
        }
    }

    private val _isPlayingRecording = MutableLiveData(false)
    val isPlayingRecording: LiveData<Boolean> = _isPlayingRecording

    fun togglePlayback() {
        val newState = !(_isPlayingRecording.value ?: false)
        _isPlayingRecording.value = newState
        viewModelScope.launch {
            wavetableSynthesizer?.setPlayback(newState)
            
            if (newState) {
                while (isActive && _isPlayingRecording.value == true) {
                    val index = wavetableSynthesizer?.getCurrentPlaylistIndex() ?: 0
                    if (_currentPlaylistIndex.value != index) {
                        _currentPlaylistIndex.postValue(index)
                    }
                    delay(100.milliseconds)
                }
            } else {
                _currentPlaylistIndex.value = 0
            }
        }
    }

    fun clearSequence() {
        viewModelScope.launch {
            if (_isDrumsMode.value == true) {
                wavetableSynthesizer?.clearDrums()
            } else {
                wavetableSynthesizer?.clearActiveTrack()
            }
            refreshEvents()
        }
    }

    fun triggerKick() {
        viewModelScope.launch {
            wavetableSynthesizer?.triggerKick()
        }
    }

    fun triggerSnare() {
        viewModelScope.launch {
            wavetableSynthesizer?.triggerSnare()
        }
    }

    fun triggerHat() {
        viewModelScope.launch {
            wavetableSynthesizer?.triggerHat()
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
            val currentSelected = _selectedTrack.value ?: 0
            
            for (i in 0 until 4) {
                wavetableSynthesizer?.setActiveTrack(i)
                val state = trackStates[i]
                wavetableSynthesizer?.apply {
                    setWavetable(state.wavetable)
                    setAttackTime(state.attack)
                    setDecayTime(state.decay)
                    setSustainLevel(state.sustain)
                    setReleaseTime(state.release)
                    setLfoRate(state.lfoRate)
                    setLfoDepth(state.lfoDepth)
                    setTremoloDepth(state.tremoloDepth)
                    setDelayTime(state.delayTime)
                    setDelayFeedback(state.delayFeedback)
                    setDelayWet(state.delayWet)
                    setVolume(state.volume)
                }
            }
            
            wavetableSynthesizer?.setActiveTrack(currentSelected)
            wavetableSynthesizer?.setDrumVolume(_drumVolume.value!!)
            
            wavetableSynthesizer?.setBpm(bpm.value!!)
            wavetableSynthesizer?.setMetronomeEnabled(isMetronomeEnabled.value!!)
            wavetableSynthesizer?.setQuantizationMode(quantization.value!!.ordinal)

            // Arrangement
            wavetableSynthesizer?.setArrangementMode(_isArrangementMode.value!!)
            wavetableSynthesizer?.setActivePattern(_activePattern.value!!)
            wavetableSynthesizer?.clearPlaylist()
            _playlist.value?.forEach { patternId ->
                wavetableSynthesizer?.addPatternToPlaylist(patternId)
            }

            updatePlayLabel()
        }
    }
}