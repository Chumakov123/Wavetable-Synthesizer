#include <jni.h>
#include <memory>
#include "Log.h"
#include "WavetableSynthesizer.h"

extern "C" {
JNIEXPORT jlong JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_create(JNIEnv *env,
                                                                            jobject thiz) {
    auto synthesizer = std::make_unique<wavetablesynthesizer::WavetableSynthesizer>();

    if (not synthesizer) {
        LOGD("Failed to create the synthesizer.");
        synthesizer.reset(nullptr);
    }
    return reinterpret_cast<jlong>(synthesizer.release());
}
JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_delete(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);

    if (not synthesizer) {
        LOGD("Attempt to destroy an uninitialized synthesizer.");
        return;
    }

    delete synthesizer;
}
JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_play(JNIEnv *env, jobject thiz,
                                                                          jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);

    if (synthesizer) {
        synthesizer->play();
    } else {
        LOGD("Synthesizer not created. Please, create the synthesizer first by calling create().");
    }
}
JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_stop(JNIEnv *env, jobject thiz,
                                                                          jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);

    if (synthesizer) {
        synthesizer->stop();
    } else {
        LOGD("Synthesizer not created. Please, create the synthesizer first by calling create().");
    }
}
JNIEXPORT jboolean JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_isPlaying(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);

    if (synthesizer) {
        return synthesizer->isPlaying();
    } else {
        LOGD("Synthesizer not created. Please, create the synthesizer first by calling create().");
    }
    return false;
}
JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setFrequency(JNIEnv *env,
                                                                                  jobject thiz,
                                                                                  jlong synthesizerHandle,
                                                                                  jfloat frequencyInHz) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);

    if (synthesizer) {
        synthesizer->setFrequency(static_cast<float>(frequencyInHz));
    } else {
        LOGD("Synthesizer not created. Please, create the synthesizer first by calling create().");
    }
}
JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setVolume(JNIEnv *env,
                                                                               jobject thiz,
                                                                               jlong synthesizerHandle,
                                                                               jfloat volumeInDb) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);

    if (synthesizer) {
        synthesizer->setVolume(static_cast<float>(volumeInDb));
    } else {
        LOGD("Synthesizer not created. Please, create the synthesizer first by calling create().");
    }
}
JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setWavetable(JNIEnv *env,
                                                                                  jobject thiz,
                                                                                  jlong synthesizerHandle,
                                                                                  jint wavetable) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    const auto nativeWavetable = static_cast<wavetablesynthesizer::Wavetable>(wavetable);

    if (synthesizer) {
        synthesizer->setWavetable(nativeWavetable);
    } else {
        LOGD("Synthesizer not created. Please, create the synthesizer first by calling create().");
    }
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_noteOn(JNIEnv *env, jobject thiz,
                                                                          jlong synthesizerHandle,
                                                                          jfloat frequencyInHz) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->noteOn(static_cast<float>(frequencyInHz));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_noteOff(JNIEnv *env, jobject thiz,
                                                                           jlong synthesizerHandle,
                                                                           jfloat frequencyInHz) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->noteOff(static_cast<float>(frequencyInHz));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setAttackTime(JNIEnv *env, jobject thiz,
                                                                                 jlong synthesizerHandle,
                                                                                 jfloat time) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setAttackTime(static_cast<float>(time));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setDecayTime(JNIEnv *env, jobject thiz,
                                                                                jlong synthesizerHandle,
                                                                                jfloat time) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setDecayTime(static_cast<float>(time));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setSustainLevel(JNIEnv *env, jobject thiz,
                                                                                   jlong synthesizerHandle,
                                                                                   jfloat level) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setSustainLevel(static_cast<float>(level));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setReleaseTime(JNIEnv *env, jobject thiz,
                                                                                  jlong synthesizerHandle,
                                                                                  jfloat time) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setReleaseTime(static_cast<float>(time));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setLfoRate(JNIEnv *env, jobject thiz,
                                                                              jlong synthesizerHandle,
                                                                              jfloat rate) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setLfoRate(static_cast<float>(rate));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setLfoDepth(JNIEnv *env, jobject thiz,
                                                                               jlong synthesizerHandle,
                                                                               jfloat depth) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setLfoDepth(static_cast<float>(depth));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setTremoloDepth(JNIEnv *env, jobject thiz,
                                                                                   jlong synthesizerHandle,
                                                                                   jfloat depth) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setTremoloDepth(static_cast<float>(depth));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setDelayTime(JNIEnv *env, jobject thiz,
                                                                                jlong synthesizerHandle,
                                                                                jfloat seconds) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setDelayTime(static_cast<float>(seconds));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setDelayFeedback(JNIEnv *env, jobject thiz,
                                                                                    jlong synthesizerHandle,
                                                                                    jfloat feedback) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setDelayFeedback(static_cast<float>(feedback));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setDelayWet(JNIEnv *env, jobject thiz,
                                                                               jlong synthesizerHandle,
                                                                               jfloat wet) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setDelayWet(static_cast<float>(wet));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setMetronomeEnabled(JNIEnv *env, jobject thiz,
                                                                                        jlong synthesizerHandle,
                                                                                        jboolean enabled) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setMetronomeEnabled(static_cast<bool>(enabled));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setBpm(JNIEnv *env, jobject thiz,
                                                                          jlong synthesizerHandle,
                                                                          jfloat bpm) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setBpm(static_cast<float>(bpm));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setRecording(JNIEnv *env, jobject thiz,
                                                                                 jlong synthesizerHandle,
                                                                                 jboolean enabled) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setRecording(static_cast<bool>(enabled));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setPlayback(JNIEnv *env, jobject thiz,
                                                                                jlong synthesizerHandle,
                                                                                jboolean enabled) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setPlayback(static_cast<bool>(enabled));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_clearSequence(JNIEnv *env, jobject thiz,
                                                                                  jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->clearSequence();
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_clearActiveTrack(JNIEnv *env, jobject thiz,
                                                                                     jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->clearActiveTrack();
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setQuantizationMode(JNIEnv *env, jobject thiz,
                                                                                        jlong synthesizerHandle,
                                                                                        jint mode) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setQuantizationMode(static_cast<int>(mode));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setActiveTrack(JNIEnv *env, jobject thiz,
                                                                                   jlong synthesizerHandle,
                                                                                   jint trackId) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setActiveTrack(static_cast<int>(trackId));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_triggerKick(JNIEnv *env, jobject thiz,
                                                                               jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->triggerKick();
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_triggerSnare(JNIEnv *env, jobject thiz,
                                                                                jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->triggerSnare();
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_triggerHat(JNIEnv *env, jobject thiz,
                                                                              jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->triggerHat();
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setDrumVolume(JNIEnv *env, jobject thiz,
                                                                                 jlong synthesizerHandle,
                                                                                 jfloat volumeInDb) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setDrumVolume(static_cast<float>(volumeInDb));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_clearDrums(JNIEnv *env, jobject thiz,
                                                                              jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->clearDrums();
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setArrangementMode(JNIEnv *env, jobject thiz,
                                                                                      jlong synthesizerHandle,
                                                                                      jboolean enabled) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setArrangementMode(static_cast<bool>(enabled));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_addPatternToPlaylist(JNIEnv *env, jobject thiz,
                                                                                        jlong synthesizerHandle,
                                                                                        jint patternId) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->addPatternToPlaylist(static_cast<int>(patternId));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_clearPlaylist(JNIEnv *env, jobject thiz,
                                                                                  jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->clearPlaylist();
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_setActivePattern(JNIEnv *env, jobject thiz,
                                                                                    jlong synthesizerHandle,
                                                                                    jint patternId) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->setActivePattern(static_cast<int>(patternId));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_copyPattern(JNIEnv *env, jobject thiz,
                                                                               jlong synthesizerHandle,
                                                                               jint sourceId, jint targetId) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->copyPattern(static_cast<int>(sourceId), static_cast<int>(targetId));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_removePattern(JNIEnv *env, jobject thiz,
                                                                                 jlong synthesizerHandle,
                                                                                 jint patternId) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->removePattern(static_cast<int>(patternId));
}

JNIEXPORT jint JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_getCurrentPlaylistIndex(JNIEnv *env, jobject thiz,
                                                                                           jlong synthesizerHandle) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) return synthesizer->getCurrentPlaylistIndex();
    return 0;
}

JNIEXPORT jfloatArray JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_getEvents(JNIEnv *env, jobject thiz,
                                                                               jlong synthesizerHandle,
                                                                               jint patternId) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (!synthesizer) return nullptr;

    int count = synthesizer->getEventCount(patternId);
    // Для каждого события передаем 5 параметров: timestamp, frequency, isNoteOn, trackId, isDrum
    jfloatArray result = env->NewFloatArray(count * 5);
    jfloat* elements = env->GetFloatArrayElements(result, nullptr);

    for (int i = 0; i < count; ++i) {
        auto event = synthesizer->getEvent(patternId, i);
        elements[i * 5 + 0] = static_cast<float>(event.timestamp);
        elements[i * 5 + 1] = event.frequency;
        elements[i * 5 + 2] = event.isNoteOn ? 1.0f : 0.0f;
        elements[i * 5 + 3] = static_cast<float>(event.trackId);
        elements[i * 5 + 4] = event.isDrum ? 1.0f : 0.0f;
    }

    env->ReleaseFloatArrayElements(result, elements, 0);
    return result;
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_updateEventTimestamp(JNIEnv *env, jobject thiz,
                                                                                          jlong synthesizerHandle,
                                                                                          jint patternId, jint index,
                                                                                          jlong newTimestamp) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->updateEventTimestamp(patternId, index, static_cast<uint64_t>(newTimestamp));
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_deleteEvent(JNIEnv *env, jobject thiz,
                                                                                jlong synthesizerHandle,
                                                                                jint patternId, jint index) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->deleteEvent(patternId, index);
}

JNIEXPORT void JNICALL
Java_com_chumakov123_wavetablesynthesizer_NativeWavetableSynthesizer_quantizePattern(JNIEnv *env, jobject thiz,
                                                                                     jlong synthesizerHandle,
                                                                                     jint patternId, jint mode) {
    auto* synthesizer = reinterpret_cast<wavetablesynthesizer::WavetableSynthesizer*>(synthesizerHandle);
    if (synthesizer) synthesizer->quantizePattern(patternId, mode);
}
}


