#include "Log.h"
#include "WavetableSynthesizer.h"

namespace wavetablesynthesizer {

    void WavetableSynthesizer::play() {
        LOGD("play() called.");
        _isPlaying = true;
    }

    void WavetableSynthesizer::stop() {
        LOGD("stop() called.");
        _isPlaying = false;
    }

    bool WavetableSynthesizer::isPlaying() {
        LOGD("isPlaying() called.");
        return _isPlaying;
    }

    void WavetableSynthesizer::setFrequency(float frequencyInHz) {
        LOGD("setFrequency() called with %.2f Hz argument", frequencyInHz);
    }

    void WavetableSynthesizer::setVolume(float volumeInDb) {
        LOGD("setVolume() called with %.2f dB argument", volumeInDb);
    }

    void WavetableSynthesizer::setWavetable(Wavetable wavetable) {
        LOGD("setWavetable() called with %.d argument", static_cast<int>(wavetable));
    }
}