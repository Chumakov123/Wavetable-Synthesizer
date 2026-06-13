#include "DelayLine.h"
#include <algorithm>
#include <cmath>

namespace udaw {
    DelayLine::DelayLine(double sampleRate, float maxDelaySeconds)
        : _sampleRate(sampleRate) {
        _buffer.resize(static_cast<size_t>(sampleRate * maxDelaySeconds), 0.0f);
        _delayTimeSamples.store(static_cast<float>(sampleRate * 0.5f)); // 500ms default
    }

    float DelayLine::process(float input) {
        float wet = _wetLevel.load(std::memory_order_relaxed);
        if (wet <= 0.001f) return input;

        // Рассчитываем позицию чтения
        float delaySamples = _delayTimeSamples.load(std::memory_order_relaxed);
        float readIndex = static_cast<float>(_writeIndex) - delaySamples;

        if (readIndex < 0) {
            readIndex += static_cast<float>(_buffer.size());
        }

        // Линейная интерполяция для более мягкого звука при изменении времени задержки
        int i1 = static_cast<int>(readIndex);
        int i2 = (i1 + 1) % _buffer.size();
        float fraction = readIndex - static_cast<float>(i1);

        float delayedSample = _buffer[i1] + fraction * (_buffer[i2] - _buffer[i1]);

        // Записываем в буфер: вход + фидбек
        float feedback = _feedback.load(std::memory_order_relaxed);
        _buffer[_writeIndex] = input + (delayedSample * feedback);

        // Двигаем индекс записи
        _writeIndex = (_writeIndex + 1) % _buffer.size();

        // Смешиваем сухой и обработанный сигнал
        return input + (delayedSample * wet);
    }

    void DelayLine::setDelayTime(float seconds) {
        float samples = std::clamp(seconds, 0.01f, 2.0f) * static_cast<float>(_sampleRate);
        _delayTimeSamples.store(samples);
    }

    void DelayLine::setFeedback(float feedback) {
        _feedback.store(std::clamp(feedback, 0.0f, 0.95f));
    }

    void DelayLine::setWetLevel(float wetLevel) {
        _wetLevel.store(std::clamp(wetLevel, 0.0f, 1.0f));
    }
}
