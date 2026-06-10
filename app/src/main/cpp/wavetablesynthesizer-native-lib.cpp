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
}


