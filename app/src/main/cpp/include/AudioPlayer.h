#pragma once

#include <cstdint>

namespace wavetablesynthesizer {
    class AudioPlayer {
    public:
        virtual ~AudioPlayer() = default;
        virtual int32_t play() = 0;
        virtual void stop() = 0; //0 означает, что функция абстрактная, и этот класс нельзя создать

    };
}