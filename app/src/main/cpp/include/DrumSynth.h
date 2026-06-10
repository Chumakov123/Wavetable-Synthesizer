#pragma once
#include <atomic>
#include <cmath>
#include "AudioSource.h"

namespace wavetablesynthesizer {

    class KickDrum {
    public:
        KickDrum(double sampleRate);
        void trigger();
        float getSample();
        bool isPlaying() const { return _amplitude > 0.0001f; }

        void setPitchDecay(float value) { _pitchDecay = value; }
        void setAmpDecay(float value) { _ampDecay = value; }
        void setStartFreq(float value) { _startFreq = value; }

    private:
        double _sampleRate;
        float _phase = 0.0f;
        float _amplitude = 0.0f;
        float _currentFreq = 0.0f;

        float _startFreq = 150.0f;
        float _endFreq = 40.0f;
        float _pitchDecay = 0.05f; // Постоянная времени для падения частоты
        float _ampDecay = 0.15f;   // Постоянная времени для затухания громкости
    };

    class DrumTrack : public AudioSource {
    public:
         DrumTrack(double sampleRate);
         float getSample() override;
         void onPlaybackStopped() override;

         void triggerKick();
         // Будущие методы:
         // void triggerSnare();
         // void triggerHat();

    private:
         KickDrum _kick;
    };
}
