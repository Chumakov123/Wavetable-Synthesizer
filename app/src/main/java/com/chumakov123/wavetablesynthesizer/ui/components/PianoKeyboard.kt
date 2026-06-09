package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun PianoKeyboard(synthesizerViewModel: WavetableSynthesizerViewModel) {
    val notes = listOf(
        "C" to 261.63f,
        "D" to 293.66f,
        "E" to 329.63f,
        "F" to 349.23f,
        "G" to 392.00f,
        "A" to 440.00f,
        "B" to 493.88f,
        "C2" to 523.25f
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        notes.forEach { (name, freq) ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .background(Color.White)
                    .pointerInput(Unit) {
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
                Text(name, color = Color.Black, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}
