#include "Sequencer.h"
#include <algorithm>

namespace wavetablesynthesizer {

    Sequencer::Sequencer(double sampleRate) : _sampleRate(sampleRate) {
        _patterns.emplace_back();
        updateLoopLength();
    }

    void Sequencer::process(uint64_t currentFrame, uint32_t numFrames) {
        if (!_isPlaying.load(std::memory_order_relaxed) &&
            !_isRecording.load(std::memory_order_relaxed)) {
            return;
        }

        std::lock_guard<std::mutex> lock(_eventsMutex);
        for (uint32_t i = 0; i < numFrames; ++i) {

            // Логика метронома (работает и при Play/Rec)
            if (_metronome && _metronome->isEnabled()) {
                if (_currentLoopSample % _samplesPerBeat == 0) {
                    bool accented = (_currentLoopSample % (_samplesPerBeat * 4) == 0);
                    _metronome->triggerClick(accented);
                }
            }

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

                    if (_noteCallback) {
                        for (int t = 0; t < 4; ++t) {
                            _noteCallback(_receiver, -2, 0.0f, false);
                        }
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

        uint64_t timestamp = _currentLoopSample;

        // Smart Wrap: если мы нажали ноту в самом конце лупа (антиципация)
        // Притягиваем её к началу (0), если до конца осталось меньше 50мс
        auto thresholdSamples = static_cast<uint64_t>(0.05 * _sampleRate);
        if (timestamp > (_loopLengthSamples - thresholdSamples)) {
            timestamp = 0;
        } else {
            timestamp = getQuantizedTimestamp(timestamp);
        }

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

        uint64_t timestamp = _currentLoopSample;

        // Smart Wrap для барабанов
        auto thresholdSamples = static_cast<uint64_t>(0.05 * _sampleRate);
        if (timestamp > (_loopLengthSamples - thresholdSamples)) {
            timestamp = 0;
        } else {
            timestamp = getQuantizedTimestamp(timestamp);
        }

        int activePattern = _activePatternId.load();
        if (activePattern >= 0 && activePattern < _patterns.size()) {
            _patterns[activePattern].events.push_back({timestamp, static_cast<float>(drumId), true, -1, true});
        }
    }

    void Sequencer::startRecording() {
        // Гасим все ноты перед стартом записи, чтобы избежать залипаний
        if (_noteCallback) {
            for (int t = 0; t < 4; ++t) {
                _noteCallback(_receiver, -2, 0.0f, false);
            }
        }

        _currentLoopSample = 0;
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

        if (_noteCallback) {
            for (int t = 0; t < 4; ++t) {
                _noteCallback(_receiver, -2, 0.0f, false);
            }
        }

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
        std::lock_guard<std::mutex> lock(_eventsMutex);

        if (_isArrangementMode.load() == enabled) return;

        if (_noteCallback) {
            for (int t = 0; t < 4; ++t) {
                _noteCallback(_receiver, -2, 0.0f, false);
            }
        }

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

        if (_activePatternId.load() == patternId) return;

        // Если мы НЕ в режиме аранжировки, то переключение паттерна
        // должно сбрасывать ноты и начинать луп заново.
        if (!_isArrangementMode.load()) {
            if (_noteCallback) {
                for (int t = 0; t < 4; ++t) {
                    _noteCallback(_receiver, -2, 0.0f, false);
                }
            }
            _currentLoopSample = 0;
        }

        while (_patterns.size() <= patternId) {
            _patterns.emplace_back();
        }
        _activePatternId.store(patternId);
    }

    void Sequencer::copyPattern(int sourceId, int targetId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (sourceId < 0 || sourceId >= _patterns.size()) return;

        while (_patterns.size() <= targetId) {
            _patterns.emplace_back();
        }
        _patterns[targetId] = _patterns[sourceId];
    }

    void Sequencer::removePattern(int patternId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return;
        _patterns[patternId].events.clear();
    }

    void Sequencer::clearAllPatterns() {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        _patterns.clear();
        _patterns.emplace_back();
        _activePatternId.store(0);
        _playlist.clear();
        _currentPlaylistIndex = 0;
        _currentLoopSample = 0;
    }

    int Sequencer::getEventCount(int patternId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return 0;
        return static_cast<int>(_patterns[patternId].events.size());
    }

    MidiEvent Sequencer::getEvent(int patternId, int eventIndex) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return {};
        const auto& events = _patterns[patternId].events;
        if (eventIndex < 0 || eventIndex >= events.size()) return {};
        return events[eventIndex];
    }

    void Sequencer::updateEventTimestamp(int patternId, int eventIndex, uint64_t newTimestamp) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return;
        auto& events = _patterns[patternId].events;
        if (eventIndex < 0 || eventIndex >= events.size()) return;
        events[eventIndex].timestamp = newTimestamp % _loopLengthSamples;
    }

    void Sequencer::updateEventFrequency(int patternId, int eventIndex, float newFrequency) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return;
        auto& events = _patterns[patternId].events;
        if (eventIndex < 0 || eventIndex >= events.size()) return;
        events[eventIndex].frequency = newFrequency;
    }

    void Sequencer::deleteEvent(int patternId, int eventIndex) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return;
        auto& events = _patterns[patternId].events;
        if (eventIndex < 0 || eventIndex >= events.size()) return;
        events.erase(events.begin() + eventIndex);
    }

    int Sequencer::addEvent(int patternId, uint64_t timestamp, float frequency, bool isNoteOn, int trackId, bool isDrum) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        while (_patterns.size() <= patternId) {
            _patterns.emplace_back();
        }
        _patterns[patternId].events.push_back({timestamp % _loopLengthSamples, frequency, isNoteOn, trackId, isDrum});
        return static_cast<int>(_patterns[patternId].events.size() - 1);
    }

    void Sequencer::quantizePattern(int patternId, QuantizationMode mode) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        if (patternId < 0 || patternId >= _patterns.size()) return;
        if (mode == QuantizationMode::None) return;

        auto& events = _patterns[patternId].events;

        float bpm = _bpm.load();
        float samplesPerBeat = static_cast<float>(_sampleRate) * 60.0f / bpm;
        float samplesPerGrid = (mode == QuantizationMode::Beat_1_16) ? (samplesPerBeat / 4.0f) : (samplesPerBeat / 8.0f);

        for (auto& event : events) {
            auto quantized = static_cast<uint64_t>(std::round(static_cast<float>(event.timestamp) / samplesPerGrid) * samplesPerGrid);
            event.timestamp = quantized % _loopLengthSamples;
        }
    }

    uint64_t Sequencer::getTotalArrangementSamples() const {
        return _playlist.size() * _loopLengthSamples;
    }

    void Sequencer::resetForRendering() {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        _currentLoopSample = 0;
        _currentPlaylistIndex = 0;
        _isPlaying.store(true);
        _isArrangementMode.store(true);
        _isRecording.store(false);
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
        float bpm = _bpm.load();
        float secondsPerBeat = 60.0f / bpm;
        _samplesPerBeat = static_cast<uint64_t>(secondsPerBeat * _sampleRate);
        _loopLengthSamples = static_cast<uint64_t>(secondsPerBeat * 4.0f * static_cast<float>(_loopLengthBars.load()) * _sampleRate);
    }
}
