package com.chumakov123.udaw

import kotlinx.serialization.Serializable
import kotlin.math.pow

@Serializable
enum class Scale(val label: String, val intervals: List<Int>) {
    CHROMATIC("None", listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
    MAJOR("Major", listOf(0, 2, 4, 5, 7, 9, 11)),
    MINOR("Minor", listOf(0, 2, 3, 5, 7, 8, 10)),
    DORIAN("Dorian", listOf(0, 2, 3, 5, 7, 9, 10)),
    PHRYGIAN("Phrygian", listOf(0, 1, 3, 5, 7, 8, 10)),
    LYDIAN("Lydian", listOf(0, 2, 4, 6, 7, 9, 11)),
    MIXOLYDIAN("Mixolydian", listOf(0, 2, 4, 5, 7, 9, 10)),
    LOCRIAN("Locrian", listOf(0, 1, 3, 5, 6, 8, 10)),
    HARMONIC_MINOR("Harmonic Minor", listOf(0, 2, 3, 5, 7, 8, 11)),
    MELODIC_MINOR("Melodic Minor", listOf(0, 2, 3, 5, 7, 9, 11)),
    PENTATONIC_MAJOR("Pent. Major", listOf(0, 2, 4, 7, 9)),
    PENTATONIC_MINOR("Pent. Minor", listOf(0, 3, 5, 7, 10))
}

@Serializable
enum class MusicalKey(val label: String, val semitonesFromC: Int) {
    C("C", 0), C_SHARP("C#", 1), D("D", 2), D_SHARP("D#", 3), E("E", 4),
    F("F", 5), F_SHARP("F#", 6), G("G", 7), G_SHARP("G#", 8), A("A", 9),
    A_SHARP("A#", 10), B("B", 11)
}

object MusicTheory {
    val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    fun getFrequency(midiNote: Int): Float {
        return 440f * 2f.pow((midiNote - 69f) / 12f)
    }

    fun getNoteName(midiNote: Int): String {
        val index = ((midiNote % 12) + 12) % 12
        val name = noteNames[index]
        val octave = kotlin.math.floor(midiNote.toDouble() / 12.0).toInt() - 1
        return "$name$octave"
    }

    fun isNoteInScale(midiNote: Int, key: MusicalKey, scale: Scale): Boolean {
        if (scale == Scale.CHROMATIC) return true
        val semitoneInOctave = (midiNote - key.semitonesFromC) % 12
        val normalizedSemitone = if (semitoneInOctave < 0) semitoneInOctave + 12 else semitoneInOctave
        return scale.intervals.contains(normalizedSemitone)
    }
}
