#include "WavetableOscillator.h"
#include <cmath>
#include "MathConstants.h"

namespace wavetablesynthesizer {
    WavetableOscillator::WavetableOscillator(std::vector<float> waveTable, float sampleRate)
    : waveTable{std::move(waveTable)}, sampleRate{sampleRate} {
        _envelope.setSampleRate(sampleRate);
        _lfo.setSampleRate(sampleRate);
    }

    float WavetableOscillator::getSample() {
        if (index >= static_cast<float>(waveTable.size())) {
            index -= static_cast<float>(waveTable.size());
        }

        float sample = 0.f;

        if (isCrossfading.load(std::memory_order_acquire)) {
            float oldSample = interpolateLineary(waveTable, index);
            float newSample = interpolateLineary(wavetableToSwap, index);
            sample = oldSample * (1.f - crossfadeProgress) + newSample * crossfadeProgress;

            crossfadeProgress += crossfadeStep;
            if (crossfadeProgress >= 1.f) {
                isCrossfading.store(false, std::memory_order_release);
                std::swap(waveTable, wavetableToSwap);
                wavetableIsBeingSwapped.store(false, std::memory_order_release);
                swapWaveTable.store(false, std::memory_order_relaxed);
            }
        } else {
            if (swapWaveTable.load(std::memory_order_acquire)) {
                isCrossfading.store(true, std::memory_order_release);
                wavetableIsBeingSwapped.store(true, std::memory_order_release);
                crossfadeProgress = 0.f;
            }
            sample = interpolateLineary(waveTable, index);
        }

        const float lfoValue = _lfo.getNextSample();
        const float lfoModulation = lfoValue * _lfoDepth.load(std::memory_order_relaxed);
        index += indexIncrement.load(std::memory_order_relaxed) * (1.f + lfoModulation);

        const float target = targetAmplitude.load(std::memory_order_relaxed);

        // Плавное изменение громкости (Smoothing)
        float currentAmplitude = amplitude.load(std::memory_order_relaxed);
        currentAmplitude = 0.995f * currentAmplitude + 0.005f * target;
        amplitude.store(currentAmplitude, std::memory_order_relaxed);

        const float envelopeAmplitude = _envelope.getNextAmplitude();
        const float tremoloModulation = 1.f + lfoValue * _tremoloDepth.load(std::memory_order_relaxed);

        return currentAmplitude * envelopeAmplitude * sample * tremoloModulation;
    }

    float WavetableOscillator::interpolateLineary(const std::vector<float>& table, float indexValue) {
        if (table.empty()) return 0.f;
        const auto truncatedIndex = static_cast<std::size_t>(indexValue);
        const auto nextIndex = (truncatedIndex + 1u) % table.size();
        const auto nextIndexWeight = indexValue - static_cast<float>(truncatedIndex);
        return table[nextIndex] * nextIndexWeight + (1.f - nextIndexWeight) * table[truncatedIndex];
    }

    void WavetableOscillator::setWavetable(const std::vector<float> &wavetable) {
        swapWaveTable.store(false, std::memory_order_release);
        while (wavetableIsBeingSwapped.load(std::memory_order_acquire)){
        }
        wavetableToSwap = wavetable;
        swapWaveTable.store(true, std::memory_order_release);

    }

    void WavetableOscillator::setFrequency(float frequency) {
        _frequency = frequency;
        indexIncrement = frequency * static_cast<float>(waveTable.size()) /
                static_cast<float>(sampleRate);
    }

    void WavetableOscillator::setAmplitude(float newAmplitude) {
        targetAmplitude.store(newAmplitude);
    }

    void WavetableOscillator::onPlaybackStopped() {
        index = 0.f;
        amplitude.store(0.f, std::memory_order_relaxed);
        isCrossfading.store(false, std::memory_order_relaxed);
        wavetableIsBeingSwapped.store(false, std::memory_order_relaxed);
        swapWaveTable.store(false, std::memory_order_relaxed);
    }

    void WavetableOscillator::noteOn() {
        _envelope.noteOn();
    }

    void WavetableOscillator::noteOff() {
        _envelope.noteOff();
    }
}