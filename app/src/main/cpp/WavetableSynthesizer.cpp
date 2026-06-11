#include "Log.h"
#include <cmath>
#include "WavetableSynthesizer.h"
#include "OboeAudioPlayer.h"
#include "Mixer.h"

namespace wavetablesynthesizer {
    WavetableSynthesizer::WavetableSynthesizer() {
        auto mixer = std::make_shared<Mixer>();

        for (int i = 0; i < NUM_TRACKS; ++i) {
            auto track = std::make_shared<SynthTrack>(sampleRate);
            _tracks.push_back(track);
            mixer->addSource(track);
        }

        _metronome = std::make_shared<Metronome>(sampleRate);
        mixer->addSource(_metronome);

        _drumTrack = std::make_shared<DrumTrack>(sampleRate);
        mixer->addSource(_drumTrack);

        _sequencer = std::make_shared<Sequencer>(sampleRate);
        _sequencer->setNoteCallback(sequencerCallback, this);
        mixer->setSequencer(_sequencer);

        _audioPlayer = std::make_unique<OboeAudioPlayer>(mixer, sampleRate);
    }

    WavetableSynthesizer::~WavetableSynthesizer() = default;

    void WavetableSynthesizer::play() {
        std::lock_guard<std::mutex> lock(_mutex);
        _isContinuousPlayActive = true;
        if (_isStreamOpen) return;

        LOGD("play() called.");
        _audioPlayer->stop();

        const auto result = _audioPlayer->play();
        if (result == 0) {
            _isStreamOpen = true;
        } else {
            LOGD("Could not start playback.");
        }
    }

    void WavetableSynthesizer::stop() {
        std::lock_guard<std::mutex> lock(_mutex);
        if (!_isContinuousPlayActive) return;
        LOGD("stop() called.");
        for (auto& track : _tracks) {
            track->stopAllNotes();
        }
        _isContinuousPlayActive = false;
    }

    bool WavetableSynthesizer::isPlaying() const {
        return _isContinuousPlayActive;
    }

    void WavetableSynthesizer::setActiveTrack(int trackId) {
        if (trackId >= 0 && trackId < NUM_TRACKS) {
            std::lock_guard<std::mutex> lock(_mutex);
            _activeTrackId = trackId;
        }
    }

    void WavetableSynthesizer::setFrequency(float frequencyInHz) {
        // First voice of active track for slider mode
    }

    void WavetableSynthesizer::setVolume(float volumeInDb) {
        _tracks[_activeTrackId]->setVolume(volumeInDb);
    }

    void WavetableSynthesizer::setWavetable(Wavetable wavetable) {
        _tracks[_activeTrackId]->setWavetable(wavetable);
    }

    void WavetableSynthesizer::noteOn(float frequencyInHz) {
        _sequencer->recordNoteOn(_activeTrackId, frequencyInHz);
        internalNoteOn(_activeTrackId, frequencyInHz);
    }

    void WavetableSynthesizer::internalNoteOn(int trackId, float frequencyInHz) {
        if (trackId < 0 || trackId >= NUM_TRACKS) return;

        if (!_isStreamOpen) {
            _audioPlayer->play();
            _isStreamOpen = true;
        }
        _tracks[trackId]->noteOn(frequencyInHz);
    }

    void WavetableSynthesizer::noteOff(float frequencyInHz) {
        _sequencer->recordNoteOff(_activeTrackId, frequencyInHz);
        internalNoteOff(_activeTrackId, frequencyInHz);
    }

    void WavetableSynthesizer::internalNoteOff(int trackId, float frequencyInHz) {
        if (trackId < 0 || trackId >= NUM_TRACKS) return;
        _tracks[trackId]->noteOff(frequencyInHz);
    }

    void WavetableSynthesizer::setAttackTime(float time) {
        _tracks[_activeTrackId]->setAttackTime(time);
    }

    void WavetableSynthesizer::setDecayTime(float time) {
        _tracks[_activeTrackId]->setDecayTime(time);
    }

    void WavetableSynthesizer::setSustainLevel(float level) {
        _tracks[_activeTrackId]->setSustainLevel(level);
    }

    void WavetableSynthesizer::setReleaseTime(float time) {
        _tracks[_activeTrackId]->setReleaseTime(time);
    }

    void WavetableSynthesizer::setLfoRate(float rate) {
        _tracks[_activeTrackId]->setLfoRate(rate);
    }

    void WavetableSynthesizer::setLfoDepth(float depth) {
        _tracks[_activeTrackId]->setLfoDepth(depth);
    }

    void WavetableSynthesizer::setTremoloDepth(float depth) {
        _tracks[_activeTrackId]->setTremoloDepth(depth);
    }

    void WavetableSynthesizer::setDelayTime(float seconds) {
        _tracks[_activeTrackId]->setDelayTime(seconds);
    }

    void WavetableSynthesizer::setDelayFeedback(float feedback) {
        _tracks[_activeTrackId]->setDelayFeedback(feedback);
    }

    void WavetableSynthesizer::setDelayWet(float wet) {
        _tracks[_activeTrackId]->setDelayWet(wet);
    }

    void WavetableSynthesizer::setMetronomeEnabled(bool enabled) {
        _metronome->setEnabled(enabled);
        if (enabled && !_isStreamOpen) {
            std::lock_guard<std::mutex> lock(_mutex);
            if (!_isStreamOpen) {
                _audioPlayer->play();
                _isStreamOpen = true;
            }
        }
    }

    void WavetableSynthesizer::setBpm(float bpm) {
        _metronome->setBpm(bpm);
        _sequencer->setBpm(bpm);
    }

    void WavetableSynthesizer::setRecording(bool enabled) {
        if (enabled) _sequencer->startRecording();
        else _sequencer->stopRecording();
    }

    void WavetableSynthesizer::setPlayback(bool enabled) {
        if (enabled) _sequencer->startPlayback();
        else _sequencer->stopPlayback();
    }

    void WavetableSynthesizer::clearSequence() {
        _sequencer->clear();
        std::lock_guard<std::mutex> lock(_mutex);
        for (auto& track : _tracks) {
            track->stopAllNotes();
        }
    }

    void WavetableSynthesizer::clearActiveTrack() {
        _sequencer->clearTrack(_activeTrackId);
        std::lock_guard<std::mutex> lock(_mutex);
        _tracks[_activeTrackId]->stopAllNotes();
    }

    void WavetableSynthesizer::setQuantizationMode(int mode) {
        _sequencer->setQuantizationMode(static_cast<QuantizationMode>(mode));
    }

    void WavetableSynthesizer::triggerKick() {
        _sequencer->recordDrum(0); // 0 = Kick
        if (!_isStreamOpen) {
            _audioPlayer->play();
            _isStreamOpen = true;
        }
        _drumTrack->triggerKick();
    }

    void WavetableSynthesizer::triggerSnare() {
        _sequencer->recordDrum(1); // 1 = Snare
        if (!_isStreamOpen) {
            _audioPlayer->play();
            _isStreamOpen = true;
        }
        _drumTrack->triggerSnare();
    }

    void WavetableSynthesizer::triggerHat() {
        _sequencer->recordDrum(2); // 2 = Hat
        if (!_isStreamOpen) {
            _audioPlayer->play();
            _isStreamOpen = true;
        }
        _drumTrack->triggerHat();
    }

    void WavetableSynthesizer::setDrumVolume(float volumeInDb) {
        _drumTrack->setVolume(volumeInDb);
    }

    void WavetableSynthesizer::clearDrums() {
        _sequencer->clearTrack(-1);
    }

    void WavetableSynthesizer::sequencerCallback(void* receiver, int trackId, float frequency, bool isNoteOn) {
        auto* synth = static_cast<WavetableSynthesizer*>(receiver);
        if (trackId == -1) {
            int drumId = static_cast<int>(frequency);
            if (drumId == 0) synth->_drumTrack->triggerKick();
            else if (drumId == 1) synth->_drumTrack->triggerSnare();
            else if (drumId == 2) synth->_drumTrack->triggerHat();
        } else {
            if (isNoteOn) synth->internalNoteOn(trackId, frequency);
            else synth->internalNoteOff(trackId, frequency);
        }
    }
}
