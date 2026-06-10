#pragma once
#include <vector>
#include <atomic>
#include <mutex>

namespace wavetablesynthesizer {
    class DelayLine {
    public:
        DelayLine(double sampleRate, float maxDelaySeconds = 2.0f);

        float process(float input);

        void setDelayTime(float seconds);
        void setFeedback(float feedback);
        void setWetLevel(float wetLevel);

    private:
        double _sampleRate;
        std::vector<float> _buffer;
        int _writeIndex = 0;

        std::atomic<float> _delayTimeSamples;
        std::atomic<float> _feedback{0.5f};
        std::atomic<float> _wetLevel{0.0f}; // 0.0 is Bypass

        std::mutex _mutex;
    };
}
