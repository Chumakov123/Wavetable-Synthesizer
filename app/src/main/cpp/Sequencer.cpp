#include "Sequencer.h"
#include <algorithm>

namespace wavetablesynthesizer {

    Sequencer::Sequencer(double sampleRate) : _sampleRate(sampleRate) {
        updateLoopLength();
    }

    void Sequencer::process(uint64_t currentFrame, uint32_t numFrames) {
        if (!_isPlaying.load(std::memory_order_relaxed) && !_isRecording.load(std::memory_order_relaxed)) {
            return;
        }

        std::lock_guard<std::mutex> lock(_eventsMutex);
        for (uint32_t i = 0; i < numFrames; ++i) {
            if (_isPlaying.load(std::memory_order_relaxed)) {
                // В реальном времени лучше использовать lock-free очередь или
                // отсортированный список с итератором, но для начала используем простой поиск.
                for (const auto& event : _events) {
                    if (event.timestamp == _currentLoopSample) {
                        if (_noteCallback) {
                            _noteCallback(_receiver, event.trackId, event.frequency, event.isNoteOn);
                        }
                    }
                }
            }

            _currentLoopSample++;
            if (_currentLoopSample >= _loopLengthSamples) {
                _currentLoopSample = 0;
            }
        }
    }

    void Sequencer::recordNoteOn(int trackId, float frequency) {
        if (!_isRecording.load()) return;

        std::lock_guard<std::mutex> lock(_eventsMutex);
        uint64_t timestamp = getQuantizedTimestamp(_currentLoopSample);
        _events.push_back({timestamp, frequency, true, trackId});
    }

    void Sequencer::recordNoteOff(int trackId, float frequency) {
        if (!_isRecording.load()) return;

        std::lock_guard<std::mutex> lock(_eventsMutex);
        uint64_t timestamp = getQuantizedTimestamp(_currentLoopSample);
        _events.push_back({timestamp, frequency, false, trackId});
    }

    void Sequencer::startRecording() {
        _isRecording.store(true);
        _isPlaying.store(true); // При записи всегда играем то, что уже есть
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
    }

    void Sequencer::clear() {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        _events.clear();
        _currentLoopSample = 0;
    }

    void Sequencer::clearTrack(int trackId) {
        std::lock_guard<std::mutex> lock(_eventsMutex);
        _events.erase(std::remove_if(_events.begin(), _events.end(),
                                     [trackId](const MidiEvent& e) { return e.trackId == trackId; }),
                      _events.end());
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

        // Округляем до ближайшей сетки
        auto quantized = static_cast<uint64_t>(std::round(static_cast<float>(timestamp) / samplesPerGrid) * samplesPerGrid);

        // Гарантируем, что не вышли за границы лупа
        return quantized % _loopLengthSamples;
    }

    void Sequencer::updateLoopLength() {
        // Длина лупа в семплах = (60 / BPM) * 4 удара * количество тактов * sampleRate
        float secondsPerBeat = 60.0f / _bpm.load();
        _loopLengthSamples = static_cast<uint64_t>(secondsPerBeat * 4.0f * static_cast<float>(_loopLengthBars.load()) * _sampleRate);
    }
}
