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
}