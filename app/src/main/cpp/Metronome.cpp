#include "Metronome.h"
#include "MathConstants.h"
#include <cmath>

namespace udaw {
    Metronome::Metronome(double sampleRate) : _sampleRate(sampleRate) {
    }

    float Metronome::getSample() {
        if (!_isEnabled.load(std::memory_order_relaxed) || _clickAmplitude <= 0.0001f) {
            return 0.0f;
        }

        float sample = std::sin(_clickPhase) * _clickAmplitude;
        _clickPhase += 2.0f * PI * _currentClickFrequency / static_cast<float>(_sampleRate);
        if (_clickPhase > 2.0f * PI) _clickPhase -= 2.0f * PI;
        _clickAmplitude *= _clickDecay;

        return sample;
    }

    void Metronome::triggerClick(bool accented) {
        _clickAmplitude = accented ? 0.7f : 0.4f;
        _currentClickFrequency = accented ? _clickFrequency * 1.5f : _clickFrequency;
        _clickPhase = 0.0f;
    }

    void Metronome::onPlaybackStopped() {
        _clickAmplitude = 0.0f;
    }

    void Metronome::setEnabled(bool enabled) {
        _isEnabled.store(enabled, std::memory_order_relaxed);
        if (!enabled) {
            _clickAmplitude = 0.0f;
        }
    }
}
