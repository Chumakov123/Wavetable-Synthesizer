#include "AdsrEnvelope.h"
#include <algorithm>

namespace udaw {

    void AdsrEnvelope::noteOn() {
        _state.store(AdsrState::ATTACK, std::memory_order_release);
    }

    void AdsrEnvelope::noteOff() {
        if (_state.load() != AdsrState::IDLE) {
            _state.store(AdsrState::RELEASE, std::memory_order_release);
        }
    }

    void AdsrEnvelope::updateRates() {
        _attackRate = 1.0f / (std::max(0.001f, _attackTime) * _sampleRate);
        _decayRate = (1.0f - _sustainLevel) / (std::max(0.001f, _decayTime) * _sampleRate);
        _releaseRate = _sustainLevel / (std::max(0.001f, _releaseTime) * _sampleRate);
    }

    float AdsrEnvelope::getNextAmplitude() {
        AdsrState currentState = _state.load(std::memory_order_acquire);

        switch (currentState) {
            case AdsrState::ATTACK:
                _currentAmplitude += _attackRate;
                if (_currentAmplitude >= 1.0f) {
                    _currentAmplitude = 1.0f;
                    _state.store(AdsrState::DECAY, std::memory_order_release);
                }
                break;
            case AdsrState::DECAY:
                _currentAmplitude -= _decayRate;
                if (_currentAmplitude <= _sustainLevel) {
                    _currentAmplitude = _sustainLevel;
                    _state.store(AdsrState::SUSTAIN, std::memory_order_release);
                }
                break;
            case AdsrState::SUSTAIN:
                _currentAmplitude = _sustainLevel;
                break;
            case AdsrState::RELEASE:
                _currentAmplitude -= _releaseRate;
                if (_currentAmplitude <= 0.0f) {
                    _currentAmplitude = 0.0f;
                    _state.store(AdsrState::IDLE, std::memory_order_release);
                }
                break;
            case AdsrState::IDLE:
                _currentAmplitude = 0.0f;
                break;
        }
        return _currentAmplitude;
    }
}
