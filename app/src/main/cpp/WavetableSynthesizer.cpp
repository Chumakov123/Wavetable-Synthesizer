#include "Log.h"
#include <cmath>
#include <fstream>
#include "WavetableSynthesizer.h"
#include "OboeAudioPlayer.h"
#include "Mixer.h"
#include "AudioTrack.h"

namespace udaw {
    WavetableSynthesizer::WavetableSynthesizer() {
        _mixer = std::make_shared<Mixer>();

        for (int i = 0; i < NUM_TRACKS; ++i) {
            auto track = std::make_shared<SynthTrack>(sampleRate);
            _tracks.push_back(track);
            _mixer->addSource(track);
        }

        _metronome = std::make_shared<Metronome>(sampleRate);
        _mixer->addSource(_metronome);

        _drumTrack = std::make_shared<DrumTrack>(sampleRate);
        _mixer->addSource(_drumTrack);

        _sequencer = std::make_shared<Sequencer>(sampleRate);
        _sequencer->setNoteCallback(sequencerCallback, this);
        _sequencer->setMetronome(_metronome); // Передаем метроном в секвенсор
        _mixer->setSequencer(_sequencer);

        _audioPlayer = std::make_unique<OboeAudioPlayer>(_mixer, sampleRate);
        _audioRecorder = std::make_unique<AudioRecorder>();

        _vocalTrack = std::make_shared<AudioTrack>(sampleRate);
        _mixer->addSource(_vocalTrack);
    }

    WavetableSynthesizer::~WavetableSynthesizer() = default;

    void WavetableSynthesizer::play() {
        std::lock_guard<std::mutex> lock(_mutex);
        _isContinuousPlayActive = true;

        internalNoteOn(_activeTrackId, _lastSliderFrequency);

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
        LOGD("stop() called.");
        _audioPlayer->stop();
        _isStreamOpen = false;
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
        std::lock_guard<std::mutex> lock(_mutex);
        _lastSliderFrequency = frequencyInHz;
        if (_isContinuousPlayActive) {
            _tracks[_activeTrackId]->setFrequency(frequencyInHz);
        }
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

        if (!_isStreamOpen && !_isRendering) {
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
        if (enabled && !_isStreamOpen && !_isRendering) {
            std::lock_guard<std::mutex> lock(_mutex);
            if (!_isStreamOpen && !_isRendering) {
                _audioPlayer->play();
                _isStreamOpen = true;
            }
        }
    }

    void WavetableSynthesizer::setBpm(float bpm) {
        _sequencer->setBpm(bpm);
    }

    void WavetableSynthesizer::setRecording(bool enabled) {
        if (enabled) _sequencer->startRecording();
        else _sequencer->stopRecording();
    }

    void WavetableSynthesizer::setPlayback(bool enabled) {
        if (enabled) {
            _sequencer->startPlayback();
        } else {
            _sequencer->stopPlayback();
            std::lock_guard<std::mutex> lock(_mutex);
            for (auto& track : _tracks) {
                track->stopAllNotes();
            }
        }
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

    void WavetableSynthesizer::renderArrangement(const char* path) {
        {
            std::lock_guard<std::mutex> lock(_mutex);
            _isRendering = true;
            _mixer->setRendering(true);
        }
        _renderingProgress.store(0.0f);

        uint64_t totalSamples = _sequencer->getTotalArrangementSamples();
        if (totalSamples == 0) {
            _isRendering = false;
            _renderingProgress.store(1.0f);
            return;
        }

        std::ofstream file(path, std::ios::binary);
        if (!file.is_open()) {
            _isRendering = false;
            _renderingProgress.store(1.0f);
            return;
        }

        // WAV Header
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
        } header;

        header.fileSize = 36 + totalSamples * 2;
        header.byteRate = 48000 * 2;
        header.blockAlign = 2;
        header.dataSize = totalSamples * 2;

        file.write(reinterpret_cast<const char*>(&header), sizeof(header));

        // Prepare for rendering
        bool wasArrangementMode = _sequencer->isArrangementMode();
        _sequencer->resetForRendering();
        for (auto& track : _tracks) track->stopAllNotes();

        // Render loop
        for (uint64_t i = 0; i < totalSamples; ++i) {
            _sequencer->process(0, 1);
            float sample = _mixer->getSample();

            // Clip and convert to 16-bit PCM
            if (sample > 1.0f) sample = 1.0f;
            if (sample < -1.0f) sample = -1.0f;

            int16_t pcmSample = static_cast<int16_t>(sample * 32767.0f);
            file.write(reinterpret_cast<const char*>(&pcmSample), sizeof(pcmSample));

            if (i % 1000 == 0) {
                _renderingProgress.store(static_cast<float>(i) / totalSamples);
            }
        }

        file.close();

        // Restore state
        _sequencer->stopPlayback();
        _sequencer->setArrangementMode(wasArrangementMode);
        _mixer->setRendering(false);
        _isRendering = false;
        _renderingProgress.store(1.0f);
    }

    void WavetableSynthesizer::triggerKick() {
        _sequencer->recordDrum(0); // 0 = Kick
        if (!_isStreamOpen && !_isRendering) {
            _audioPlayer->play();
            _isStreamOpen = true;
        }
        _drumTrack->triggerKick();
    }

    void WavetableSynthesizer::triggerSnare() {
        _sequencer->recordDrum(1); // 1 = Snare
        if (!_isStreamOpen && !_isRendering) {
            _audioPlayer->play();
            _isStreamOpen = true;
        }
        _drumTrack->triggerSnare();
    }

    void WavetableSynthesizer::triggerHat() {
        _sequencer->recordDrum(2); // 2 = Hat
        if (!_isStreamOpen && !_isRendering) {
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

    bool WavetableSynthesizer::startMicRecording(const char* path) {
        return _audioRecorder->startRecording(path);
    }

    void WavetableSynthesizer::stopMicRecording() {
        _audioRecorder->stopRecording();
    }

    bool WavetableSynthesizer::isMicRecording() const {
        return _audioRecorder->isRecording();
    }

    void WavetableSynthesizer::loadAudioTrack(const char* path) {
        _vocalTrack->loadFile(path);
    }

    void WavetableSynthesizer::setAudioTrackEnabled(bool enabled) {
        _vocalTrack->setEnabled(enabled);
    }

    void WavetableSynthesizer::setAudioTrackOffset(float seconds) {
        _vocalTrack->setOffsetSamples(static_cast<int64_t>(seconds * sampleRate));
    }

    void WavetableSynthesizer::setAudioTrackVolume(float volumeInDb) {
        float volume = std::pow(10.0f, volumeInDb / 20.0f);
        _vocalTrack->setVolume(volume);
    }

    void WavetableSynthesizer::sequencerCallback(void* receiver, int trackId, float frequency, bool isNoteOn) {
        auto* synth = static_cast<WavetableSynthesizer*>(receiver);
        if (trackId == -1) {
            int drumId = static_cast<int>(frequency);
            if (drumId == 0) synth->_drumTrack->triggerKick();
            else if (drumId == 1) synth->_drumTrack->triggerSnare();
            else if (drumId == 2) synth->_drumTrack->triggerHat();
        } else if (trackId == -2) {
            for (auto& track : synth->_tracks) {
                track->stopAllNotes();
            }
        } else {
            if (isNoteOn) synth->internalNoteOn(trackId, frequency);
            else synth->internalNoteOff(trackId, frequency);
        }
    }
}
