#pragma once

#include <memory>
#include <string>
#include <fstream>
#include <mutex>
#include <atomic>
#include <oboe/Oboe.h>

namespace wavetablesynthesizer {

    class AudioRecorder : public oboe::AudioStreamDataCallback {
    public:
        AudioRecorder();
        ~AudioRecorder();

        bool startRecording(const std::string& path);
        void stopRecording();
        bool isRecording() const { return _isRecording; }

        oboe::DataCallbackResult onAudioReady(oboe::AudioStream* audioStream,
                                              void* audioData,
                                              int32_t framesCount) override;

    private:
        void writeWavHeader();
        void updateWavHeader();

        std::shared_ptr<oboe::AudioStream> _stream;
        std::ofstream _file;
        std::string _filePath;
        std::atomic<bool> _isRecording{false};
        std::atomic<uint32_t> _totalSamples{0};
        std::mutex _mutex;

        static constexpr int kSampleRate = 48000;
        static constexpr int kChannelCount = 1;
    };
}
