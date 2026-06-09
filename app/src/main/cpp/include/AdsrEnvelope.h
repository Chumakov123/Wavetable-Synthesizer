#pragma once
#include <atomic>

namespace wavetablesynthesizer {
    enum class AdsrState {
        IDLE, ATTACK, DECAY, SUSTAIN, RELEASE
    };

    class AdsrEnvelope {
    public:
        void setSampleRate(float sampleRate) { _sampleRate = sampleRate; updateRates(); }

        void noteOn();
        void noteOff();
        float getNextAmplitude();

        bool isIdle() const { return _state == AdsrState::IDLE; }

        void setAttackTime(float time) { _attackTime = time; updateRates(); }
        void setDecayTime(float time) { _decayTime = time; updateRates(); }
        void setSustainLevel(float level) { _sustainLevel = level; }
        void setReleaseTime(float time) { _releaseTime = time; updateRates(); }

        void reset() {
            _currentAmplitude = 0.f;
            _state.store(AdsrState::IDLE, std::memory_order_release);
        }

    private:
        void updateRates();

        std::atomic<AdsrState> _state{AdsrState::IDLE};
        float _currentAmplitude = 0.f;
        float _sampleRate = 48000.f;

        float _attackTime = 0.01f;  // сек
        float _decayTime = 0.1f;    // сек
        float _sustainLevel = 0.7f; // 0.0 - 1.0
        float _releaseTime = 0.3f;  // сек

        float _attackRate = 0.f;
        float _decayRate = 0.f;
        float _releaseRate = 0.f;
    };
}
