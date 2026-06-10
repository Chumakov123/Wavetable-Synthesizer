package com.chumakov123.wavetablesynthesizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.chumakov123.wavetablesynthesizer.ui.components.FxControls
import com.chumakov123.wavetablesynthesizer.ui.components.LfoControls
import com.chumakov123.wavetablesynthesizer.ui.components.MetronomeControl
import com.chumakov123.wavetablesynthesizer.ui.components.OctaveControl
import com.chumakov123.wavetablesynthesizer.ui.components.TransportControls
import com.chumakov123.wavetablesynthesizer.ui.components.PitchControl
import com.chumakov123.wavetablesynthesizer.ui.components.PlayControl
import com.chumakov123.wavetablesynthesizer.ui.components.PianoKeyboard
import com.chumakov123.wavetablesynthesizer.ui.components.PresetSelector
import com.chumakov123.wavetablesynthesizer.ui.components.TrackSelector
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
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Ряд 1: Глобальное управление (Studio + Synth Settings)
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF222222), RoundedCornerShape(4.dp)).padding(2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Группа слева: Клавиатура, Октава, Громкость
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Switch(
                    checked = isKeyboardMode,
                    onCheckedChange = { synthesizerViewModel.setKeyboardMode(it) },
                    modifier = Modifier.scale(0.5f)
                )
                OctaveControl(synthesizerViewModel)
                VolumeControl(synthesizerViewModel)
            }

            // Группа справа: Метроном, Транспорт
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                MetronomeControl(synthesizerViewModel)
                TransportControls(synthesizerViewModel)
            }
        }

        // Ряд 2: Параметры звука (Presets + Mode + Content)
        Row(
            modifier = Modifier.fillMaxWidth().height(65.dp).padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PresetSelector(synthesizerViewModel)
                    TrackSelector(synthesizerViewModel)
                }
                ModeSelector(
                    currentMode = panelMode,
                    onModeSelected = { synthesizerViewModel.setControlPanelMode(it) }
                )
            }

            // Контент выбранной панели (WAVE, ADSR, LFO)
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                when (panelMode) {
                    WavetableSynthesizerViewModel.ControlPanelMode.WAVE -> WavetableSelectionPanel(synthesizerViewModel)
                    WavetableSynthesizerViewModel.ControlPanelMode.ADSR -> AdsrControls(synthesizerViewModel)
                    WavetableSynthesizerViewModel.ControlPanelMode.LFO -> LfoControls(synthesizerViewModel)
                    WavetableSynthesizerViewModel.ControlPanelMode.FX -> FxControls(synthesizerViewModel)
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
            .padding(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
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
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = mode.name,
                    fontSize = 8.sp,
                    color = if (isSelected) Color.White else Color.LightGray
                )
            }
        }
    }
}
