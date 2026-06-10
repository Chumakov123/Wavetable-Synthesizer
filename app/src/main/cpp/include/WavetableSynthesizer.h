#pragma once
#include <memory>
#include <mutex>
#include "Wavetable.h"
#include "WavetableFactory.h"
#include "Metronome.h"
#include "Sequencer.h"

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

        void setLfoRate(float rate);
        void setLfoDepth(float depth);
        void setTremoloDepth(float depth);

        void setMetronomeEnabled(bool enabled);
        void setBpm(float bpm);

        // Методы для Секвенсора
        void setRecording(bool enabled);
        void setPlayback(bool enabled);
        void clearSequence();
        void setQuantizationMode(int mode);

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

        float _lfoRate = 5.0f;
        float _lfoDepth = 0.0f;
        float _tremoloDepth = 0.0f;

        std::vector<std::shared_ptr<WavetableOscillator>> _voices;
        std::shared_ptr<Metronome> _metronome;
        std::shared_ptr<Sequencer> _sequencer;
        std::unique_ptr<AudioPlayer> _audioPlayer;

        // Внутренний метод для нот (чтобы избежать рекурсии при записи)
        void internalNoteOn(float frequencyInHz);
        void internalNoteOff(float frequencyInHz);
        static void sequencerCallback(void* receiver, float frequency, bool isNoteOn);
    };
}