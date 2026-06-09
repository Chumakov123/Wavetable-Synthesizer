#pragma once

#include <vector>
#include <atomic>
#include "AudioSource.h"

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
    private:
        float interpolateLineary(const std::vector<float>& table, float indexValue) const;

        float index = 0.f;
        std::atomic<float> indexIncrement{0.f};
        std::vector<float> waveTable;
        float sampleRate;
        std::atomic<float> targetAmplitude{1.f};
        std::atomic<float> amplitude{0.f};
        std::atomic<bool> swapWaveTable{false};
        std::vector<float> wavetableToSwap;
        std::atomic<bool> wavetableIsBeingSwapped{false};

        std::atomic<bool> isCrossfading{false};
        float crossfadeProgress = 0.f;
        const float crossfadeStep = 0.0005f; // Скорость кроссфейда (около 40мс при 48кГц)
    };

    class A4Oscillator : public AudioSource {
    public:
        explicit  A4Oscillator(float sampleRate);

        float getSample() override;
        void onPlaybackStopped() override;

    private:
        float _phase = 0.f;
        float _phaseIncrement = 0.f;
    };
}