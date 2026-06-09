package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun PianoKeyboard(synthesizerViewModel: WavetableSynthesizerViewModel) {
    val whiteNotes = listOf(
        "C" to 261.63f,
        "D" to 293.66f,
        "E" to 329.63f,
        "F" to 349.23f,
        "G" to 392.00f,
        "A" to 440.00f,
        "B" to 493.88f,
        "C2" to 523.25f
    )

    val blackNotes = listOf(
        "C#" to 277.18f to 0, // после C
        "D#" to 311.13f to 1, // после D
        "F#" to 369.99f to 3, // после F
        "G#" to 415.30f to 4, // после G
        "A#" to 466.16f to 5  // после A
    )

    val activeNotes = synthesizerViewModel.activeNotes.observeAsState(emptySet())

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp)
    ) {
        val whiteKeyWidth = this.maxWidth / whiteNotes.size
        val blackKeyWidth = whiteKeyWidth * 0.6f
        val blackKeyHeight = this.maxHeight * 0.6f

        // Белые клавиши
        Row(modifier = Modifier.fillMaxSize()) {
            whiteNotes.forEach { (name, freq) ->
                PianoKey(
                    name = name,
                    freq = freq,
                    isActive = activeNotes.value.contains(freq),
                    color = Color.White,
                    activeColor = Color.Yellow,
                    textColor = Color.Black,
                    synthesizerViewModel = synthesizerViewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(2.dp)
                )
            }
        }

        // Черные клавиши
        blackNotes.forEach { (noteInfo, index) ->
            val (name, freq) = noteInfo
            PianoKey(
                name = name,
                freq = freq,
                isActive = activeNotes.value.contains(freq),
                color = Color.Black,
                activeColor = Color.Yellow,
                textColor = Color.White,
                synthesizerViewModel = synthesizerViewModel,
                modifier = Modifier
                    .offset(x = whiteKeyWidth * (index + 1) - blackKeyWidth / 2)
                    .size(blackKeyWidth, blackKeyHeight)
            )
        }
    }
}

@Composable
private fun PianoKey(
    name: String,
    freq: Float,
    isActive: Boolean,
    color: Color,
    activeColor: Color,
    textColor: Color,
    synthesizerViewModel: WavetableSynthesizerViewModel,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .background(if (isActive) activeColor else color)
            .pointerInput(freq) {
                detectTapGestures(
                    onPress = {
                        synthesizerViewModel.noteOn(freq)
                        tryAwaitRelease()
                        synthesizerViewModel.noteOff(freq)
                    }
                )
            },
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
