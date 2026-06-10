#include "DrumSynth.h"
#include <algorithm>

namespace wavetablesynthesizer {

    KickDrum::KickDrum(double sampleRate) : _sampleRate(sampleRate) {}

    void KickDrum::trigger() {
        _phase = 0.0f;
        _amplitude = 1.0f;
        _currentFreq = _startFreq;
    }

    float KickDrum::getSample() {
        if (_amplitude <= 0.0001f) {
            _amplitude = 0.0f;
            return 0.0f;
        }

        float sample = std::sin(_phase);

        // Добавим небольшое искажение для "характера" (soft clipping)
        sample = std::tanh(sample * 1.5f);

        // Обновляем фазу
        _phase += 2.0f * M_PI * _currentFreq / static_cast<float>(_sampleRate);
        if (_phase > 2.0f * M_PI) _phase -= 2.0f * M_PI;

        // Падение частоты (Pitch Envelope)
        // Чем больше _pitchDecay, тем медленнее падает частота
        float pitchCoefficient = std::exp(-1.0f / (static_cast<float>(_sampleRate) * _pitchDecay));
        _currentFreq = _endFreq + (_currentFreq - _endFreq) * pitchCoefficient;

        // Затухание громкости (Amplitude Envelope)
        float ampCoefficient = std::exp(-1.0f / (static_cast<float>(_sampleRate) * _ampDecay));
        _amplitude *= ampCoefficient;

        return sample * _amplitude;
    }

    // --- DrumTrack ---

    DrumTrack::DrumTrack(double sampleRate) : _kick(sampleRate) {}

    float DrumTrack::getSample() {
        return _kick.getSample();
    }

    void DrumTrack::onPlaybackStopped() {
        // Ничего не делаем
    }

    void DrumTrack::triggerKick() {
        _kick.trigger();
    }
}
