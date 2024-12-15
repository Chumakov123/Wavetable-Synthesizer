#include "Log.h"
#include <cmath>
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
        LOGD("play() called.");
        const auto result = _audioPlayer->play();
        if (result == 0) {
            _isPlaying = true;
        } else {
            LOGD("Could not start playbacl.");
        }
    }

    void WavetableSynthesizer::stop() {
        std::lock_guard<std::mutex> lock(_mutex);
        LOGD("stop() called.");
        _audioPlayer->stop();
        _isPlaying = false;
    }

    bool WavetableSynthesizer::isPlaying() const {
        LOGD("isPlaying() called.");
        return _isPlaying;
    }

    void WavetableSynthesizer::setFrequency(float frequencyInHz) {
        _oscillator->setFrequency(frequencyInHz);
        //LOGD("setFrequency() called with %.2f Hz argument", frequencyInHz);
    }

    float dbToAmplitude(float dB) {
        return std::pow(10.f, dB / 20.f);
    }

    void WavetableSynthesizer::setVolume(float volumeInDb) {
        const auto amplitude = dbToAmplitude(volumeInDb);
        _oscillator->setAmplitude(amplitude);
        //LOGD("setVolume() called with %.2f dB argument", volumeInDb);
    }

    void WavetableSynthesizer::setWavetable(Wavetable wavetable) {
        if (_currentWavetable != wavetable) {
            _currentWavetable = wavetable;
            _oscillator->setWavetable(_wavetableFactory.getWaveTable(wavetable));
        }

        //LOGD("setWavetable() called with %.d argument", static_cast<int>(wavetable));
    }
}