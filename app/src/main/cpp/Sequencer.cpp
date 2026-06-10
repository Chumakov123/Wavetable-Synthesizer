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
                            _noteCallback(_receiver, event.frequency, event.isNoteOn);
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

    void Sequencer::recordNoteOn(float frequency) {
        if (!_isRecording.load()) return;

        std::lock_guard<std::mutex> lock(_eventsMutex);
        _events.push_back({_currentLoopSample, frequency, true});
    }

    void Sequencer::recordNoteOff(float frequency) {
        if (!_isRecording.load()) return;

        std::lock_guard<std::mutex> lock(_eventsMutex);
        _events.push_back({_currentLoopSample, frequency, false});
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

    void Sequencer::setBpm(float bpm) {
        _bpm.store(bpm);
        updateLoopLength();
    }

    void Sequencer::setLoopLengthBars(int bars) {
        _loopLengthBars.store(bars);
        updateLoopLength();
    }

    void Sequencer::updateLoopLength() {
        // Длина лупа в семплах = (60 / BPM) * 4 удара * количество тактов * sampleRate
        float secondsPerBeat = 60.0f / _bpm.load();
        _loopLengthSamples = static_cast<uint64_t>(secondsPerBeat * 4.0f * static_cast<float>(_loopLengthBars.load()) * _sampleRate);
    }
}
