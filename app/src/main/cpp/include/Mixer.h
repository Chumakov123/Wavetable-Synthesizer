#pragma once
#include <vector>
#include <memory>
#include <mutex>
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

        float getSample() override {
            if (_sequencer) {
                _sequencer->process(0, 1); // Продвигаем время на 1 семпл
            }

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
    };
}
