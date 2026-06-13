#include "SynthTrack.h"
#include <cmath>

namespace udaw {
    SynthTrack::SynthTrack(double sampleRate) : _sampleRate(sampleRate), _delayLine(sampleRate) {
        for (int i = 0; i < MAX_VOICES; ++i) {
            _voices.push_back(std::make_shared<WavetableOscillator>(
                _wavetableFactory.getWaveTable(_currentWavetable), static_cast<float>(sampleRate)));
        }
    }

    float SynthTrack::getSample() {
        float sample = 0.0f;
        {
            std::lock_guard<std::mutex> lock(_mutex);
            for (auto& voice : _voices) {
                sample += voice->getSample();
            }
        }
        return _delayLine.process(sample);
    }

    void SynthTrack::onPlaybackStopped() {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) {
            voice->onPlaybackStopped();
        }
    }

    void SynthTrack::noteOn(float frequencyInHz) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) {
            if (voice->isBusy() && std::abs(voice->getFrequency() - frequencyInHz) < 0.1f) {
                voice->noteOn();
                return;
            }
        }
        for (auto& voice : _voices) {
            if (!voice->isBusy()) {
                voice->setFrequency(frequencyInHz);
                voice->setAmplitude(_amplitude);
                voice->noteOn();
                return;
            }
        }
    }

    void SynthTrack::noteOff(float frequencyInHz) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) {
            if (std::abs(voice->getFrequency() - frequencyInHz) < 0.1f) {
                voice->noteOff();
            }
        }
    }

    void SynthTrack::setFrequency(float frequencyInHz) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) {
            if (voice->isBusy()) {
                voice->setFrequency(frequencyInHz);
            }
        }
    }

    void SynthTrack::stopAllNotes() {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) {
            voice->noteOff();
        }
    }

    void SynthTrack::setWavetable(Wavetable wavetable) {
        std::lock_guard<std::mutex> lock(_mutex);
        if (_currentWavetable != wavetable) {
            _currentWavetable = wavetable;
            auto table = _wavetableFactory.getWaveTable(wavetable);
            for (auto& voice : _voices) {
                voice->setWavetable(table);
            }
        }
    }

    float dbToAmplitude_Internal(float dB) {
        return std::pow(10.f, dB / 20.f);
    }

    void SynthTrack::setVolume(float volumeInDb) {
        std::lock_guard<std::mutex> lock(_mutex);
        _amplitude = dbToAmplitude_Internal(volumeInDb);
        for (auto& voice : _voices) {
            voice->setAmplitude(_amplitude);
        }
    }

    void SynthTrack::setAttackTime(float time) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) voice->setAttackTime(time);
    }

    void SynthTrack::setDecayTime(float time) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) voice->setDecayTime(time);
    }

    void SynthTrack::setSustainLevel(float level) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) voice->setSustainLevel(level);
    }

    void SynthTrack::setReleaseTime(float time) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) voice->setReleaseTime(time);
    }

    void SynthTrack::setLfoRate(float rate) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) voice->setLfoRate(rate);
    }

    void SynthTrack::setLfoDepth(float depth) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) voice->setLfoDepth(depth);
    }

    void SynthTrack::setTremoloDepth(float depth) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) voice->setTremoloDepth(depth);
    }

    void SynthTrack::setDelayTime(float seconds) {
        _delayLine.setDelayTime(seconds);
    }

    void SynthTrack::setDelayFeedback(float feedback) {
        _delayLine.setFeedback(feedback);
    }

    void SynthTrack::setDelayWet(float wet) {
        _delayLine.setWetLevel(wet);
    }

    bool SynthTrack::isBusy() const {
        for (auto& voice : _voices) {
            if (voice->isBusy()) return true;
        }
        return false;
    }
}
