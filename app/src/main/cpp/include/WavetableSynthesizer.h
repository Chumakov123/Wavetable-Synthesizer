#pragma once
#include <memory>
#include <mutex>
#include "Wavetable.h"
#include "WavetableFactory.h"

namespace wavetablesynthesizer {
    class WavetableOscillator;
    class AudioPlayer;

    constexpr  auto sampleRate = 48000;
    constexpr  auto MAX_VOICES = 8;

    class WavetableSynthesizer {
    public:
        WavetableSynthesizer();
        ~WavetableSynthesizer();
        void play();
        void stop();
        bool isPlaying() const;
        void setFrequency(float frequencyInHz);
        void setVolume(float volumeInDb);
        void setWavetable(Wavetable wavetable);
        void noteOn(float frequencyInHz);
        void noteOff(float frequencyInHz);

        void setAttackTime(float time);
        void setDecayTime(float time);
        void setSustainLevel(float level);
        void setReleaseTime(float time);
    private:
        std::atomic<bool> _isStreamOpen = false;
        std::atomic<bool> _isContinuousPlayActive = false;
        std::mutex _mutex;
        WavetableFactory _wavetableFactory;
        Wavetable _currentWavetable{Wavetable::SINE};
        float _amplitude = 1.f;

        float _attackTime = 0.01f;
        float _decayTime = 0.1f;
        float _sustainLevel = 0.7f;
        float _releaseTime = 0.3f;

        std::vector<std::shared_ptr<WavetableOscillator>> _voices;
        std::unique_ptr<AudioPlayer> _audioPlayer;
    };
}