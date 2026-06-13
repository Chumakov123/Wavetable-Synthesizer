package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.MusicTheory
import com.chumakov123.wavetablesynthesizer.MusicalKey
import com.chumakov123.wavetablesynthesizer.Scale
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel
import kotlin.math.pow

@Composable
fun PianoKeyboard(synthesizerViewModel: WavetableSynthesizerViewModel) {
    val octave by synthesizerViewModel.octave.observeAsState(0)
    val multiplier = 2f.pow(octave)

    val selectedScale by synthesizerViewModel.selectedScale.observeAsState(Scale.CHROMATIC)
    val selectedKey by synthesizerViewModel.selectedKey.observeAsState(MusicalKey.C)

    val isChromatic = selectedScale == Scale.CHROMATIC

    val baseWhiteNotes = remember(selectedScale, selectedKey) {
        if (isChromatic) {
            listOf(
                60 to 261.63f, 62 to 293.66f, 64 to 329.63f, 65 to 349.23f,
                67 to 392.00f, 69 to 440.00f, 71 to 493.88f,
                72 to 523.25f, 74 to 587.33f, 76 to 659.25f, 77 to 698.46f,
                79 to 783.99f, 81 to 880.00f, 83 to 987.77f, 84 to 1046.50f
            )
        } else {
            val notes = mutableListOf<Pair<Int, Float>>()
            var currentMidi = 60 + selectedKey.semitonesFromC
            while (notes.size < 15) {
                if (MusicTheory.isNoteInScale(currentMidi, selectedKey, selectedScale)) {
                    notes.add(currentMidi to MusicTheory.getFrequency(currentMidi))
                }
                currentMidi++
            }
            notes
        }
    }

    val baseBlackNotes = remember(selectedScale, selectedKey) {
        if (isChromatic) {
            listOf(
                (61 to 277.18f) to 0, (63 to 311.13f) to 1, (66 to 369.99f) to 3,
                (68 to 415.30f) to 4, (70 to 466.16f) to 5,
                (73 to 554.37f) to 7, (75 to 622.25f) to 8, (78 to 739.99f) to 10,
                (80 to 830.61f) to 11, (82 to 932.33f) to 12
            )
        } else {
            emptyList()
        }
    }

    val activeNotes by synthesizerViewModel.activeNotes.observeAsState(emptySet())
    val pointerToBaseFreq = remember { mutableMapOf<PointerId, Float>() }

    val currentMultiplierState = rememberUpdatedState(multiplier)

    val prevMultiplier = remember { mutableFloatStateOf(multiplier) }
    LaunchedEffect(multiplier) {
        val oldMult = prevMultiplier.floatValue
        if (oldMult != multiplier) {
            pointerToBaseFreq.values.forEach { baseFreq ->
                synthesizerViewModel.noteOff(baseFreq * oldMult)
                synthesizerViewModel.noteOn(baseFreq * multiplier)
            }
            prevMultiplier.floatValue = multiplier
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val whiteKeyWidth = this.maxWidth / baseWhiteNotes.size
        val blackKeyWidth = whiteKeyWidth * 0.6f
        val blackKeyHeight = this.maxHeight * 0.6f

        val whiteKeyWidthPx = constraints.maxWidth.toFloat() / baseWhiteNotes.size
        val blackKeyWidthPx = whiteKeyWidthPx * 0.6f
        val blackKeyHeightPx = constraints.maxHeight.toFloat() * 0.6f

        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(whiteKeyWidthPx, blackKeyWidthPx, blackKeyHeightPx, baseWhiteNotes, baseBlackNotes) {
                awaitEachGesture {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            val pointerId = change.id
                            if (change.pressed) {
                                val newBaseFreq = getFrequencyAt(
                                    change.position,
                                    whiteKeyWidthPx,
                                    blackKeyWidthPx,
                                    blackKeyHeightPx,
                                    baseWhiteNotes,
                                    baseBlackNotes
                                )
                                val oldBaseFreq = pointerToBaseFreq[pointerId]

                                if (newBaseFreq != oldBaseFreq) {
                                    val m = currentMultiplierState.value
                                    if (oldBaseFreq != null) synthesizerViewModel.noteOff(oldBaseFreq * m)
                                    if (newBaseFreq != null) {
                                        synthesizerViewModel.noteOn(newBaseFreq * m)
                                        pointerToBaseFreq[pointerId] = newBaseFreq
                                    } else {
                                        pointerToBaseFreq.remove(pointerId)
                                    }
                                }
                                change.consume()
                            } else {
                                val releasedBaseFreq = pointerToBaseFreq.remove(pointerId)
                                if (releasedBaseFreq != null) {
                                    synthesizerViewModel.noteOff(releasedBaseFreq * currentMultiplierState.value)
                                }
                            }
                        }
                        if (event.changes.all { !it.pressed }) break
                    }
                }
            }) {
            // Белые клавиши
            Row(modifier = Modifier.fillMaxSize()) {
                baseWhiteNotes.forEach { (baseMidi, baseFreq) ->
                    val actualMidi = baseMidi + octave * 12
                    val freq = baseFreq * multiplier
                    PianoKey(
                        name = MusicTheory.getNoteName(actualMidi),
                        isActive = activeNotes.contains(freq),
                        color = Color.White,
                        activeColor = Color.Yellow,
                        textColor = Color.Black,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Black)
                    )
                }
            }

            // Черные клавиши
            baseBlackNotes.forEach { (noteInfo, index) ->
                val (baseMidi, baseFreq) = noteInfo
                val actualMidi = baseMidi + octave * 12
                val freq = baseFreq * multiplier
                PianoKey(
                    name = MusicTheory.getNoteName(actualMidi),
                    isActive = activeNotes.contains(freq),
                    color = Color.Black,
                    activeColor = Color.Yellow,
                    textColor = Color.White,
                    modifier = Modifier
                        .offset(x = whiteKeyWidth * (index + 1) - blackKeyWidth / 2)
                        .size(blackKeyWidth, blackKeyHeight)
                )
            }
        }
    }
}

private fun getFrequencyAt(
    position: Offset,
    whiteKeyWidth: Float,
    blackKeyWidth: Float,
    blackKeyHeight: Float,
    whiteNotes: List<Pair<Int, Float>>,
    blackNotes: List<Pair<Pair<Int, Float>, Int>>
): Float? {
    val x = position.x
    val y = position.y

    // Сначала проверяем черные клавиши (они сверху)
    blackNotes.forEach { (noteInfo, index) ->
        val (_, freq) = noteInfo
        val left = whiteKeyWidth * (index + 1) - blackKeyWidth / 2
        val right = left + blackKeyWidth
        if (x in left..right && y >= 0 && y <= blackKeyHeight) {
            return freq
        }
    }

    // Затем белые
    val whiteIndex = (x / whiteKeyWidth).toInt()
    if (whiteIndex in whiteNotes.indices && y >= 0) {
        return whiteNotes[whiteIndex].second
    }

    return null
}

@Composable
private fun PianoKey(
    name: String,
    isActive: Boolean,
    color: Color,
    activeColor: Color,
    textColor: Color,
    modifier: Modifier
) {
    Box(
        modifier = modifier.background(if (isActive) activeColor else color),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            text = name,
            color = textColor,
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}
