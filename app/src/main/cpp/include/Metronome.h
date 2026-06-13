#pragma once

#include "AudioSource.h"
#include <atomic>
#include <cmath>

namespace udaw {
    class Metronome : public AudioSource {
    public:
        Metronome(double sampleRate);

        float getSample() override;
        void onPlaybackStopped() override;

        void triggerClick(bool accented); // Ручной триггер клика
        void setEnabled(bool enabled);
        bool isEnabled() const { return _isEnabled.load(); }

    private:
        double _sampleRate;
        std::atomic<bool> _isEnabled{false};

        // Параметры "щелчка"
        float _clickPhase = 0.0f;
        float _clickAmplitude = 0.0f;
        float _currentClickFrequency = 1000.0f;
        const float _clickFrequency = 1000.0f;
        const float _clickDecay = 0.995f;
    };
}
