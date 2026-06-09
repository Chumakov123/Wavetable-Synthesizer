#include "Log.h"
#include <cmath>
#include <thread>
#include <chrono>
#include "WavetableSynthesizer.h"
#include "OboeAudioPlayer.h"
#include "WavetableOscillator.h"

namespace wavetablesynthesizer {
    WavetableSynthesizer::WavetableSynthesizer()
    : _oscillator{std::make_shared<WavetableOscillator>(
            _wavetableFactory.getWaveTable(_currentWavetable)
            ,sampleRate)},
      _audioPlayer{
              std::make_unique<OboeAudioPlayer>(_oscillator, sampleRate)} {}

    WavetableSynthesizer::~WavetableSynthesizer() = default;

    void WavetableSynthesizer::play() {
        std::lock_guard<std::mutex> lock(_mutex);
        _isContinuousPlayActive = true;
        if (_isStreamOpen) {
            _oscillator->noteOn();
            return;
        }

        LOGD("play() called.");
        _oscillator->resetEnvelope();
        _oscillator->setAmplitude(_amplitude);
        _audioPlayer->stop();

        const auto result = _audioPlayer->play();
        if (result == 0) {
            _isStreamOpen = true;
            _oscillator->noteOn();
        } else {
            LOGD("Could not start playback.");
        }
    }

    void WavetableSynthesizer::stop() {
        std::lock_guard<std::mutex> lock(_mutex);
        if (!_isContinuousPlayActive) return;
        LOGD("stop() called.");
        _oscillator->noteOff();
        _isContinuousPlayActive = false;
    }

    bool WavetableSynthesizer::isPlaying() const {
        return _isContinuousPlayActive;
    }

    void WavetableSynthesizer::setFrequency(float frequencyInHz) {
        _oscillator->setFrequency(frequencyInHz);
        //LOGD("setFrequency() called with %.2f Hz argument", frequencyInHz);
    }

    float dbToAmplitude(float dB) {
        return std::pow(10.f, dB / 20.f);
    }

    void WavetableSynthesizer::setVolume(float volumeInDb) {
        _amplitude = dbToAmplitude(volumeInDb);
        _oscillator->setAmplitude(_amplitude);
        //LOGD("setVolume() called with %.2f dB argument", volumeInDb);
    }

    void WavetableSynthesizer::setWavetable(Wavetable wavetable) {
        if (_currentWavetable != wavetable) {
            _currentWavetable = wavetable;
            _oscillator->setWavetable(_wavetableFactory.getWaveTable(wavetable));
        }

        //LOGD("setWavetable() called with %.d argument", static_cast<int>(wavetable));
    }

    void WavetableSynthesizer::noteOn() {
        std::lock_guard<std::mutex> lock(_mutex);
        if (_isStreamOpen) {
            _oscillator->noteOn();
            return;
        }

        LOGD("noteOn() opening stream.");
        _oscillator->resetEnvelope();
        _oscillator->setAmplitude(_amplitude);
        _audioPlayer->stop();

        const auto result = _audioPlayer->play();
        if (result == 0) {
            _isStreamOpen = true;
            _oscillator->noteOn();
        }
    }

    void WavetableSynthesizer::noteOff() {
        std::lock_guard<std::mutex> lock(_mutex);
        if (_isContinuousPlayActive) return; // Не выключаем, если нажата кнопка Play
        _oscillator->noteOff();
    }
}