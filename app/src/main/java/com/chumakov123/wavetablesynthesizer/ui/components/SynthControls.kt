package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.wavetablesynthesizer.R
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun ControlsPanel(
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val isKeyboardMode = synthesizerViewModel.isKeyboardMode.observeAsState(true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isKeyboardMode.value) {
                PianoKeyboard(synthesizerViewModel)
            } else {
                PitchControl(synthesizerViewModel)
                PlayControl(synthesizerViewModel)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VolumeControl(synthesizerViewModel)
        }
    }
}

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
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
        Knob(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.size(100.dp).padding(8.dp),
            valueRange = valueRange
        )
        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeMute, contentDescription = null)
    }
}
