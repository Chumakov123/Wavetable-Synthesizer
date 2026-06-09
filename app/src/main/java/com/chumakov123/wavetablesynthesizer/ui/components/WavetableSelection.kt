package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.wavetablesynthesizer.R
import com.chumakov123.wavetablesynthesizer.Wavetable
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun WavetableSelectionPanel(
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val isKeyboardMode = synthesizerViewModel.isKeyboardMode.observeAsState(true)
    val isRecording = synthesizerViewModel.isRecording.observeAsState(false)
    val isPlayingRecording = synthesizerViewModel.isPlayingRecording.observeAsState(false)
    val currentWavetable = synthesizerViewModel.wavetable.observeAsState(Wavetable.SINE)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.25f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.wavetable))
            WavetableSelectionButtons(
                currentWavetable = currentWavetable.value ?: Wavetable.SINE,
                onWavetableSelected = { synthesizerViewModel.setWavetable(it) }
            )
        }

        Row(
            modifier = Modifier.weight(0.5f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Keyboard")
                Switch(
                    checked = isKeyboardMode.value,
                    onCheckedChange = { synthesizerViewModel.setKeyboardMode(it) }
                )
            }

            Button(
                onClick = { synthesizerViewModel.toggleRecording() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording.value) Color.Red else Color.Gray
                )
            ) {
                Text(if (isRecording.value) "Stop Rec" else "Record")
            }

            Button(
                onClick = { synthesizerViewModel.playRecording() },
                enabled = !isRecording.value && !isPlayingRecording.value
            ) {
                Text(if (isPlayingRecording.value) "Playing..." else "Play Rec")
            }
        }
    }
}

@Composable
fun WavetableSelectionButtons(
    currentWavetable: Wavetable,
    onWavetableSelected: (Wavetable) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (wavetable in Wavetable.entries) {
            WavetableIconButton(
                wavetable = wavetable,
                isSelected = wavetable == currentWavetable,
                onClick = { onWavetableSelected(wavetable) }
            )
        }
    }
}

@Composable
fun WavetableIconButton(
    wavetable: Wavetable,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.padding(4.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            painter = painterResource(wavetable.toResourceImage()),
            contentDescription = stringResource(wavetable.toResourceString())
        )
    }
}
