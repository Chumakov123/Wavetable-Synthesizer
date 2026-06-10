package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.R
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun PitchControl(
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val frequency = synthesizerViewModel.frequency.observeAsState()
    PitchControlContent(
        pitchControlLabel = stringResource(R.string.frequency),
        value = synthesizerViewModel.sliderPositionFromFrequencyInHz(frequency.value!!),
        onValueChange = {
            synthesizerViewModel.setFrequencySliderPosition(it)
        },
        valueRange = 0f..1f,
        frequencyValueLabel = stringResource(R.string.frequency_value, frequency.value!!)
    )
}

@Composable
fun PitchControlContent(
    pitchControlLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    frequencyValueLabel: String
) {
    Text(text = pitchControlLabel, fontSize = 12.sp)
    Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    Text(text = frequencyValueLabel, fontSize = 10.sp)
}

@Composable
fun PlayControl(
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val playButtonLabel = synthesizerViewModel.playButtonLabel.observeAsState()
    Button(onClick = { synthesizerViewModel.playClicked() }) {
        Text(stringResource(playButtonLabel.value!!))
    }
}

@Composable
fun VolumeControl(
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val volume = synthesizerViewModel.volume.observeAsState()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.AutoMirrored.Filled.VolumeMute, null, Modifier.size(14.dp), tint = Color.Gray)
        Knob(
            value = volume.value!!,
            onValueChange = { synthesizerViewModel.setVolume(it) },
            modifier = Modifier.size(36.dp).padding(2.dp),
            valueRange = synthesizerViewModel.volumeRange
        )
        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, Modifier.size(14.dp), tint = Color.Gray)
    }
}

@Composable
fun MetronomeControl(viewModel: WavetableSynthesizerViewModel) {
    val isEnabled by viewModel.isMetronomeEnabled.observeAsState(false)
    val bpm by viewModel.bpm.observeAsState(120f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Switch(
            checked = isEnabled,
            onCheckedChange = { viewModel.setMetronomeEnabled(it) },
            modifier = Modifier.scale(0.5f)
        )
        Text("BPM:${bpm.toInt()}", fontSize = 9.sp, color = Color.Gray)
        Knob(
            value = bpm,
            onValueChange = { viewModel.setBpm(it) },
            valueRange = 40f..240f,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun TransportControls(viewModel: WavetableSynthesizerViewModel) {
    val isRecording by viewModel.isRecording.observeAsState(false)
    val isPlaying by viewModel.isPlayingRecording.observeAsState(false)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        IconButton(onClick = { viewModel.toggleRecording() }, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.FiberManualRecord, null, Modifier.size(18.dp), tint = if (isRecording) Color.Red else Color.Gray)
        }
        IconButton(onClick = { viewModel.togglePlayback() }, modifier = Modifier.size(30.dp)) {
            Icon(if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, null, Modifier.size(18.dp), tint = if (isPlaying) Color.Green else Color.Gray)
        }
        IconButton(onClick = { viewModel.clearSequence() }, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Default.Delete, null, Modifier.size(18.dp), tint = Color.Gray)
        }
        QuantizationSelector(viewModel)
    }
}

@Composable
fun QuantizationSelector(viewModel: WavetableSynthesizerViewModel) {
    val currentQuant by viewModel.quantization.observeAsState(WavetableSynthesizerViewModel.Quantization.OFF)
    Row(
        modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(4.dp)).padding(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        WavetableSynthesizerViewModel.Quantization.entries.forEach { mode ->
            val isSelected = currentQuant == mode
            Box(
                modifier = Modifier
                    .background(if (isSelected) Color.Gray else Color.Transparent, RoundedCornerShape(2.dp))
                    .clickable { viewModel.setQuantization(mode) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(text = mode.label, fontSize = 7.sp, color = if (isSelected) Color.White else Color.LightGray)
            }
        }
    }
}
