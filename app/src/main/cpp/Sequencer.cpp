#include "Sequencer.h"
#include <algorithm>

namespace wavetablesynthesizer {

    Sequencer::Sequencer(double sampleRate) : _sampleRate(sampleRate) {
        _patterns.push_back(Pattern());
        updateLoopLength();
    }

    void Sequencer::process(uint64_t currentFrame, uint32_t numFrames) {
        if (!_isPlaying.load(std::memory_order_relaxed) && !_isRecording.load(std::memory_order_relaxed)) {
            return;
        }

        std::lock_guard<std::mutex> lock(_eventsMutex);
        for (uint32_t i = 0; i < numFrames; ++i) {
            if (_isPlaying.load(std::memory_order_relaxed)) {
                if (_isArrangementMode.load()) {
                    if (!_playlist.empty() && _currentPlaylistIndex < _playlist.size()) {
                        int patternId = _playlist[_currentPlaylistIndex];
                        playEventsAt(patternId, _currentLoopSample);
                    }
                } else {
                    playEventsAt(_activePatternId.load(), _currentLoopSample);
                }
            }

            _currentLoopSample++;
            if (_currentLoopSample >= _loopLengthSamples) {
                _currentLoopSample = 0;

                if (_isArrangementMode.load() && !_playlist.empty()) {
                    _currentPlaylistIndex++;
                    if (_currentPlaylistIndex >= _playlist.size()) {
                        _currentPlaylistIndex = 0; // Зацикливаем весь плейлист
                    }
                }
            }
        }
    }

    void Sequencer::playEventsAt(int patternId, uint64_t timestamp) {
        if (patternId < 0 || patternId >= _patterns.size()) return;

        const auto& events = _patterns[patternId].events;
        for (const auto& event : events) {
            if (event.timestamp == timestamp) {
                if (_noteCallback) {
                    if (event.isDrum) {
                        _noteCallback(_receiver, -1, event.frequency, true);
                    } else {
                        _noteCallback(_receiver, event.trackId, event.frequency, event.isNoteOn);
                    }
                }
            }
        }
    }

    void Sequencer::recordNoteOn(int trackId, float frequency) {
        if (!_isRecording.load()) return;

        std::lock_guard<std::mutex> lock(_eventsMutex);
        uint64_t timestamp = getQuantizedTimestamp(_currentLoopSample);
        int activePattern = _activePatternId.load();
        if (activePattern >= 0 && activePattern < _patterns.size()) {
            _patterns[activePattern].events.push_back({timestamp, frequency, true, trackId, false});
        }
    }

    void Sequencer::recordNoteOff(int trackId, float frequency) {
        if (!_isRecording.load()) return;

        std::lock_guard<std::mutex> lock(_eventsMutex);
        uint64_t timestamp = getQuantizedTimestamp(_currentLoopSample);
        int activePattern = _activePatternId.load();
        if (activePattern >= 0 && activePattern < _patterns.size()) {
            _patterns[activePattern].events.push_back({timestamp, frequency, false, trackId, false});
        }
    }

    void Sequencer::recordDrum(int drumId) {
        if (!_isRecording.load()) return;

        std::lock_guard<std::mutex> lock(_eventsMutex);
        uint64_t timestamp = getQuantizedTimestamp(_currentLoopSample);
        int activePattern = _activePatternId.load();
        if (activePattern >= 0 && activePattern < _patterns.size()) {
            _patterns[activePattern].events.push_back({timestamp, static_cast<float>(drumId), true, -1, true});
        }
    }

    void Sequencer::startRecording() {
        _isRecording.store(true);
        _isPlaying.store(true);
    }

    void Sequencer::stopRecording() {
        _isRecording.store(false);
    }

    void Sequencer::startPlayback() {
        _isPlaying.store(true);
    }

    void Sequencer::stopPlayback() {
        _isPlaying.store(false);
        _currentLoopSample = 0;
        _currentPlaylistIndex = 0;
    }

    void Sequencer::clear() {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        int activePattern = _activePatternId.load();
        if (activePattern >= 0 && activePattern < _patterns.size()) {
            _patterns[activePattern].events.clear();
        }
        _currentLoopSample = 0;
    }

    void Sequencer::clearTrack(int trackId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        int activePattern = _activePatternId.load();
        if (activePattern >= 0 && activePattern < _patterns.size()) {
            auto& events = _patterns[activePattern].events;
            events.erase(std::remove_if(events.begin(), events.end(),
                                         [trackId](const MidiEvent& e) { return e.trackId == trackId; }),
                          events.end());
        }
    }

    void Sequencer::setBpm(float bpm) {
        _bpm.store(bpm);
        updateLoopLength();
    }

    void Sequencer::setLoopLengthBars(int bars) {
        _loopLengthBars.store(bars);
        updateLoopLength();
    }

    void Sequencer::setQuantizationMode(QuantizationMode mode) {
        _quantizationMode.store(mode);
    }

    void Sequencer::setArrangementMode(bool enabled) {
        _isArrangementMode.store(enabled);
        _currentPlaylistIndex = 0;
        _currentLoopSample = 0;
    }

    void Sequencer::addPatternToPlaylist(int patternId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        _playlist.push_back(patternId);
    }

    void Sequencer::clearPlaylist() {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        _playlist.clear();
        _currentPlaylistIndex = 0;
    }

    void Sequencer::setActivePattern(int patternId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        while (_patterns.size() <= patternId) {
            _patterns.push_back(Pattern());
        }
        _activePatternId.store(patternId);
    }

    void Sequencer::copyPattern(int sourceId, int targetId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (sourceId < 0 || sourceId >= _patterns.size()) return;

        while (_patterns.size() <= targetId) {
            _patterns.push_back(Pattern());
        }
        _patterns[targetId] = _patterns[sourceId];
    }

    void Sequencer::removePattern(int patternId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return;
        _patterns[patternId].events.clear();
    }

    uint64_t Sequencer::getQuantizedTimestamp(uint64_t timestamp) {
        QuantizationMode mode = _quantizationMode.load();
        if (mode == QuantizationMode::None) return timestamp;

        float bpm = _bpm.load();
        float samplesPerBeat = static_cast<float>(_sampleRate) * 60.0f / bpm;
        float samplesPerGrid = 0.0f;

        if (mode == QuantizationMode::Beat_1_16) {
            samplesPerGrid = samplesPerBeat / 4.0f; // 1/16 note = 1/4 beat
        } else if (mode == QuantizationMode::Beat_1_32) {
            samplesPerGrid = samplesPerBeat / 8.0f; // 1/32 note = 1/8 beat
        }

        if (samplesPerGrid <= 0.0f) return timestamp;

        auto quantized = static_cast<uint64_t>(std::round(static_cast<float>(timestamp) / samplesPerGrid) * samplesPerGrid);
        return quantized % _loopLengthSamples;
    }

    void Sequencer::updateLoopLength() {
        float secondsPerBeat = 60.0f / _bpm.load();
        _loopLengthSamples = static_cast<uint64_t>(secondsPerBeat * 4.0f * static_cast<float>(_loopLengthBars.load()) * _sampleRate);
    }
}
