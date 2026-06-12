package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.MainActivity
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
fun ProjectStatus(viewModel: WavetableSynthesizerViewModel) {
    val name by viewModel.projectName.observeAsState("untitled")
    val isDirty by viewModel.isDirty.observeAsState(false)
    
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$name${if (isDirty) "*" else ""}",
            color = if (isDirty) Color(0xFFFFCC00) else Color.LightGray,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun TransportControls(viewModel: WavetableSynthesizerViewModel) {
    val isRecording by viewModel.isRecording.observeAsState(false)
    val isMicRecording by viewModel.isMicRecording.observeAsState(false)
    val isPlaying by viewModel.isPlayingRecording.observeAsState(false)
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            // Upper row: Save, Load, Render, Mic
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                IconButton(onClick = {
                    if (viewModel.projectName.value?.startsWith("untitled") == true) {
                        viewModel.showProjectNameDialog()
                    } else {
                        viewModel.saveProject(context)
                    }
                }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.FileUpload, null, Modifier.size(18.dp), tint = Color.Cyan)
                }
                IconButton(onClick = { viewModel.showDialog(WavetableSynthesizerViewModel.DialogType.PROJECT_LIST) }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.FileDownload, null, Modifier.size(18.dp), tint = Color.Magenta)
                }
                IconButton(onClick = { viewModel.renderToWav(context) }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.AudioFile, null, Modifier.size(18.dp), tint = Color.Yellow)
                }
                IconButton(onClick = {
                    (context as? MainActivity)?.toggleMicRecording()
                }, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Mic, null, Modifier.size(18.dp), tint = if (isMicRecording) Color.Red else Color.Gray)
                }
            }
            
            // Lower row: Record, Play/Stop, Delete
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
            }
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

@Composable
fun TrackSelector(viewModel: WavetableSynthesizerViewModel) {
    val selectedTrack by viewModel.selectedTrack.observeAsState(0)
    Row(
        modifier = Modifier.background(Color.DarkGray, RoundedCornerShape(4.dp)).padding(1.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        for (i in 0 until 4) {
            val isSelected = selectedTrack == i
            Box(
                modifier = Modifier
                    .background(if (isSelected) Color(0xFF444444) else Color.Transparent, RoundedCornerShape(2.dp))
                    .clickable { viewModel.setSelectedTrack(i) }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "T${i + 1}",
                    fontSize = 10.sp,
                    color = if (isSelected) Color.Green else Color.LightGray
                )
            }
        }
    }
}

@Composable
fun SynthDialogs(viewModel: WavetableSynthesizerViewModel) {
    val activeDialog by viewModel.activeDialog.observeAsState(WavetableSynthesizerViewModel.DialogType.NONE)
    val context = LocalContext.current

    when (activeDialog) {
        WavetableSynthesizerViewModel.DialogType.PROJECT_NAME -> {
            var name by remember { mutableStateOf(viewModel.projectName.value ?: "") }
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("Save Project As") },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Project Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (name.isNotBlank()) {
                            viewModel.saveProject(context, name)
                            
                            val pending = viewModel.getPendingProjectName()
                            val isExit = viewModel.isExitPending()
                            
                            if (pending != null) {
                                if (pending == "NEW_PROJECT_ACTION") {
                                    viewModel.createNewProject()
                                } else {
                                    viewModel.loadProject(context, pending)
                                }
                            } else if (isExit) {
                                (context as? MainActivity)?.finish()
                            }
                            
                            viewModel.dismissDialog()
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("Cancel") }
                }
            )
        }
        WavetableSynthesizerViewModel.DialogType.SAVE_CONFIRMATION -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("Unsaved Changes") },
                text = { Text("Do you want to save changes to ${viewModel.projectName.value}?") },
                confirmButton = {
                    TextButton(onClick = {
                        if (viewModel.projectName.value == "untitled") {
                            viewModel.showProjectNameDialog()
                        } else {
                            viewModel.saveProject(context)
                            val pending = viewModel.getPendingProjectName()
                            val isExit = viewModel.isExitPending()
                            if (pending != null) {
                                if (pending == "NEW_PROJECT_ACTION") {
                                    viewModel.createNewProject()
                                } else {
                                    viewModel.loadProject(context, pending)
                                }
                            } else if (isExit) {
                                (context as? MainActivity)?.finish()
                            }
                            viewModel.dismissDialog()
                        }
                    }) { Text("Save") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            val pending = viewModel.getPendingProjectName()
                            val isExit = viewModel.isExitPending()
                            viewModel.dismissDialog()
                            
                            if (pending != null) {
                                if (pending == "NEW_PROJECT_ACTION") {
                                    viewModel.createNewProject()
                                } else {
                                    viewModel.loadProject(context, pending)
                                }
                            } else if (isExit) {
                                (context as? MainActivity)?.finish()
                            }
                        }) { Text("Don't Save") }
                        TextButton(onClick = { viewModel.dismissDialog() }) { Text("Cancel") }
                    }
                }
            )
        }
        WavetableSynthesizerViewModel.DialogType.PROJECT_LIST -> {
            val list by viewModel.projectList.observeAsState(emptyList())
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("Projects") },
                text = {
                    LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                        item {
                            Text(
                                text = "+ Create New Project",
                                color = Color.Green,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (viewModel.isDirty.value == true) {
                                            viewModel.onProjectSelected("NEW_PROJECT_ACTION")
                                        } else {
                                            viewModel.createNewProject()
                                            viewModel.dismissDialog()
                                        }
                                    }
                                    .padding(16.dp),
                                fontSize = 16.sp
                            )
                            HorizontalDivider(color = Color.DarkGray)
                        }
                        if (list.isEmpty()) {
                            item {
                                Text("No saved projects.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                            }
                        } else {
                            items(list) { item ->
                                Text(
                                    text = item,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (viewModel.isDirty.value == true) {
                                                viewModel.onProjectSelected(item)
                                            } else {
                                                viewModel.loadProject(context, item)
                                                viewModel.dismissDialog()
                                            }
                                        }
                                        .padding(16.dp),
                                    fontSize = 16.sp
                                )
                                HorizontalDivider(color = Color.DarkGray)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("Close") }
                }
            )
        }
        WavetableSynthesizerViewModel.DialogType.MIGRATION_REQUIRED -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("Storage Migration") },
                text = { Text("You have projects in internal storage. Would you like to select an external folder (U-DAW/Projects) and move them there?") },
                confirmButton = {
                    TextButton(onClick = {
                        (context as? MainActivity)?.requestProjectsFolder()
                        viewModel.dismissDialog()
                    }) { Text("Select Folder") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("Later") }
                }
            )
        }
        WavetableSynthesizerViewModel.DialogType.EXPORT_SETUP -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("Export Setup") },
                text = { Text("Please select an output folder for your WAV files (e.g., U-DAW/Output).") },
                confirmButton = {
                    TextButton(onClick = {
                        (context as? MainActivity)?.requestOutputFolder()
                        viewModel.dismissDialog()
                    }) { Text("Select Folder") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("Cancel") }
                }
            )
        }
        WavetableSynthesizerViewModel.DialogType.RENDERING -> {
            val progress by viewModel.renderingProgress.observeAsState(0f)
            AlertDialog(
                onDismissRequest = { }, // Force wait
                title = { Text("Rendering...") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                        Text("${(progress * 100).toInt()}%")
                    }
                },
                confirmButton = {}
            )
        }
        else -> {}
    }
}
