package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel
import kotlin.math.pow

@Composable
fun PianoKeyboard(synthesizerViewModel: WavetableSynthesizerViewModel) {
    val octave by synthesizerViewModel.octave.observeAsState(0)
    val multiplier = 2f.pow(octave)

    val baseWhiteNotes = remember {
        listOf(
            "C" to 261.63f, "D" to 293.66f, "E" to 329.63f, "F" to 349.23f,
            "G" to 392.00f, "A" to 440.00f, "B" to 493.88f, "C2" to 523.25f
        )
    }

    val baseBlackNotes = remember {
        listOf(
            ("C#" to 277.18f) to 0, ("D#" to 311.13f) to 1, ("F#" to 369.99f) to 3,
            ("G#" to 415.30f) to 4, ("A#" to 466.16f) to 5
        )
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

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { synthesizerViewModel.setOctave(octave - 1) }) {
                Text("-")
            }
            Text(text = "Octave: $octave")
            Button(onClick = { synthesizerViewModel.setOctave(octave + 1) }) {
                Text("+")
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 16.dp)
        ) {
            val whiteKeyWidth = this.maxWidth / baseWhiteNotes.size
            val blackKeyWidth = whiteKeyWidth * 0.6f
            val blackKeyHeight = this.maxHeight * 0.6f

            val whiteKeyWidthPx = constraints.maxWidth.toFloat() / baseWhiteNotes.size
            val blackKeyWidthPx = whiteKeyWidthPx * 0.6f
            val blackKeyHeightPx = constraints.maxHeight.toFloat() * 0.6f

            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(whiteKeyWidthPx, blackKeyWidthPx, blackKeyHeightPx) {
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
                    baseWhiteNotes.forEach { (name, baseFreq) ->
                        val freq = baseFreq * multiplier
                        PianoKey(
                            name = name,
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
                    val (name, baseFreq) = noteInfo
                    val freq = baseFreq * multiplier
                    PianoKey(
                        name = name,
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
}

private fun getFrequencyAt(
    position: Offset,
    whiteKeyWidth: Float,
    blackKeyWidth: Float,
    blackKeyHeight: Float,
    whiteNotes: List<Pair<String, Float>>,
    blackNotes: List<Pair<Pair<String, Float>, Int>>
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
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
