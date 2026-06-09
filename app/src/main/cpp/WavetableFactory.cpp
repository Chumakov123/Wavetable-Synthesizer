#include "WavetableFactory.h"
#include "Wavetable.h"
#include <cmath>
#include "MathConstants.h"

namespace {
    constexpr int WAVETABLE_LENGTH = 256;

    std::vector<float> generateSineWaveTable() {
        auto sineWaveTable = std::vector<float>(WAVETABLE_LENGTH);

        for (auto i = 0; i < WAVETABLE_LENGTH; ++i) {
            sineWaveTable[i] = std::sin(2.f * wavetablesynthesizer::PI * static_cast<float>(i) / static_cast<float>(WAVETABLE_LENGTH));
        }
        return sineWaveTable;
    }

    std::vector<float> generateTriangleWaveTable() {
        auto triangleWaveTable = std::vector<float>(WAVETABLE_LENGTH, 0.0f);

        constexpr auto HARMONICS_COUNT = 13;

        const float coefficient =
                8.0f / (wavetablesynthesizer::PI * wavetablesynthesizer::PI);

        for (int k = 1; k <= HARMONICS_COUNT; ++k) {
            const float sign = (k % 2 == 0) ? 1.0f : -1.0f;
            const auto harmonic = static_cast<float>(2 * k - 1);
            const float harmonicFactor = 1.0f / (harmonic * harmonic);

            for (int j = 0; j < WAVETABLE_LENGTH; ++j) {
                const float phase =
                        2.0f * wavetablesynthesizer::PI
                        * static_cast<float>(j)
                        / static_cast<float>(WAVETABLE_LENGTH);

                triangleWaveTable[j] += coefficient
                                        * sign
                                        * harmonicFactor
                                        * std::sin(harmonic * phase);
            }
        }

        return triangleWaveTable;
    }

    std::vector<float> generateSquareWaveTable() {
        auto squareWaveTable = std::vector<float>(WAVETABLE_LENGTH, 0.0f);

        constexpr auto HARMONICS_COUNT = 7;

        const float coefficient =
                4.0f / wavetablesynthesizer::PI;

        for (int k = 1; k <= HARMONICS_COUNT; ++k) {
            const auto harmonic = static_cast<float>(2 * k - 1);
            const float harmonicFactor = 1.0f / harmonic;

            for (int j = 0; j < WAVETABLE_LENGTH; ++j) {
                const float phase =
                        2.0f * wavetablesynthesizer::PI
                        * static_cast<float>(j)
                        / static_cast<float>(WAVETABLE_LENGTH);

                squareWaveTable[j] += coefficient
                                      * harmonicFactor
                                      * std::sin(harmonic * phase);
            }
        }

        return squareWaveTable;
    }

    std::vector<float> generateSawWaveTable() {
        auto sawWaveTable = std::vector<float>(WAVETABLE_LENGTH, 0.0f);

        constexpr auto HARMONICS_COUNT = 26;

        const float coefficient =
                2.0f / wavetablesynthesizer::PI;

        for (int k = 1; k <= HARMONICS_COUNT; ++k) {
            const float sign = (k % 2 == 0) ? 1.0f : -1.0f;
            const auto harmonic = static_cast<float>(k);
            const float harmonicFactor = 1.0f / harmonic;

            for (int j = 0; j < WAVETABLE_LENGTH; ++j) {
                const float phase =
                        2.0f * wavetablesynthesizer::PI
                        * static_cast<float>(j)
                        / static_cast<float>(WAVETABLE_LENGTH);

                sawWaveTable[j] += coefficient
                                   * sign
                                   * harmonicFactor
                                   * std::sin(harmonic * phase);
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
                return {WAVETABLE_LENGTH, 0.f};

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