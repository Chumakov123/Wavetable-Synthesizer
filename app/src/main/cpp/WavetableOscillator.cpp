#include "WavetableOscillator.h"
#include <cmath>
#include "MathConstants.h"

namespace wavetablesynthesizer {
    WavetableOscillator::WavetableOscillator(std::vector<float> waveTable, float sampleRate)
    : waveTable{std::move(waveTable)}, sampleRate{sampleRate} {}

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

        index += indexIncrement.load(std::memory_order_relaxed);

        const float target = targetAmplitude.load(std::memory_order_relaxed);
        float currentAmplitude = amplitude.load(std::memory_order_relaxed);
        currentAmplitude = 0.995f * currentAmplitude + 0.005f * target;

        if (std::abs(currentAmplitude - target) < 0.0001f) {
            currentAmplitude = target;
        }

        amplitude.store(currentAmplitude, std::memory_order_relaxed);

        return currentAmplitude * sample;
    }

    float WavetableOscillator::interpolateLineary(const std::vector<float>& table, float indexValue) const {
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

    A4Oscillator::A4Oscillator(float sampleRate)
        : _phaseIncrement{440.f / sampleRate * 2.f * PI} {}
    float A4Oscillator::getSample() {
        const auto sample = 0.5f * std::sin(_phase);
        _phase = std::fmod(_phase + _phaseIncrement, 2.f * PI);
        return sample;
    }

    void A4Oscillator::onPlaybackStopped() {
        _phase = 0.f;
    }
}