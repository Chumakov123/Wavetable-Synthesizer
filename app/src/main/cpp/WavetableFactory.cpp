#include "WavetableFactory.h"
#include "Wavetable.h"
#include <cmath>
#include "MathConstants.h"

namespace {
    constexpr auto WAVETABLE_LENGHT = 256;

    std::vector<float> generateSineWaveTable() {
        auto sineWaveTable = std::vector<float>(WAVETABLE_LENGHT);

        for (auto i = 0; i < WAVETABLE_LENGHT; ++i) {
            sineWaveTable[i] = std::sin(2.f * wavetablesynthesizer::PI * static_cast<float>(i) / static_cast<float>(WAVETABLE_LENGHT));
        }
        return sineWaveTable;
    }

    std::vector<float> generateTriangleWaveTable() {
        auto triangleWaveTable = std::vector<float>(WAVETABLE_LENGHT);

        constexpr auto HARMONICS_COUNT = 13;

        for (auto k = 1; k <= HARMONICS_COUNT; ++k) {
            for (auto j = 0; j < WAVETABLE_LENGHT; ++j) {
                const auto phase = 2.f * wavetablesynthesizer::PI * static_cast<float>(j) / static_cast<float>(WAVETABLE_LENGHT);
                triangleWaveTable[j] += 8.f / std::pow(wavetablesynthesizer::PI, 2.f)
                        * std::pow(-1.f, k) * std::pow(static_cast<float>(2 * k - 1), -2.f)
                        * std::sin((2.f * static_cast<float>(k) - 1.f) * phase);
            }
        }

        return triangleWaveTable;
    }

    std::vector<float> generateSquareWaveTable() {
        auto squareWaveTable = std::vector<float>(WAVETABLE_LENGHT, 0.f);

        constexpr auto HARMONICS_COUNT = 7;
        for (auto k = 1; k <= HARMONICS_COUNT; ++k) {
            for (auto j = 0; j < WAVETABLE_LENGHT; ++j) {
                const auto phase = 2.f * wavetablesynthesizer::PI * static_cast<float>(j) / static_cast<float>(WAVETABLE_LENGHT);
                squareWaveTable[j] += 4.f / wavetablesynthesizer::PI * std::pow(2.f * static_cast<float>(k) - 1.f, -1.f)
                                      * std::sin((2.f * static_cast<float>(k) - 1.f) * phase);
            }
        }

        return squareWaveTable;
    }

    std::vector<float> generateSawWaveTable() {
        auto sawWaveTable = std::vector<float>(WAVETABLE_LENGHT, 0.f);

        constexpr auto HARMONICS_COUNT = 26;

        for (auto k = 1; k <= HARMONICS_COUNT; ++k) {
            for (auto j = 0; j < WAVETABLE_LENGHT; ++j) {
                const auto phase = 2.f * wavetablesynthesizer::PI * static_cast<float>(j) / static_cast<float>(WAVETABLE_LENGHT);
                sawWaveTable[j] += 2.f / wavetablesynthesizer::PI * std::pow(-1.f, k) * std::pow(static_cast<float>(k), -1.f)
                                   * std::sin(static_cast<float>(k) * phase);
            }
        }

        return sawWaveTable;
    }

    template <typename F>
    std::vector<float> generateWaveTableOnce(std::vector<float>& waveTable, F&& generator) {
        if (waveTable.empty()) {
            waveTable = generator();
            float maxAbs = 0.0f;
            for (float sample : waveTable) {
                maxAbs = std::max(maxAbs, std::abs(sample));
            }
            if (maxAbs > 0.0f) {
                for (float& sample : waveTable) {
                    sample /= maxAbs;
                }
            }
        }
        return waveTable;
    }
}

namespace wavetablesynthesizer {

    std::vector<float> WavetableFactory::getWaveTable(Wavetable wavetable) {
        switch (wavetable) {
            case Wavetable::SINE:
                return sineWaveTable();
            case Wavetable::TRIANGLE:
                return triangleWaveTable();
            case Wavetable::SQUARE:
                return squareWaveTable();
            case Wavetable::SAW:
                return sawWaveTable();
            default:
                return {WAVETABLE_LENGHT, 0.f};

        }
    }
    std::vector<float> WavetableFactory::sineWaveTable() {
        return generateWaveTableOnce(_sineWaveTable, &generateSineWaveTable);
    }
    std::vector<float> WavetableFactory::triangleWaveTable() {
        return generateWaveTableOnce(_triangleWaveTable, &generateTriangleWaveTable);
    }

    std::vector<float> WavetableFactory::squareWaveTable() {
        return generateWaveTableOnce(_squareWaveTable, &generateSquareWaveTable);
    }
    std::vector<float> WavetableFactory::sawWaveTable() {
        return generateWaveTableOnce(_sawWaveTable, &generateSawWaveTable);
    }
}