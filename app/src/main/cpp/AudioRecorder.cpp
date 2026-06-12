#include "AudioRecorder.h"
#include "Log.h"

namespace wavetablesynthesizer {

    AudioRecorder::AudioRecorder() {}

    AudioRecorder::~AudioRecorder() {
        stopRecording();
    }

    bool AudioRecorder::startRecording(const std::string& path) {
        std::lock_guard<std::mutex> lock(_mutex);
        if (_isRecording) return false;

        _filePath = path;
        _file.open(_filePath, std::ios::binary);
        if (!_file.is_open()) {
            LOGD("Failed to open file for recording: %s", path.c_str());
            return false;
        }

        _totalSamples = 0;
        writeWavHeader();

        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Input)
               ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
               ->setSharingMode(oboe::SharingMode::Exclusive)
               ->setFormat(oboe::AudioFormat::Float)
               ->setChannelCount(kChannelCount)
               ->setSampleRate(kSampleRate)
               ->setDataCallback(this);

        oboe::Result result = builder.openStream(_stream);
        if (result != oboe::Result::OK) {
            LOGD("Failed to open input stream: %s", oboe::convertToText(result));
            _file.close();
            return false;
        }

        result = _stream->requestStart();
        if (result != oboe::Result::OK) {
            LOGD("Failed to start input stream: %s", oboe::convertToText(result));
            _stream->close();
            _file.close();
            return false;
        }

        _isRecording = true;
        return true;
    }

    void AudioRecorder::stopRecording() {
        std::lock_guard<std::mutex> lock(_mutex);
        if (!_isRecording) return;

        _isRecording = false;

        if (_stream) {
            _stream->requestStop();
            _stream->close();
            _stream.reset();
        }

        if (_file.is_open()) {
            updateWavHeader();
            _file.close();
        }
    }

    oboe::DataCallbackResult AudioRecorder::onAudioReady(oboe::AudioStream* audioStream,
                                                          void* audioData,
                                                          int32_t framesCount) {
        if (!_isRecording) return oboe::DataCallbackResult::Stop;

        const float* floatData = static_cast<const float*>(audioData);
        for (int i = 0; i < framesCount; ++i) {
            float sample = floatData[i];
            // Clip
            if (sample > 1.0f) sample = 1.0f;
            if (sample < -1.0f) sample = -1.0f;

            int16_t pcmSample = static_cast<int16_t>(sample * 32767.0f);
            _file.write(reinterpret_cast<const char*>(&pcmSample), sizeof(pcmSample));
        }

        _totalSamples += framesCount;
        return oboe::DataCallbackResult::Continue;
    }

    struct WavHeader {
        char riff[4] = {'R', 'I', 'F', 'F'};
        uint32_t fileSize;
        char wave[4] = {'W', 'A', 'V', 'E'};
        char fmt[4] = {'f', 'm', 't', ' '};
        uint32_t fmtSize = 16;
        uint16_t audioFormat = 1; // PCM
        uint16_t numChannels = 1;
        uint32_t sampleRate = 48000;
        uint32_t byteRate;
        uint16_t blockAlign;
        uint16_t bitsPerSample = 16;
        char data[4] = {'d', 'a', 't', 'a'};
        uint32_t dataSize;
    };

    void AudioRecorder::writeWavHeader() {
        WavHeader header;
        header.fileSize = 0; // Will be updated
        header.byteRate = kSampleRate * sizeof(int16_t);
        header.blockAlign = sizeof(int16_t);
        header.dataSize = 0; // Will be updated
        _file.write(reinterpret_cast<const char*>(&header), sizeof(header));
    }

    void AudioRecorder::updateWavHeader() {
        uint32_t dataSize = _totalSamples * sizeof(int16_t);
        uint32_t fileSize = 36 + dataSize;

        _file.seekp(4);
        _file.write(reinterpret_cast<const char*>(&fileSize), 4);
        _file.seekp(40);
        _file.write(reinterpret_cast<const char*>(&dataSize), 4);
    }
}
