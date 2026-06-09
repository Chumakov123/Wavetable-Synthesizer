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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun PianoKeyboard(synthesizerViewModel: WavetableSynthesizerViewModel) {
    val whiteNotes = listOf(
        "C" to 261.63f, "D" to 293.66f, "E" to 329.63f, "F" to 349.23f,
        "G" to 392.00f, "A" to 440.00f, "B" to 493.88f, "C2" to 523.25f
    )

    val blackNotes = listOf(
        "C#" to 277.18f to 0, "D#" to 311.13f to 1, "F#" to 369.99f to 3,
        "G#" to 415.30f to 4, "A#" to 466.16f to 5
    )

    val activeNotes = synthesizerViewModel.activeNotes.observeAsState(emptySet())
    val pointerToFreq = remember { mutableMapOf<PointerId, Float>() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        val whiteKeyWidth = this.maxWidth / whiteNotes.size
        val blackKeyWidth = whiteKeyWidth * 0.6f
        val blackKeyHeight = this.maxHeight * 0.6f
        
        val whiteKeyWidthPx = constraints.maxWidth.toFloat() / whiteNotes.size
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
                                val newFreq = getFrequencyAt(
                                    change.position,
                                    whiteKeyWidthPx,
                                    blackKeyWidthPx,
                                    blackKeyHeightPx,
                                    whiteNotes,
                                    blackNotes
                                )
                                val oldFreq = pointerToFreq[pointerId]

                                if (newFreq != oldFreq) {
                                    if (oldFreq != null) synthesizerViewModel.noteOff(oldFreq)
                                    if (newFreq != null) synthesizerViewModel.noteOn(newFreq)
                                    if (newFreq != null) {
                                        pointerToFreq[pointerId] = newFreq
                                    } else {
                                        pointerToFreq.remove(pointerId)
                                    }
                                }
                                change.consume()
                            } else {
                                val releasedFreq = pointerToFreq.remove(pointerId)
                                if (releasedFreq != null) {
                                    synthesizerViewModel.noteOff(releasedFreq)
                                }
                            }
                        }
                        // Выходим из цикла, если все пальцы подняты
                        if (event.changes.all { !it.pressed }) break
                    }
                }
            }) {
            // Белые клавиши
            Row(modifier = Modifier.fillMaxSize()) {
                whiteNotes.forEach { (name, freq) ->
                    PianoKey(
                        name = name,
                        isActive = activeNotes.value.contains(freq),
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
            blackNotes.forEach { (noteInfo, index) ->
                val (name, freq) = noteInfo
                PianoKey(
                    name = name,
                    isActive = activeNotes.value.contains(freq),
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
