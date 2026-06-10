package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.Wavetable
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun WavetableSelectionPanel(
    synthesizerViewModel: WavetableSynthesizerViewModel,
    modifier: Modifier = Modifier
) {
    val isRecording = synthesizerViewModel.isRecording.observeAsState(false)
    val isPlayingRecording = synthesizerViewModel.isPlayingRecording.observeAsState(false)
    val currentWavetable = synthesizerViewModel.wavetable.observeAsState(Wavetable.SINE)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Секция выбора формы волны
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WavetableSelectionButtons(
                currentWavetable = currentWavetable.value,
                onWavetableSelected = { synthesizerViewModel.setWavetable(it) }
            )
        }

        // Кнопки записи
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { synthesizerViewModel.toggleRecording() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording.value) Color.Red else Color.DarkGray
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(if (isRecording.value) "STOP" else "REC", fontSize = 10.sp)
            }

            Button(
                onClick = { synthesizerViewModel.playRecording() },
                enabled = !isRecording.value && !isPlayingRecording.value,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(if (isPlayingRecording.value) "PLAYING" else "PLAY REC", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun WavetableSelectionButtons(
    currentWavetable: Wavetable,
    onWavetableSelected: (Wavetable) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
        modifier = Modifier.size(40.dp).padding(2.dp),
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
