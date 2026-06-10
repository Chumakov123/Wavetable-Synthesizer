package com.chumakov123.wavetablesynthesizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel
import com.chumakov123.wavetablesynthesizer.ui.components.AdsrControls
import com.chumakov123.wavetablesynthesizer.ui.components.LfoControls
import com.chumakov123.wavetablesynthesizer.ui.components.MetronomeControl
import com.chumakov123.wavetablesynthesizer.ui.components.OctaveControl
import com.chumakov123.wavetablesynthesizer.ui.components.TransportControls
import com.chumakov123.wavetablesynthesizer.ui.components.PitchControl
import com.chumakov123.wavetablesynthesizer.ui.components.PlayControl
import com.chumakov123.wavetablesynthesizer.ui.components.PianoKeyboard
import com.chumakov123.wavetablesynthesizer.ui.components.PresetSelector
import com.chumakov123.wavetablesynthesizer.ui.components.VolumeControl
import com.chumakov123.wavetablesynthesizer.ui.components.WavetableSelectionPanel

@Composable
fun WavetableSynthesizerApp(
    modifier: Modifier,
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val isKeyboardMode by synthesizerViewModel.isKeyboardMode.observeAsState(true)
    val panelMode by synthesizerViewModel.controlPanelMode.observeAsState(WavetableSynthesizerViewModel.ControlPanelMode.WAVE)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Верхняя панель
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая группа: Клавиатура и Октава
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("KEY", fontSize = 9.sp, color = Color.Gray)
                    Switch(
                        checked = isKeyboardMode,
                        onCheckedChange = { synthesizerViewModel.setKeyboardMode(it) },
                        modifier = Modifier.scale(0.6f)
                    )
                }
                OctaveControl(synthesizerViewModel, modifier = Modifier.padding(start = 4.dp))
                MetronomeControl(synthesizerViewModel)
                TransportControls(synthesizerViewModel)
            }

            // Центральная группа: Переключатель панелей WAVE/ADSR
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetSelector(synthesizerViewModel)
                ModeSelector(
                    currentMode = panelMode,
                    onModeSelected = { synthesizerViewModel.setControlPanelMode(it) }
                )
            }

            // Правая группа: Громкость
            VolumeControl(synthesizerViewModel)
        }

        // Контент выбранной панели
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            contentAlignment = Alignment.Center
        ) {
            when (panelMode) {
                WavetableSynthesizerViewModel.ControlPanelMode.WAVE -> {
                    WavetableSelectionPanel(synthesizerViewModel)
                }
                WavetableSynthesizerViewModel.ControlPanelMode.ADSR -> {
                    AdsrControls(synthesizerViewModel)
                }
                WavetableSynthesizerViewModel.ControlPanelMode.LFO -> {
                    LfoControls(synthesizerViewModel)
                }
            }
        }

        // Нижняя часть: Клавиатура
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isKeyboardMode) {
                PianoKeyboard(synthesizerViewModel)
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PitchControl(synthesizerViewModel)
                    PlayControl(synthesizerViewModel)
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    currentMode: WavetableSynthesizerViewModel.ControlPanelMode,
    onModeSelected: (WavetableSynthesizerViewModel.ControlPanelMode) -> Unit
) {
    Row(
        modifier = Modifier
            .background(Color.DarkGray, RoundedCornerShape(4.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        WavetableSynthesizerViewModel.ControlPanelMode.entries.forEach { mode ->
            val isSelected = currentMode == mode
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) Color.Gray else Color.Transparent,
                        RoundedCornerShape(2.dp)
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = mode.name,
                    fontSize = 9.sp,
                    color = if (isSelected) Color.White else Color.LightGray
                )
            }
        }
    }
}
