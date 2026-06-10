#pragma once

#include <vector>
#include <atomic>
#include <mutex>
#include <stdint.h>

namespace wavetablesynthesizer {

    struct MidiEvent {
        uint64_t timestamp; // Время в семплах от начала лупа
        float frequency;
        bool isNoteOn;
    };

    class Sequencer {
    public:
        Sequencer(double sampleRate);

        void process(uint64_t currentFrame, uint32_t numFrames);

        // Запись событий
        void recordNoteOn(float frequency);
        void recordNoteOff(float frequency);

        // Управление
        void startRecording();
        void stopRecording();
        void startPlayback();
        void stopPlayback();
        void clear();

        void setLoopLengthBars(int bars);
        void setBpm(float bpm);

        bool isRecording() const { return _isRecording.load(); }
        bool isPlaying() const { return _isPlaying.load(); }

        // Callback для синтезатора
        using NoteCallback = void(*)(void* receiver, float frequency, bool isNoteOn);
        void setNoteCallback(NoteCallback callback, void* receiver) {
            _noteCallback = callback;
            _receiver = receiver;
        }

    private:
        double _sampleRate;
        std::atomic<float> _bpm{120.0f};
        std::atomic<int> _loopLengthBars{4};

        std::atomic<bool> _isRecording{false};
        std::atomic<bool> _isPlaying{false};

        std::vector<MidiEvent> _events;
        std::mutex _eventsMutex;

        uint64_t _currentLoopSample = 0;
        uint64_t _loopLengthSamples = 0;

        NoteCallback _noteCallback = nullptr;
        void* _receiver = nullptr;

        void updateLoopLength();
    };
}
