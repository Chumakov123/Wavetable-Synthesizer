#pragma once

#include <vector>
#include <string>
#include <mutex>
#include <atomic>
#include <fstream>
#include "AudioSource.h"

namespace udaw {

    class AudioTrack : public AudioSource {
    public:
        AudioTrack(int samplingRate);
        ~AudioTrack();

        bool loadFile(const std::string& path);
        void setEnabled(bool enabled) { _enabled = enabled; }
        bool isEnabled() const { return _enabled; }

        void setOffsetSamples(int64_t samples) { _offsetSamples = samples; }
        int64_t getOffsetSamples() const { return _offsetSamples; }

        void setVolume(float volume) { _volume = volume; }

        void resetPlayback();
        void setPosition(int64_t samplePosition) override { _currentPosition = samplePosition; }

        float getSample() override;
        void onPlaybackStopped() override;
        void process(int32_t framesCount) override;

    private:
        int _samplingRate;
        std::vector<float> _buffer;
        std::atomic<bool> _enabled{false};
        std::atomic<int64_t> _offsetSamples{0};
        std::atomic<float> _volume{1.0f};

        std::atomic<int64_t> _currentPosition{0}; // Position in the project
        std::mutex _mutex;
    };
}
