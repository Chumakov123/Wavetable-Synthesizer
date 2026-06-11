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

        sample = std::tanh(sample * 1.5f);

        _phase += 2.0f * M_PI * _currentFreq / static_cast<float>(_sampleRate);
        if (_phase > 2.0f * M_PI) _phase -= 2.0f * M_PI;

        float pitchCoefficient = std::exp(-1.0f / (static_cast<float>(_sampleRate) * _pitchDecay));
        _currentFreq = _endFreq + (_currentFreq - _endFreq) * pitchCoefficient;

        float ampCoefficient = std::exp(-1.0f / (static_cast<float>(_sampleRate) * _ampDecay));
        _amplitude *= ampCoefficient;

        return sample * _amplitude;
    }

    // --- SnareDrum ---

    SnareDrum::SnareDrum(double sampleRate) : _sampleRate(sampleRate) {}

    void SnareDrum::trigger() {
        _phase = 0.0f;
        _bodyAmp = 1.0f;
        _noiseAmp = 1.0f;
    }

    float SnareDrum::getSample() {
        if (_bodyAmp <= 0.0001f && _noiseAmp <= 0.0001f) {
            _bodyAmp = 0.0f;
            _noiseAmp = 0.0f;
            return 0.0f;
        }

        float bodySample = std::sin(_phase) * _bodyAmp * 0.5f;
        _phase += 2.0f * M_PI * _bodyFreq / static_cast<float>(_sampleRate);
        if (_phase > 2.0f * M_PI) _phase -= 2.0f * M_PI;

        float noiseSample = _noise.getSample() * _noiseAmp * 0.5f;

        float bodyCoeff = std::exp(-1.0f / (static_cast<float>(_sampleRate) * _bodyDecay));
        float noiseCoeff = std::exp(-1.0f / (static_cast<float>(_sampleRate) * _noiseDecay));

        _bodyAmp *= bodyCoeff;
        _noiseAmp *= noiseCoeff;

        return bodySample + noiseSample;
    }

    // --- HiHat ---

    HiHat::HiHat(double sampleRate) : _sampleRate(sampleRate) {}

    void HiHat::trigger() {
        _amplitude = 1.0f;
    }

    float HiHat::getSample() {
        if (_amplitude <= 0.0001f) {
            _amplitude = 0.0f;
            return 0.0f;
        }

        float noise = _noise.getSample();

        // High Pass Filter (простейший)
        float out = noise - _hpfState;
        _hpfState = noise * 0.8f; // Коэффициент фильтрации

        float coeff = std::exp(-1.0f / (static_cast<float>(_sampleRate) * _decay));
        _amplitude *= coeff;

        return out * _amplitude * 0.3f;
    }

    // --- DrumTrack ---

    DrumTrack::DrumTrack(double sampleRate) : _kick(sampleRate), _snare(sampleRate), _hat(sampleRate) {}

    float DrumTrack::getSample() {
        return (_kick.getSample() + _snare.getSample() + _hat.getSample()) * _gain.load();
    }

    void DrumTrack::onPlaybackStopped() {
        // Ничего не делаем
    }

    void DrumTrack::triggerKick() {
        _kick.trigger();
    }

    void DrumTrack::triggerSnare() {
        _snare.trigger();
    }

    void DrumTrack::triggerHat() {
        _hat.trigger();
    }

    void DrumTrack::setVolume(float volumeInDb) {
        _gain.store(std::pow(10.0f, volumeInDb / 20.0f));
    }
}
