#pragma once
#include <vector>
#include <memory>
#include <mutex>
#include <atomic>
#include <cstdint>
#include "AudioSource.h"
#include "Sequencer.h"

namespace wavetablesynthesizer {
    class Mixer : public AudioSource {
    public:
        void addSource(const std::shared_ptr<AudioSource>& source) {
            std::lock_guard<std::mutex> lock(_mutex);
            _sources.push_back(source);
        }

        void setSequencer(const std::shared_ptr<Sequencer>& sequencer) {
            _sequencer = sequencer;
        }

        void setRendering(bool rendering) {
            _isRendering = rendering;
        }

        bool isRendering() override {
            return _isRendering;
        }

        void process(int32_t framesCount) override {
            if (_sequencer) {
                _sequencer->process(0, framesCount);
            }
        }

        float getSample() override {
            float sample = 0.f;
            {
                std::lock_guard<std::mutex> lock(_mutex);
                for (auto& source : _sources) {
                    sample += source->getSample();
                }
            }
            // Нормализуем громкость
            return sample * 0.25f;
        }

        void onPlaybackStopped() override {
            std::lock_guard<std::mutex> lock(_mutex);
            for (auto& source : _sources) {
                source->onPlaybackStopped();
            }
        }

    private:
        std::vector<std::shared_ptr<AudioSource>> _sources;
        std::shared_ptr<Sequencer> _sequencer;
        std::mutex _mutex;
        std::atomic<bool> _isRendering{false};
    };
}
