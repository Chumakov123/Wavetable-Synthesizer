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
        int trackId;
        bool isDrum;
    };

    enum class QuantizationMode {
        None,
        Beat_1_16,
        Beat_1_32
    };

    struct Pattern {
        std::vector<MidiEvent> events;
    };

    class Sequencer {
    public:
        Sequencer(double sampleRate);

        void process(uint64_t currentFrame, uint32_t numFrames);

        // Запись событий
        void recordNoteOn(int trackId, float frequency);
        void recordNoteOff(int trackId, float frequency);
        void recordDrum(int drumId);

        // Управление
        void startRecording();
        void stopRecording();
        void startPlayback();
        void stopPlayback();
        void clear();
        void clearTrack(int trackId);

        void setLoopLengthBars(int bars);
        void setBpm(float bpm);
        void setQuantizationMode(QuantizationMode mode);

        bool isRecording() const { return _isRecording.load(); }
        bool isPlaying() const { return _isPlaying.load(); }

        // Arrangement Mode
        void setArrangementMode(bool enabled);
        void addPatternToPlaylist(int patternId);
        void clearPlaylist();
        void setActivePattern(int patternId);
        int getActivePattern() const { return _activePatternId; }
        void copyPattern(int sourceId, int targetId);
        void removePattern(int patternId);

        // Callback для синтезатора
        using NoteCallback = void(*)(void* receiver, int trackId, float frequency, bool isNoteOn);
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

        std::vector<Pattern> _patterns;
        std::vector<int> _playlist;
        std::atomic<int> _activePatternId{0};
        std::atomic<bool> _isArrangementMode{false};

        int _currentPlaylistIndex = 0;

        std::mutex _eventsMutex;

        uint64_t _currentLoopSample = 0;
        uint64_t _loopLengthSamples = 0;

        std::atomic<QuantizationMode> _quantizationMode{QuantizationMode::None};

        NoteCallback _noteCallback = nullptr;
        void* _receiver = nullptr;

        void updateLoopLength();
        uint64_t getQuantizedTimestamp(uint64_t timestamp);
        void playEventsAt(int patternId, uint64_t timestamp);
    };
}
