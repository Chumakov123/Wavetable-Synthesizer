#pragma once

#include "AudioSource.h"
#include <atomic>
#include <cmath>

namespace wavetablesynthesizer {
    class Metronome : public AudioSource {
    public:
        Metronome(double sampleRate);

        float getSample() override;
        void onPlaybackStopped() override;

        void setBpm(float bpm);
        void setEnabled(bool enabled);
        bool isEnabled() const { return _isEnabled.load(); }

    private:
        double _sampleRate;
        std::atomic<float> _bpm{120.0f};
        std::atomic<bool> _isEnabled{false};

        uint64_t _totalSamples = 0;
        uint64_t _samplesPerBeat = 0;

        // Параметры "щелчка"
        float _clickPhase = 0.0f;
        float _clickAmplitude = 0.0f;
        const float _clickFrequency = 1000.0f;
        const float _clickDecay = 0.995f; // Быстрое затухание

        void updateSamplesPerBeat();
    };
}
