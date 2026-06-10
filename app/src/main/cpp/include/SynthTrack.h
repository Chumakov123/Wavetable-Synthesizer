#pragma once
#include <vector>
#include <memory>
#include <mutex>
#include "AudioSource.h"
#include "WavetableOscillator.h"
#include "Wavetable.h"
#include "WavetableFactory.h"
#include "DelayLine.h"

namespace wavetablesynthesizer {
    class SynthTrack : public AudioSource {
    public:
        SynthTrack(double sampleRate);
        float getSample() override;
        void onPlaybackStopped() override;

        void noteOn(float frequencyInHz);
        void noteOff(float frequencyInHz);
        void stopAllNotes();

        void setWavetable(Wavetable wavetable);
        void setVolume(float volumeInDb);

        void setAttackTime(float time);
        void setDecayTime(float time);
        void setSustainLevel(float level);
        void setReleaseTime(float time);

        void setLfoRate(float rate);
        void setLfoDepth(float depth);
        void setTremoloDepth(float depth);

        // FX Delay
        void setDelayTime(float seconds);
        void setDelayFeedback(float feedback);
        void setDelayWet(float wet);

        bool isBusy() const;

    private:
        static constexpr int MAX_VOICES = 8;
        double _sampleRate;
        std::vector<std::shared_ptr<WavetableOscillator>> _voices;
        WavetableFactory _wavetableFactory;
        Wavetable _currentWavetable{Wavetable::SINE};
        float _amplitude = 0.06f; // Default volume

        DelayLine _delayLine;

        std::mutex _mutex;
    };
}
