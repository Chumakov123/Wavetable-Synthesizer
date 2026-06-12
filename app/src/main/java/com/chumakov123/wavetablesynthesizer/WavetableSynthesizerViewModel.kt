package com.chumakov123.wavetablesynthesizer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.exp
import kotlin.math.ln
import kotlin.time.Duration.Companion.milliseconds
import androidx.core.content.edit
import androidx.core.net.toUri

class WavetableSynthesizerViewModel : ViewModel() {
    var wavetableSynthesizer: WavetableSynthesizer? = null
        set (value) {
            field = value
            applyParameters()
        }

    private val _frequency = MutableLiveData(300f)
    val frequency: LiveData<Float> = _frequency

    fun setFrequencySliderPosition(frequencySliderPosition: Float) {
        val frequencyInHz = frequencyInHzFromSliderPosition(frequencySliderPosition)
        _frequency.value = frequencyInHz
        markDirty()
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
        markDirty()
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
        markDirty()
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

    @Serializable
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

    @Serializable
    data class ProjectData(
        val bpm: Float,
        val drumVolume: Float,
        val isArrangementMode: Boolean,
        val activePattern: Int,
        val tracks: List<TrackState>,
        val playlist: List<Int>,
        val patterns: List<List<MidiEventData>>,
        val vocalTrackPath: String? = null,
        val isVocalTrackEnabled: Boolean = true,
        val vocalTrackOffset: Float = 0f,
        val vocalTrackVolume: Float = 0f
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
        markDirty()

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
        markDirty()
        trackStates[_selectedTrack.value!!].attack = time
        viewModelScope.launch {
            wavetableSynthesizer?.setAttackTime(time)
        }
    }

    fun setDecay(time: Float) {
        _decay.value = time
        markDirty()
        trackStates[_selectedTrack.value!!].decay = time
        viewModelScope.launch {
            wavetableSynthesizer?.setDecayTime(time)
        }
    }

    fun setSustain(level: Float) {
        _sustain.value = level
        markDirty()
        trackStates[_selectedTrack.value!!].sustain = level
        viewModelScope.launch {
            wavetableSynthesizer?.setSustainLevel(level)
        }
    }

    fun setRelease(time: Float) {
        _release.value = time
        markDirty()
        trackStates[_selectedTrack.value!!].release = time
        viewModelScope.launch {
            wavetableSynthesizer?.setReleaseTime(time)
        }
    }

    fun setLfoRate(rate: Float) {
        _lfoRate.value = rate
        markDirty()
        trackStates[_selectedTrack.value!!].lfoRate = rate
        viewModelScope.launch {
            wavetableSynthesizer?.setLfoRate(rate)
        }
    }

    fun setLfoDepth(depth: Float) {
        _lfoDepth.value = depth
        markDirty()
        trackStates[_selectedTrack.value!!].lfoDepth = depth
        viewModelScope.launch {
            wavetableSynthesizer?.setLfoDepth(depth)
        }
    }

    fun setTremoloDepth(depth: Float) {
        _tremoloDepth.value = depth
        markDirty()
        trackStates[_selectedTrack.value!!].tremoloDepth = depth
        viewModelScope.launch {
            wavetableSynthesizer?.setTremoloDepth(depth)
        }
    }

    fun setDelayTime(seconds: Float) {
        _delayTime.value = seconds
        markDirty()
        trackStates[_selectedTrack.value!!].delayTime = seconds
        viewModelScope.launch {
            wavetableSynthesizer?.setDelayTime(seconds)
        }
    }

    fun setDelayFeedback(feedback: Float) {
        _delayFeedback.value = feedback
        markDirty()
        trackStates[_selectedTrack.value!!].delayFeedback = feedback
        viewModelScope.launch {
            wavetableSynthesizer?.setDelayFeedback(feedback)
        }
    }

    fun setDelayWet(wet: Float) {
        _delayWet.value = wet
        markDirty()
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
        markDirty()
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
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.setQuantizationMode(mode.ordinal)
        }
    }

    fun toggleArrangementMode() {
        val newState = !(_isArrangementMode.value ?: false)
        _isArrangementMode.value = newState
        markDirty()
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
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.copyPattern(sourceId, targetId)
        }
    }

    fun removePattern(patternId: Int) {
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.removePattern(patternId)
        }
    }

    fun addPatternToPlaylist(patternId: Int) {
        val currentPlaylist = _playlist.value?.toMutableList() ?: mutableListOf()
        currentPlaylist.add(patternId)
        _playlist.value = currentPlaylist
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.addPatternToPlaylist(patternId)
        }
    }

    fun clearPlaylist() {
        _playlist.value = emptyList()
        markDirty()
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
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.updateEventTimestamp(_activePattern.value ?: 0, index, newTimestamp)
            refreshEvents()
        }
    }

    fun updateEventFrequency(index: Int, newFrequency: Float) {
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.updateEventFrequency(_activePattern.value ?: 0, index, newFrequency)
            refreshEvents()
        }
    }

    fun addNote(timestamp: Long, frequency: Float, duration: Long) {
        markDirty()
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
        markDirty()
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
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.deleteEvent(_activePattern.value ?: 0, index)
            refreshEvents()
        }
    }

    fun quantizeActivePattern() {
        markDirty()
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

    private val _isMicRecording = MutableLiveData(false)
    val isMicRecording: LiveData<Boolean> = _isMicRecording

    private val _vocalTrackPath = MutableLiveData<String?>(null)
    val vocalTrackPath: LiveData<String?> = _vocalTrackPath

    private val _isVocalTrackEnabled = MutableLiveData(true)
    val isVocalTrackEnabled: LiveData<Boolean> = _isVocalTrackEnabled

    private val _vocalTrackOffset = MutableLiveData(0f)
    val vocalTrackOffset: LiveData<Float> = _vocalTrackOffset

    private val _vocalTrackVolume = MutableLiveData(0f)
    val vocalTrackVolume: LiveData<Float> = _vocalTrackVolume

    private val _isRendering = MutableLiveData(false)
    val isRendering: LiveData<Boolean> = _isRendering

    private val _renderingProgress = MutableLiveData(0f)
    val renderingProgress: LiveData<Float> = _renderingProgress

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
                markDirty()
                refreshEvents() // Обновляем список нот после окончания записи
            }
        }
    }

    fun toggleMicRecording(context: Context) {
        val isCurrentlyRecording = _isMicRecording.value ?: false
        val nextRecordingState = !isCurrentlyRecording

        viewModelScope.launch {
            if (nextRecordingState) {
                val projectsUriStr = _projectsFolderUri.value
                if (projectsUriStr != null) {
                    // Создаем подкаталог Vocals в папке проектов
                    val treeUri = projectsUriStr.toUri()
                    val treeFile = DocumentFile.fromTreeUri(context, treeUri)
                    var vocalsDir = treeFile?.findFile("Vocals")
                    if (vocalsDir == null) {
                        vocalsDir = treeFile?.createDirectory("Vocals")
                    }

                    val tempFile = File(context.cacheDir, "temp_mic.wav")
                    val started = wavetableSynthesizer?.startMicRecording(tempFile.absolutePath) ?: false
                    if (started) {
                        _isMicRecording.value = true
                        
                        // Запоминаем позицию начала записи (если будем поддерживать запись не с 0)
                        // Сейчас просто сбрасываем offset в 0, так как togglePlayback сбросит на 0
                        _vocalTrackOffset.value = 0f
                        wavetableSynthesizer?.setAudioTrackOffset(0f)

                        // Автоматически запускаем воспроизведение аранжировки
                        if (_isPlayingRecording.value != true) {
                            togglePlayback()
                        }
                    } else {
                        Toast.makeText(context, "Failed to start microphone recording", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please set projects folder first", Toast.LENGTH_SHORT).show()
                    _activeDialog.value = DialogType.MIGRATION_REQUIRED
                }
            } else {
                wavetableSynthesizer?.stopMicRecording()
                _isMicRecording.value = false
                
                if (_isPlayingRecording.value == true) {
                    togglePlayback()
                }

                val tempFile = File(context.cacheDir, "temp_mic.wav")
                if (tempFile.exists()) {
                    val name = _projectName.value ?: "untitled"
                    val timestamp = System.currentTimeMillis()
                    val fileName = "${name}_mic_${timestamp}.wav"
                    
                    val projectsUriStr = _projectsFolderUri.value
                    if (projectsUriStr != null) {
                        withContext(Dispatchers.IO) {
                            try {
                                val treeUri = projectsUriStr.toUri()
                                val treeFile = DocumentFile.fromTreeUri(context, treeUri)
                                var vocalsDir = treeFile?.findFile("Vocals")
                                if (vocalsDir == null) {
                                    vocalsDir = treeFile?.createDirectory("Vocals")
                                }

                                val wavFile = vocalsDir?.createFile("audio/wav", fileName)
                                wavFile?.uri?.let { destUri ->
                                    context.contentResolver.openOutputStream(destUri)?.use { out ->
                                        tempFile.inputStream().use { input ->
                                            input.copyTo(out)
                                        }
                                    }
                                    
                                    // Сохраняем локальную копию для движка (Oboe)
                                    val vocalsLocalDir = File(context.filesDir, "Vocals")
                                    if (!vocalsLocalDir.exists()) vocalsLocalDir.mkdirs()
                                    
                                    val permanentFile = File(vocalsLocalDir, fileName)
                                    tempFile.copyTo(permanentFile, overwrite = true)
                                    
                                    withContext(Dispatchers.Main) {
                                        _vocalTrackPath.value = permanentFile.absolutePath
                                        wavetableSynthesizer?.loadAudioTrack(permanentFile.absolutePath)
                                        wavetableSynthesizer?.setAudioTrackEnabled(true)
                                        _isVocalTrackEnabled.value = true
                                        markDirty()
                                        Toast.makeText(context, "Saved to Vocals/$fileName", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("SynthVM", "Mic recording copy failed", e)
                            } finally {
                                tempFile.delete()
                            }
                        }
                    }
                }
            }
        }
    }

    fun setVocalTrackEnabled(enabled: Boolean) {
        _isVocalTrackEnabled.value = enabled
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.setAudioTrackEnabled(enabled)
        }
    }

    fun setVocalTrackOffset(seconds: Float) {
        _vocalTrackOffset.value = seconds
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.setAudioTrackOffset(seconds)
        }
    }

    fun setVocalTrackVolume(volumeInDb: Float) {
        _vocalTrackVolume.value = volumeInDb
        markDirty()
        viewModelScope.launch {
            wavetableSynthesizer?.setAudioTrackVolume(volumeInDb)
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
        markDirty()
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

    private val _projectName = MutableLiveData("untitled")
    val projectName: LiveData<String> = _projectName

    private val _isDirty = MutableLiveData(false)
    val isDirty: LiveData<Boolean> = _isDirty

    enum class DialogType { NONE, SAVE_CONFIRMATION, PROJECT_NAME, PROJECT_LIST, MIGRATION_REQUIRED, EXPORT_SETUP, RENDERING }
    private val _activeDialog = MutableLiveData(DialogType.NONE)
    val activeDialog: LiveData<DialogType> = _activeDialog

    private var pendingProjectToLoad: String? = null
    private var isExitPending = false

    fun showDialog(type: DialogType) {
        if (type == DialogType.SAVE_CONFIRMATION && pendingProjectToLoad == null) {
            isExitPending = true
        }
        _activeDialog.value = type
    }

    fun dismissDialog() {
        _activeDialog.value = DialogType.NONE
        pendingProjectToLoad = null
        isExitPending = false
    }
    
    fun isExitPending() = isExitPending

    fun createNewProject() {
        _projectName.value = "untitled"
        _isDirty.value = false
        _selectedTrack.value = 0
        for (i in 0 until 4) {
            trackStates[i] = TrackState()
        }
        _bpm.value = 120f
        _drumVolume.value = -12f
        _isArrangementMode.value = false
        _activePattern.value = 0
        _playlist.value = emptyList()
        _currentPlaylistIndex.value = 0
        
        viewModelScope.launch {
            wavetableSynthesizer?.clearAllPatterns()
            setSelectedTrack(0)
            applyParameters()
            refreshEvents()
        }
    }

    fun onProjectSelected(name: String) {
        if (_isDirty.value == true) {
            pendingProjectToLoad = name
            _activeDialog.value = DialogType.SAVE_CONFIRMATION
        } else {
            // Need to call loadProject, but it needs Context
            // The UI will handle this
        }
    }
    
    fun getPendingProjectName() = pendingProjectToLoad

    private val _projectList = MutableLiveData<List<String>>(emptyList())
    val projectList: LiveData<List<String>> = _projectList

    private val _projectsFolderUri = MutableLiveData<String?>(null)
    val projectsFolderUri: LiveData<String?> = _projectsFolderUri

    private val _outputFolderUri = MutableLiveData<String?>(null)
    val outputFolderUri: LiveData<String?> = _outputFolderUri

    fun setProjectsFolderUri(uri: String?, context: Context) {
        _projectsFolderUri.value = uri
        val prefs = context.getSharedPreferences("synth_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("projects_folder_uri", uri) }
        if (uri != null) {
            refreshProjectList(context)
        }
    }

    fun setOutputFolderUri(uri: String?, context: Context) {
        _outputFolderUri.value = uri
        val prefs = context.getSharedPreferences("synth_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("output_folder_uri", uri) }
    }

    private fun markDirty() {
        if (_isDirty.value == false) {
            _isDirty.value = true
        }
    }

    private fun getNextUntitledName(): String {
        val list = _projectList.value ?: emptyList()
        var i = 1
        while (list.contains("untitled-$i")) {
            i++
        }
        return "untitled-$i"
    }

    fun setProjectName(name: String) {
        _projectName.value = name
        markDirty()
    }
    
    fun showProjectNameDialog() {
        if (_projectName.value == "untitled") {
            _projectName.value = getNextUntitledName()
        }
        _activeDialog.value = DialogType.PROJECT_NAME
    }

    fun refreshProjectList(context: Context) {
        viewModelScope.launch {
            val list = mutableListOf<String>()
            val uriStr = _projectsFolderUri.value
            
            if (uriStr != null) {
                // List from External (SAF)
                withContext(Dispatchers.IO) {
                    try {
                        val treeUri = uriStr.toUri()
                        val treeFile = DocumentFile.fromTreeUri(context, treeUri)
                        treeFile?.listFiles()?.forEach { file ->
                            if (file.name?.endsWith(".udw") == true) {
                                list.add(file.name!!.removeSuffix(".udw"))
                            } else if (file.name == "project.json") {
                                list.add("project")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SynthVM", "Failed to list external projects", e)
                    }
                }
            }
            
            // Also include internal ones for migration
            val internalFiles = context.filesDir.listFiles { _, name -> name.endsWith(".udw") || name == "project.json" }
            internalFiles?.forEach { file ->
                val name = if (file.name == "project.json") "project" else file.name.removeSuffix(".udw")
                if (!list.contains(name)) list.add(name)
            }
            
            _projectList.postValue(list.sorted())
        }
    }

    fun initStorage(context: Context) {
        val prefs = context.getSharedPreferences("synth_prefs", Context.MODE_PRIVATE)
        _projectsFolderUri.value = prefs.getString("projects_folder_uri", null)
        _outputFolderUri.value = prefs.getString("output_folder_uri", null)
        
        refreshProjectList(context)
        
        // Check for migration
        val internalFiles = context.filesDir.listFiles { _, name -> name.endsWith(".udw") }
        if (internalFiles != null && internalFiles.isNotEmpty() && _projectsFolderUri.value == null) {
            _activeDialog.value = DialogType.MIGRATION_REQUIRED
        } else if (internalFiles != null && internalFiles.isNotEmpty()) {
            migrateProjectsToExternal(context)
        }
    }

    private fun migrateProjectsToExternal(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val uriStr = _projectsFolderUri.value ?: return@launch
            val treeUri = uriStr.toUri()
            val treeFile = DocumentFile.fromTreeUri(context, treeUri) ?: return@launch
            
            val internalFiles = context.filesDir.listFiles { _, name -> name.endsWith(".udw") || name == "project.json" } ?: return@launch
            
            for (file in internalFiles) {
                try {
                    val content = file.readText()
                    val targetName = if (file.name == "project.json") "project.udw" else file.name
                    val newFile = treeFile.createFile("application/octet-stream", targetName)
                    newFile?.uri?.let { destUri ->
                        context.contentResolver.openOutputStream(destUri)?.use { out ->
                            out.write(content.toByteArray())
                        }
                        file.delete()
                    }
                } catch (e: Exception) {
                    Log.e("SynthVM", "Migration failed for ${file.name}", e)
                }
            }
            refreshProjectList(context)
        }
    }

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
            wavetableSynthesizer?.setFrequency(frequency.value!!)
            wavetableSynthesizer?.setDrumVolume(_drumVolume.value!!)
            
            wavetableSynthesizer?.setBpm(bpm.value!!)
            wavetableSynthesizer?.setMetronomeEnabled(isMetronomeEnabled.value!!)
            wavetableSynthesizer?.setQuantizationMode(quantization.value!!.ordinal)

            // Vocal Track
            _vocalTrackPath.value?.let { path ->
                wavetableSynthesizer?.loadAudioTrack(path)
                wavetableSynthesizer?.setAudioTrackEnabled(_isVocalTrackEnabled.value!!)
                wavetableSynthesizer?.setAudioTrackOffset(_vocalTrackOffset.value!!)
                wavetableSynthesizer?.setAudioTrackVolume(_vocalTrackVolume.value!!)
            }

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

    fun saveProject(context: Context, name: String? = null) {
        val finalName = name ?: _projectName.value ?: "untitled"
        if (_isDirty.value == false && name == null) {
            Log.d("SynthVM", "No changes to save")
            return
        }

        viewModelScope.launch {
            val patterns = mutableListOf<List<MidiEventData>>()
            val patternCount = wavetableSynthesizer?.getPatternCount() ?: 0
            for (i in 0 until patternCount) {
                patterns.add(wavetableSynthesizer?.getEvents(i) ?: emptyList())
            }

            val projectData = ProjectData(
                bpm = _bpm.value ?: 120f,
                drumVolume = _drumVolume.value ?: -12f,
                isArrangementMode = _isArrangementMode.value ?: false,
                activePattern = _activePattern.value ?: 0,
                tracks = trackStates.toList(),
                playlist = _playlist.value ?: emptyList(),
                patterns = patterns,
                vocalTrackPath = _vocalTrackPath.value,
                isVocalTrackEnabled = _isVocalTrackEnabled.value ?: true,
                vocalTrackOffset = _vocalTrackOffset.value ?: 0f,
                vocalTrackVolume = _vocalTrackVolume.value ?: 0f
            )

            val jsonString = Json.encodeToString(projectData)
            val fileName = "$finalName.udw"

            withContext(Dispatchers.IO) {
                try {
                    val uriStr = _projectsFolderUri.value
                    if (uriStr != null) {
                        val treeUri = uriStr.toUri()
                        val treeFile = DocumentFile.fromTreeUri(context, treeUri)
                        var file = treeFile?.findFile(fileName)
                        if (file == null) {
                            file = treeFile?.createFile("application/octet-stream", fileName)
                        }
                        
                        file?.uri?.let { destUri ->
                            context.contentResolver.openOutputStream(destUri)?.use { out ->
                                out.write(jsonString.toByteArray())
                            }
                        }
                    } else {
                        // Fallback to internal
                        val file = File(context.filesDir, fileName)
                        file.writeText(jsonString)
                    }

                    withContext(Dispatchers.Main) {
                        _projectName.value = finalName
                        _isDirty.value = false
                        refreshProjectList(context)
                        Toast.makeText(context, "Project Saved: $finalName", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("SynthVM", "Failed to save project", e)
                }
            }
        }
    }

    fun loadProject(context: Context, name: String) {
        viewModelScope.launch {
            val jsonString = withContext(Dispatchers.IO) {
                try {
                    val uriStr = _projectsFolderUri.value
                    if (uriStr != null) {
                        val treeUri = uriStr.toUri()
                        val treeFile = DocumentFile.fromTreeUri(context, treeUri)
                        // Try .udw then .json
                        var file = treeFile?.findFile("$name.udw")
                        if (file == null && name == "project") {
                            file = treeFile?.findFile("project.json")
                        }
                        
                        file?.uri?.let { srcUri ->
                            context.contentResolver.openInputStream(srcUri)?.use { input ->
                                input.bufferedReader().readText()
                            }
                        }
                    } else {
                        var file = File(context.filesDir, "$name.udw")
                        if (!file.exists() && name == "project") {
                            file = File(context.filesDir, "project.json")
                        }
                        if (file.exists()) file.readText() else null
                    }
                } catch (e: Exception) {
                    Log.e("SynthVM", "Failed to read project file", e)
                    null
                }
            } ?: return@launch

            try {
                val projectData = Json.decodeFromString<ProjectData>(jsonString)
                
                // Clear native state first
                wavetableSynthesizer?.clearAllPatterns()

                _bpm.value = projectData.bpm
                _drumVolume.value = projectData.drumVolume
                _isArrangementMode.value = projectData.isArrangementMode
                _activePattern.value = projectData.activePattern
                _vocalTrackPath.value = projectData.vocalTrackPath
                _isVocalTrackEnabled.value = projectData.isVocalTrackEnabled
                _vocalTrackOffset.value = projectData.vocalTrackOffset
                _vocalTrackVolume.value = projectData.vocalTrackVolume

                projectData.tracks.forEachIndexed { i, state ->
                    if (i < trackStates.size) {
                        trackStates[i] = state
                    }
                }

                _playlist.value = projectData.playlist

                projectData.patterns.forEachIndexed { i, events ->
                    for (event in events) {
                        wavetableSynthesizer?.addEvent(
                            i,
                            event.timestamp,
                            event.frequency,
                            event.isNoteOn,
                            event.trackId,
                            event.isDrum
                        )
                    }
                }

                // Update UI state to match selected track
                setSelectedTrack(_selectedTrack.value ?: 0)
                applyParameters()
                refreshEvents()
                
                _projectName.value = name
                _isDirty.value = false
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Project Loaded: $name", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SynthVM", "Failed to parse project JSON", e)
            }
        }
    }

    fun renderToWav(context: Context) {
        if (_outputFolderUri.value == null) {
            _activeDialog.value = DialogType.EXPORT_SETUP
            return
        }

        viewModelScope.launch {
            val name = _projectName.value ?: "untitled"
            val tempFile = File(context.cacheDir, "temp_render.wav")
            
            // Stop real-time playback during rendering
            val wasPlaying = wavetableSynthesizer?.isPlaying() ?: false
            if (wasPlaying) {
                wavetableSynthesizer?.stop()
                updatePlayLabel()
            }

            _isRendering.value = true
            _renderingProgress.value = 0f
            _activeDialog.value = DialogType.RENDERING

            // Poll progress
            val progressJob = launch {
                while (isActive) {
                    val progress = wavetableSynthesizer?.getRenderingProgress() ?: 1f
                    _renderingProgress.postValue(progress)
                    if (progress >= 1f) break
                    delay(100.milliseconds)
                }
            }

            withContext(Dispatchers.Default) {
                wavetableSynthesizer?.renderArrangement(tempFile.absolutePath)
            }
            
            progressJob.cancel()
            _renderingProgress.value = 1f
            _isRendering.value = false
            _activeDialog.value = DialogType.NONE
            
            if (tempFile.exists() && tempFile.length() > 44) {
                val uriStr = _outputFolderUri.value
                if (uriStr != null) {
                    withContext(Dispatchers.IO) {
                        try {
                            val treeUri = uriStr.toUri()
                            val treeFile = DocumentFile.fromTreeUri(context, treeUri)
                            val wavFile = treeFile?.createFile("audio/wav", "$name.wav")
                            wavFile?.uri?.let { destUri ->
                                context.contentResolver.openOutputStream(destUri)?.use { out ->
                                    tempFile.inputStream().use { input ->
                                        input.copyTo(out)
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Rendered to: $name.wav", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("SynthVM", "Render copy failed", e)
                        } finally {
                            tempFile.delete()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Render failed (maybe playlist is empty?)", Toast.LENGTH_SHORT).show()
                }
            }
            
            // Resume playback if it was active (optional, maybe better leave stopped)
            // if (wasPlaying) wavetableSynthesizer?.play()
        }
    }
}
