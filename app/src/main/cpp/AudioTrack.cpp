#include "include/AudioTrack.h"
#include "include/Log.h"
#include <fstream>
#include <algorithm>

namespace wavetablesynthesizer {

    AudioTrack::AudioTrack(int samplingRate) : _samplingRate(samplingRate) {}

    AudioTrack::~AudioTrack() {}

    struct WavHeader {
        char riff[4];
        uint32_t fileSize;
        char wave[4];
        char fmt[4];
        uint32_t fmtSize;
        uint16_t audioFormat;
        uint16_t numChannels;
        uint32_t sampleRate;
        uint32_t byteRate;
        uint16_t blockAlign;
        uint16_t bitsPerSample;
        char data[4];
        uint32_t dataSize;
    };

    bool AudioTrack::loadFile(const std::string& path) {
        std::lock_guard<std::mutex> lock(_mutex);
        std::ifstream file(path, std::ios::binary);
        if (!file.is_open()) return false;

        WavHeader header;
        file.read(reinterpret_cast<char*>(&header), sizeof(header));

        if (std::string(header.riff, 4) != "RIFF" || std::string(header.wave, 4) != "WAVE") {
            return false;
        }

        uint32_t numSamples = header.dataSize / (header.bitsPerSample / 8);
        _buffer.resize(numSamples);

        if (header.bitsPerSample == 16) {
            std::vector<int16_t> pcm(numSamples);
            file.read(reinterpret_cast<char*>(pcm.data()), header.dataSize);
            for (size_t i = 0; i < numSamples; ++i) {
                _buffer[i] = pcm[i] / 32767.0f;
            }
        } else if (header.bitsPerSample == 32) {
            file.read(reinterpret_cast<char*>(_buffer.data()), header.dataSize);
        }

        _currentPosition = 0;
        return true;
    }

    void AudioTrack::resetPlayback() {
        _currentPosition = 0;
    }

    float AudioTrack::getSample() {
        if (!_enabled || _buffer.empty()) return 0.0f;

        int64_t posInFile = _currentPosition - _offsetSamples;
        if (posInFile < 0 || posInFile >= static_cast<int64_t>(_buffer.size())) {
            return 0.0f;
        }

        return _buffer[posInFile] * _volume;
    }

    void AudioTrack::onPlaybackStopped() {
        _currentPosition = 0;
    }

    void AudioTrack::process(int32_t framesCount) {
        // Position is updated in getSample() for simplicity in this mono-stream mixer
        // but if we had a multi-channel buffer process, we'd update it here.
    }
}
