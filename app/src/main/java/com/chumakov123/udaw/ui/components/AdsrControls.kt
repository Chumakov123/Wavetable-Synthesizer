package com.chumakov123.udaw.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.udaw.MainViewModel
import java.util.Locale

@Composable
fun AdsrControls(
    synthesizerViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val attack = synthesizerViewModel.attack.observeAsState(0.01f)
    val decay = synthesizerViewModel.decay.observeAsState(0.1f)
    val sustain = synthesizerViewModel.sustain.observeAsState(0.7f)
    val release = synthesizerViewModel.release.observeAsState(0.3f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdsrKnob(
            label = "A",
            value = attack.value,
            onValueChange = { synthesizerViewModel.setAttack(it) },
            valueRange = 0.001f..2.0f
        )
        AdsrKnob(
            label = "D",
            value = decay.value,
            onValueChange = { synthesizerViewModel.setDecay(it) },
            valueRange = 0.001f..2.0f
        )
        AdsrKnob(
            label = "S",
            value = sustain.value,
            onValueChange = { synthesizerViewModel.setSustain(it) },
            valueRange = 0.0f..1.0f
        )
        AdsrKnob(
            label = "R",
            value = release.value,
            onValueChange = { synthesizerViewModel.setRelease(it) },
            valueRange = 0.001f..5.0f
        )
    }
}

@Composable
fun OctaveControl(
    synthesizerViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val octave by synthesizerViewModel.octave.observeAsState(0)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text("OCTAVE", fontSize = 10.sp, color = Color.Gray)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { synthesizerViewModel.setOctave(octave - 1) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .background(Color.Black, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = octave.toString(),
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
            }
            IconButton(
                onClick = { synthesizerViewModel.setOctave(octave + 1) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
    }
}

@Composable
private fun AdsrKnob(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, fontSize = 10.sp)
        Knob(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.size(40.dp).padding(2.dp),
            valueRange = valueRange
        )
        Text(
            text = String.format(Locale.US, "%.1f", value),
            fontSize = 9.sp
        )
    }
}
