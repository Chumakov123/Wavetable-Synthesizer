package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    Text(
        text = pitchControlLabel,
    )
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,    )
    Text(
        text = frequencyValueLabel
    )
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

    VolumeControlContent(
        value = volume.value!!,
        onValueChange = {
            synthesizerViewModel.setVolume(it)
        },
        valueRange = synthesizerViewModel.volumeRange
    )
}

@Composable
fun VolumeControlContent(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeMute, contentDescription = null, modifier = Modifier.size(20.dp))
        Knob(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.size(60.dp).padding(4.dp),
            valueRange = valueRange
        )
        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun MetronomeControl(viewModel: WavetableSynthesizerViewModel) {
    val isEnabled by viewModel.isMetronomeEnabled.observeAsState(false)
    val bpm by viewModel.bpm.observeAsState(120f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("METRO", fontSize = 9.sp, color = Color.Gray)
            Switch(
                checked = isEnabled,
                onCheckedChange = { viewModel.setMetronomeEnabled(it) },
                modifier = Modifier.scale(0.6f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("BPM: ${bpm.toInt()}", fontSize = 9.sp, color = Color.Gray)
            Knob(
                value = bpm,
                onValueChange = { viewModel.setBpm(it) },
                valueRange = 40f..240f,
                modifier = Modifier.size(35.dp)
            )
        }
    }
}

@Composable
fun TransportControls(viewModel: WavetableSynthesizerViewModel) {
    val isRecording by viewModel.isRecording.observeAsState(false)
    val isPlaying by viewModel.isPlayingRecording.observeAsState(false)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        IconButton(
            onClick = { viewModel.toggleRecording() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = "Record",
                tint = if (isRecording) Color.Red else Color.Gray
            )
        }

        IconButton(
            onClick = { viewModel.togglePlayback() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Play Loop",
                tint = if (isPlaying) Color.Green else Color.Gray
            )
        }

        IconButton(
            onClick = { viewModel.clearSequence() },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Clear",
                tint = Color.Gray
            )
        }
    }
}
