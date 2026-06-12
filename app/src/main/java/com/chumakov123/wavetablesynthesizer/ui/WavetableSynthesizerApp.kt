package com.chumakov123.wavetablesynthesizer.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel
import com.chumakov123.wavetablesynthesizer.ui.components.AdsrControls
import com.chumakov123.wavetablesynthesizer.ui.components.ArrangementControls
import com.chumakov123.wavetablesynthesizer.ui.components.DrumSection
import com.chumakov123.wavetablesynthesizer.ui.components.FxControls
import com.chumakov123.wavetablesynthesizer.ui.components.GridEditor
import com.chumakov123.wavetablesynthesizer.ui.components.LfoControls
import com.chumakov123.wavetablesynthesizer.ui.components.MetronomeControl
import com.chumakov123.wavetablesynthesizer.ui.components.OctaveControl
import com.chumakov123.wavetablesynthesizer.ui.components.PianoKeyboard
import com.chumakov123.wavetablesynthesizer.ui.components.PitchControl
import com.chumakov123.wavetablesynthesizer.ui.components.PlayControl
import com.chumakov123.wavetablesynthesizer.ui.components.PresetSelector
import com.chumakov123.wavetablesynthesizer.ui.components.ProjectStatus
import com.chumakov123.wavetablesynthesizer.ui.components.SynthDialogs
import com.chumakov123.wavetablesynthesizer.ui.components.TrackSelector
import com.chumakov123.wavetablesynthesizer.ui.components.TransportControls
import com.chumakov123.wavetablesynthesizer.ui.components.VocalTrackControls
import com.chumakov123.wavetablesynthesizer.ui.components.VolumeControl
import com.chumakov123.wavetablesynthesizer.ui.components.WavetableSelectionPanel

@Composable
fun WavetableSynthesizerApp(
    modifier: Modifier,
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val isKeyboardMode by synthesizerViewModel.isKeyboardMode.observeAsState(true)
    val isDrumsMode by synthesizerViewModel.isDrumsMode.observeAsState(false)
    val panelMode by synthesizerViewModel.controlPanelMode.observeAsState(WavetableSynthesizerViewModel.ControlPanelMode.WAVE)
    val isArrangementExpanded by synthesizerViewModel.isArrangementExpanded.observeAsState(false)
    val isDirty by synthesizerViewModel.isDirty.observeAsState(false)

    BackHandler(enabled = isDirty) {
        synthesizerViewModel.showDialog(WavetableSynthesizerViewModel.DialogType.SAVE_CONFIRMATION)
    }

    // Чтобы сетка не закрывалась при переключении панелей, вынесем её состояние из panelMode
    var showGrid by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Ряд 1: Глобальное управление (Studio + Synth Settings)
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF222222), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Группа слева: Клавиатура, Октава, Громкость
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PIANO", fontSize = 8.sp, color = if (isKeyboardMode) Color.White else Color.Gray)
                        Switch(
                            checked = isKeyboardMode,
                            onCheckedChange = { synthesizerViewModel.setKeyboardMode(it) },
                            modifier = Modifier.scale(0.5f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("DRUMS", fontSize = 8.sp, color = if (isDrumsMode) Color.White else Color.Gray)
                        Switch(
                            checked = isDrumsMode,
                            onCheckedChange = { synthesizerViewModel.setDrumsMode(it) },
                            modifier = Modifier.scale(0.5f)
                        )
                    }
                    OctaveControl(synthesizerViewModel)
                    VolumeControl(synthesizerViewModel)
                }

                // Центр: ARRANGE и GRID
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { synthesizerViewModel.toggleArrangementExpanded() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ViewHeadline, 
                            contentDescription = "Arrange", 
                            tint = if (isArrangementExpanded) Color(0xFF4CAF50) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showGrid = !showGrid },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.GridOn, 
                            contentDescription = "Grid", 
                            tint = if (showGrid) Color(0xFF2196F3) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Группа справа: Метроном, Транспорт
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MetronomeControl(synthesizerViewModel)
                    TransportControls(synthesizerViewModel)
                }
            }

            // Arrangement Controls (Collapsible)
            if (isArrangementExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ArrangementControls(synthesizerViewModel)
                    VocalTrackControls(synthesizerViewModel)
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
                        WavetableSynthesizerViewModel.ControlPanelMode.GRID -> {
                            WavetableSelectionPanel(synthesizerViewModel)
                        }
                    }
                }

                // Logo U-DAW
                Column(
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "U-DAW",
                        style = TextStyle(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFBB00), Color(0xFFFF6600))
                            ),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = "Synthesizer",
                        color = Color.Gray,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    )
                }
            }

            // Нижняя часть: Клавиатура или Барабаны
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    showGrid -> GridEditor(synthesizerViewModel)
                    isDrumsMode -> DrumSection(synthesizerViewModel)
                    isKeyboardMode -> PianoKeyboard(synthesizerViewModel)
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            PitchControl(synthesizerViewModel)
                            PlayControl(synthesizerViewModel)
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 2.dp, end = 8.dp)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            ProjectStatus(synthesizerViewModel)
        }
    }
    
    SynthDialogs(synthesizerViewModel)
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
        WavetableSynthesizerViewModel.ControlPanelMode.entries
            .filter { it != WavetableSynthesizerViewModel.ControlPanelMode.GRID }
            .forEach { mode ->
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
