#pragma once
#include <memory>
#include <mutex>
#include "Wavetable.h"
#include "WavetableFactory.h"
#include "Metronome.h"
#include "Sequencer.h"
#include "SynthTrack.h"
#include "DrumSynth.h"

namespace wavetablesynthesizer {
    class AudioPlayer;

    constexpr  auto sampleRate = 48000;
    constexpr  auto NUM_TRACKS = 4;

    class WavetableSynthesizer {
    public:
        WavetableSynthesizer();
        ~WavetableSynthesizer();
        void play();
        void stop();
        bool isPlaying() const;

        // Переключатель активного трека для редактирования параметров из UI
        void setActiveTrack(int trackId);

        void setFrequency(float frequencyInHz);
        void setVolume(float volumeInDb);
        void setWavetable(Wavetable wavetable);
        void noteOn(float frequencyInHz);
        void noteOff(float frequencyInHz);

        void setAttackTime(float time);
        void setDecayTime(float time);
        void setSustainLevel(float level);
        void setReleaseTime(float time);

        void setLfoRate(float rate);
        void setLfoDepth(float depth);
        void setTremoloDepth(float depth);

        // FX Delay
        void setDelayTime(float seconds);
        void setDelayFeedback(float feedback);
        void setDelayWet(float wet);

        void setMetronomeEnabled(bool enabled);
        void setBpm(float bpm);

        // Методы для Секвенсора
        void setRecording(bool enabled);
        void setPlayback(bool enabled);
        void clearSequence();
        void clearActiveTrack();
        void setQuantizationMode(int mode);

        // Arrangement Mode
        void setArrangementMode(bool enabled) { _sequencer->setArrangementMode(enabled); }
        void addPatternToPlaylist(int patternId) { _sequencer->addPatternToPlaylist(patternId); }
        void clearPlaylist() { _sequencer->clearPlaylist(); }
        void setActivePattern(int patternId) { _sequencer->setActivePattern(patternId); }
        void copyPattern(int sourceId, int targetId) { _sequencer->copyPattern(sourceId, targetId); }
        void removePattern(int patternId) { _sequencer->removePattern(patternId); }

        // Grid Editing
        int getEventCount(int patternId) { return _sequencer->getEventCount(patternId); }
        MidiEvent getEvent(int patternId, int index) { return _sequencer->getEvent(patternId, index); }
        void updateEventTimestamp(int patternId, int index, uint64_t timestamp) {
            _sequencer->updateEventTimestamp(patternId, index, timestamp);
        }
        void updateEventFrequency(int patternId, int index, float frequency) {
            _sequencer->updateEventFrequency(patternId, index, frequency);
        }
        void deleteEvent(int patternId, int index) { _sequencer->deleteEvent(patternId, index); }
        int addEvent(int patternId, uint64_t timestamp, float frequency, bool isNoteOn, int trackId, bool isDrum) {
            return _sequencer->addEvent(patternId, timestamp, frequency, isNoteOn, trackId, isDrum);
        }
        void quantizePattern(int patternId, int mode) {
            _sequencer->quantizePattern(patternId, static_cast<QuantizationMode>(mode));
        }

        int getCurrentPlaylistIndex() const { return _sequencer->getCurrentPlaylistIndex(); }

        // Drums
        void triggerKick();
        void triggerSnare();
        void triggerHat();
        void setDrumVolume(float volumeInDb);
        void clearDrums();

    private:
        std::atomic<bool> _isStreamOpen = false;
        std::atomic<bool> _isContinuousPlayActive = false;
        std::mutex _mutex;

        int _activeTrackId = 0;
        std::vector<std::shared_ptr<SynthTrack>> _tracks;

        std::shared_ptr<Metronome> _metronome;
        std::shared_ptr<Sequencer> _sequencer;
        std::shared_ptr<DrumTrack> _drumTrack;
        std::unique_ptr<AudioPlayer> _audioPlayer;

        // Внутренний метод для нот (чтобы избежать рекурсии при записи)
        void internalNoteOn(int trackId, float frequencyInHz);
        void internalNoteOff(int trackId, float frequencyInHz);
        static void sequencerCallback(void* receiver, int trackId, float frequency, bool isNoteOn);
    };
}