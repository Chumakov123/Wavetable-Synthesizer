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
        float interpolateLineary() const;
        void swapWavetableIfNecessary();

        float index = 0.f;
        std::atomic<float> indexIncrement{0.f};
        std::vector<float> waveTable;
        float sampleRate;
        std::atomic<float> amplitude{1.f};
        std::atomic<bool> swapWaveTable{false};
        std::vector<float> wavetableToSwap;
        std::atomic<bool> wavetableIsBeingSwapped{false};

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