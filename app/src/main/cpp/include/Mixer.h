#pragma once
#include <vector>
#include <memory>
#include <mutex>
#include "AudioSource.h"

namespace wavetablesynthesizer {
    class Mixer : public AudioSource {
    public:
        void addSource(const std::shared_ptr<AudioSource>& source) {
            std::lock_guard<std::mutex> lock(_mutex);
            _sources.push_back(source);
        }

        float getSample() override {
            float sample = 0.f;
            {
                std::lock_guard<std::mutex> lock(_mutex);
                for (auto& source : _sources) {
                    sample += source->getSample();
                }
            }
            // Нормализуем громкость, чтобы избежать клиппинга при аккордах
            return sample * 0.3f;
        }

        void onPlaybackStopped() override {
            std::lock_guard<std::mutex> lock(_mutex);
            for (auto& source : _sources) {
                source->onPlaybackStopped();
            }
        }

    private:
        std::vector<std::shared_ptr<AudioSource>> _sources;
        std::mutex _mutex;
    };
}
