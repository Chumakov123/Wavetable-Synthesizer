#pragma once
#include <atomic>
#include "MathConstants.h"
#include <cmath>

namespace udaw {
    class Lfo {
    public:
        void setSampleRate(float sampleRate) { _sampleRate = sampleRate; }
        void setFrequency(float frequency) { _frequency.store(frequency, std::memory_order_relaxed); }

        float getNextSample() {
            const float frequency = _frequency.load(std::memory_order_relaxed);
            if (frequency <= 0.f) return 0.f;

            _phase += 2.f * PI * frequency / _sampleRate;
            if (_phase > 2.f * PI) _phase -= 2.f * PI;
            return std::sin(_phase);
        }

    private:
        float _phase = 0.f;
        std::atomic<float> _frequency{5.f};
        float _sampleRate = 48000.f;
    };
}
