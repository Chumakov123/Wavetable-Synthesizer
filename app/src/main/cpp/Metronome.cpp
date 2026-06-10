#include "Metronome.h"
#include "MathConstants.h"
#include <cmath>

namespace wavetablesynthesizer {
    Metronome::Metronome(double sampleRate) : _sampleRate(sampleRate) {
        updateSamplesPerBeat();
    }

    float Metronome::getSample() {
        if (!_isEnabled.load(std::memory_order_relaxed)) {
            _totalSamples = 0;
            return 0.0f;
        }

        float sample = 0.0f;

        // Генерируем звук щелчка
        if (_clickAmplitude > 0.0001f) {
            sample = std::sin(_clickPhase) * _clickAmplitude;
            _clickPhase += 2.0f * PI * _clickFrequency / static_cast<float>(_sampleRate);
            if (_clickPhase > 2.0f * PI) _clickPhase -= 2.0f * PI;
            _clickAmplitude *= _clickDecay;
        }

        if (_totalSamples % _samplesPerBeat == 0) {
            _clickAmplitude = 0.5f;
            _clickPhase = 0.0f;
        }

        _totalSamples++;
        return sample;
    }

    void Metronome::onPlaybackStopped() {
        _totalSamples = 0;
        _clickAmplitude = 0.0f;
    }

    void Metronome::setBpm(float bpm) {
        _bpm.store(bpm, std::memory_order_relaxed);
        updateSamplesPerBeat();
    }

    void Metronome::setEnabled(bool enabled) {
        _isEnabled.store(enabled, std::memory_order_relaxed);
        if (!enabled) {
            _clickAmplitude = 0.0f;
            _totalSamples = 0;
        }
    }

    void Metronome::updateSamplesPerBeat() {
        float bpm = _bpm.load(std::memory_order_relaxed);
        if (bpm <= 0) bpm = 120.0f;
        _samplesPerBeat = static_cast<uint64_t>(_sampleRate * 60.0f / bpm);
    }
}
