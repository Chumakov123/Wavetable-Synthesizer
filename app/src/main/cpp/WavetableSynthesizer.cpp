#include "Log.h"
#include <cmath>
#include "WavetableSynthesizer.h"
#include "OboeAudioPlayer.h"
#include "WavetableOscillator.h"
#include "Mixer.h"
#include "Sequencer.h"

namespace wavetablesynthesizer {
    WavetableSynthesizer::WavetableSynthesizer() {
        auto mixer = std::make_shared<Mixer>();
        for (int i = 0; i < MAX_VOICES; ++i) {
            auto voice = std::make_shared<WavetableOscillator>(
                _wavetableFactory.getWaveTable(_currentWavetable), sampleRate);
            voice->setAttackTime(_attackTime);
            voice->setDecayTime(_decayTime);
            voice->setSustainLevel(_sustainLevel);
            voice->setReleaseTime(_releaseTime);
            voice->setLfoRate(_lfoRate);
            voice->setLfoDepth(_lfoDepth);
            voice->setTremoloDepth(_tremoloDepth);
            _voices.push_back(voice);
            mixer->addSource(voice);
        }

        _metronome = std::make_shared<Metronome>(sampleRate);
        mixer->addSource(_metronome);

        _sequencer = std::make_shared<Sequencer>(sampleRate);
        _sequencer->setNoteCallback(sequencerCallback, this);
        mixer->setSequencer(_sequencer);

        _audioPlayer = std::make_unique<OboeAudioPlayer>(mixer, sampleRate);
    }

    WavetableSynthesizer::~WavetableSynthesizer() = default;

    void WavetableSynthesizer::play() {
        std::lock_guard<std::mutex> lock(_mutex);
        _isContinuousPlayActive = true;
        if (_isStreamOpen) {
            _voices[0]->noteOn(); // Для режима слайдера используем первый голос
            return;
        }

        LOGD("play() called.");
        _audioPlayer->stop();

        const auto result = _audioPlayer->play();
        if (result == 0) {
            _isStreamOpen = true;
            _voices[0]->noteOn();
        } else {
            LOGD("Could not start playback.");
        }
    }

    void WavetableSynthesizer::stop() {
        std::lock_guard<std::mutex> lock(_mutex);
        if (!_isContinuousPlayActive) return;
        LOGD("stop() called.");
        for (auto& voice : _voices) {
            voice->noteOff();
        }
        _isContinuousPlayActive = false;
    }

    bool WavetableSynthesizer::isPlaying() const {
        return _isContinuousPlayActive;
    }

    void WavetableSynthesizer::setFrequency(float frequencyInHz) {
        _voices[0]->setFrequency(frequencyInHz);
    }

    float dbToAmplitude(float dB) {
        return std::pow(10.f, dB / 20.f);
    }

    void WavetableSynthesizer::setVolume(float volumeInDb) {
        _amplitude = dbToAmplitude(volumeInDb);
        for (auto& voice : _voices) {
            voice->setAmplitude(_amplitude);
        }
    }

    void WavetableSynthesizer::setWavetable(Wavetable wavetable) {
        if (_currentWavetable != wavetable) {
            _currentWavetable = wavetable;
            auto table = _wavetableFactory.getWaveTable(wavetable);
            for (auto& voice : _voices) {
                voice->setWavetable(table);
            }
        }
    }

    void WavetableSynthesizer::noteOn(float frequencyInHz) {
        _sequencer->recordNoteOn(frequencyInHz);
        internalNoteOn(frequencyInHz);
    }

    void WavetableSynthesizer::internalNoteOn(float frequencyInHz) {
        std::lock_guard<std::mutex> lock(_mutex);

        // 1. Ищем, не играет ли уже эта нота
        for (auto& voice : _voices) {
            if (voice->isBusy() && std::abs(voice->getFrequency() - frequencyInHz) < 0.1f) {
                voice->noteOn();
                return;
            }
        }

        // 2. Ищем свободный голос
        for (auto& voice : _voices) {
            if (!voice->isBusy()) {
                voice->setFrequency(frequencyInHz);
                voice->setAmplitude(_amplitude);

                if (!_isStreamOpen) {
                    _audioPlayer->play();
                    _isStreamOpen = true;
                }
                voice->noteOn();
                return;
            }
        }
    }

    void WavetableSynthesizer::noteOff(float frequencyInHz) {
        _sequencer->recordNoteOff(frequencyInHz);
        internalNoteOff(frequencyInHz);
    }

    void WavetableSynthesizer::internalNoteOff(float frequencyInHz) {
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& voice : _voices) {
            if (std::abs(voice->getFrequency() - frequencyInHz) < 0.1f) {
                voice->noteOff();
            }
        }
    }

    void WavetableSynthesizer::setAttackTime(float time) {
        _attackTime = time;
        for (auto& voice : _voices) {
            voice->setAttackTime(time);
        }
    }

    void WavetableSynthesizer::setDecayTime(float time) {
        _decayTime = time;
        for (auto& voice : _voices) {
            voice->setDecayTime(time);
        }
    }

    void WavetableSynthesizer::setSustainLevel(float level) {
        _sustainLevel = level;
        for (auto& voice : _voices) {
            voice->setSustainLevel(level);
        }
    }

    void WavetableSynthesizer::setReleaseTime(float time) {
        _releaseTime = time;
        for (auto& voice : _voices) {
            voice->setReleaseTime(time);
        }
    }

    void WavetableSynthesizer::setLfoRate(float rate) {
        _lfoRate = rate;
        for (auto& voice : _voices) {
            voice->setLfoRate(rate);
        }
    }

    void WavetableSynthesizer::setLfoDepth(float depth) {
        _lfoDepth = depth;
        for (auto& voice : _voices) {
            voice->setLfoDepth(depth);
        }
    }

    void WavetableSynthesizer::setTremoloDepth(float depth) {
        _tremoloDepth = depth;
        for (auto& voice : _voices) {
            voice->setTremoloDepth(depth);
        }
    }

    void WavetableSynthesizer::setMetronomeEnabled(bool enabled) {
        _metronome->setEnabled(enabled);
    }

    void WavetableSynthesizer::setBpm(float bpm) {
        _metronome->setBpm(bpm);
        _sequencer->setBpm(bpm);
    }

    void WavetableSynthesizer::setRecording(bool enabled) {
        if (enabled) _sequencer->startRecording();
        else _sequencer->stopRecording();
    }

    void WavetableSynthesizer::setPlayback(bool enabled) {
        if (enabled) _sequencer->startPlayback();
        else _sequencer->stopPlayback();
    }

    void WavetableSynthesizer::clearSequence() {
        _sequencer->clear();
    }

    void WavetableSynthesizer::sequencerCallback(void* receiver, float frequency, bool isNoteOn) {
        auto* synth = static_cast<WavetableSynthesizer*>(receiver);
        if (isNoteOn) synth->internalNoteOn(frequency);
        else synth->internalNoteOff(frequency);
    }
}
