#pragma once
#include <atomic>
#include <cmath>
#include <random>
#include "AudioSource.h"

namespace wavetablesynthesizer {

    class WhiteNoise {
    public:
        WhiteNoise() : _dist(-1.0f, 1.0f) {}
        float getSample() { return _dist(_engine); }
    private:
        std::mt19937 _engine{std::random_device{}()};
        std::uniform_real_distribution<float> _dist;
    };

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
        float _pitchDecay = 0.05f;
        float _ampDecay = 0.15f;
    };

    class SnareDrum {
    public:
        SnareDrum(double sampleRate);
        void trigger();
        float getSample();

    private:
        double _sampleRate;
        float _phase = 0.0f;
        float _bodyAmp = 0.0f;
        float _noiseAmp = 0.0f;

        WhiteNoise _noise;

        const float _bodyFreq = 180.0f;
        const float _bodyDecay = 0.08f;
        const float _noiseDecay = 0.15f;
    };

    class HiHat {
    public:
        HiHat(double sampleRate);
        void trigger();
        float getSample();

    private:
        double _sampleRate;
        float _amplitude = 0.0f;
        WhiteNoise _noise;
        float _hpfState = 0.0f;

        const float _decay = 0.05f;
    };

    class DrumTrack : public AudioSource {
    public:
         DrumTrack(double sampleRate);
         float getSample() override;
         void onPlaybackStopped() override;

         void triggerKick();
         void triggerSnare();
         void triggerHat();

    private:
         KickDrum _kick;
         SnareDrum _snare;
         HiHat _hat;
    };
}
