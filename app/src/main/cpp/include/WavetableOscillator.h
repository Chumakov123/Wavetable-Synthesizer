#pragma once

#include <vector>
#include <atomic>
#include "AudioSource.h"
#include "AdsrEnvelope.h"
#include "Lfo.h"

namespace  wavetablesynthesizer {
    class WavetableOscillator : public AudioSource {
    public:
        WavetableOscillator() = default;
        WavetableOscillator(std::vector<float> waveTable, float sampleRate);

        float getSample() override;

        void onPlaybackStopped() override;

        virtual void setFrequency(float frequency);
        virtual void setAmplitude(float newAmplitude);
        virtual void setWavetable(const std::vector<float>& wavetable);

        void noteOn();
        void noteOff();
        void resetEnvelope() { _envelope.reset(); }
        bool isBusy() const { return !_envelope.isIdle(); }
        float getFrequency() const { return _frequency; }

        void setAttackTime(float time) { _envelope.setAttackTime(time); }
        void setDecayTime(float time) { _envelope.setDecayTime(time); }
        void setSustainLevel(float level) { _envelope.setSustainLevel(level); }
        void setReleaseTime(float time) { _envelope.setReleaseTime(time); }

        void setLfoRate(float rate) { _lfo.setFrequency(rate); }
        void setLfoDepth(float depth) { _lfoDepth.store(depth, std::memory_order_relaxed); }
        void setTremoloDepth(float depth) { _tremoloDepth.store(depth, std::memory_order_relaxed); }
    private:
        static float interpolateLineary(const std::vector<float>& table, float indexValue);

        float index = 0.f;
        std::atomic<float> indexIncrement{0.f};
        float _frequency = 0.f;
        std::vector<float> waveTable;
        float sampleRate = 48000.f;
        std::atomic<float> targetAmplitude{1.f};
        std::atomic<float> amplitude{0.f};
        std::atomic<bool> swapWaveTable{false};
        std::vector<float> wavetableToSwap;
        std::atomic<bool> wavetableIsBeingSwapped{false};

        std::atomic<bool> isCrossfading{false};
        float crossfadeProgress = 0.f;
        const float crossfadeStep = 0.0005f; // Скорость кроссфейда (около 40мс при 48кГц)

        AdsrEnvelope _envelope;
        Lfo _lfo;
        std::atomic<float> _lfoDepth{0.f};
        std::atomic<float> _tremoloDepth{0.f};
    };
}
