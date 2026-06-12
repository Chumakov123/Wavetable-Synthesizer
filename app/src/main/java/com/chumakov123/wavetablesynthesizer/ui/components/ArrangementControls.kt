package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.MainActivity
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun ArrangementControls(viewModel: WavetableSynthesizerViewModel) {
    val isArrangementMode by viewModel.isArrangementMode.observeAsState(false)
    val activePattern by viewModel.activePattern.observeAsState(0)
    val playlist by viewModel.playlist.observeAsState(emptyList())
    val currentPlaylistIndex by viewModel.currentPlaylistIndex.observeAsState(0)
    
    val isVocalEnabled by viewModel.isVocalTrackEnabled.observeAsState(true)
    val vocalOffset by viewModel.vocalTrackOffset.observeAsState(0f)
    val vocalVolume by viewModel.vocalTrackVolume.observeAsState(0f)
    val vocalPath by viewModel.vocalTrackPath.observeAsState(null)
    val isMicRecording by viewModel.isMicRecording.observeAsState(false)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ARRANGE", color = Color.White, fontSize = 10.sp)
                Switch(
                    checked = isArrangementMode,
                    onCheckedChange = { viewModel.toggleArrangementMode() },
                    modifier = Modifier.scale(0.6f)
                )

                Spacer(Modifier.width(8.dp))

                // Mic recording button
                IconButton(
                    onClick = { (context as? MainActivity)?.toggleMicRecording() },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Mic",
                        tint = if (isMicRecording) Color.Red else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (vocalPath != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text("VOC", fontSize = 8.sp, color = Color.Yellow)
                        Switch(
                            checked = isVocalEnabled,
                            onCheckedChange = { viewModel.setVocalTrackEnabled(it) },
                            modifier = Modifier.scale(0.5f).size(30.dp, 20.dp)
                        )
                        
                        Text("OFS", fontSize = 7.sp, color = Color.Gray)
                        Knob(
                            value = vocalOffset,
                            onValueChange = { viewModel.setVocalTrackOffset(it) },
                            valueRange = -1f..1f,
                            modifier = Modifier.size(18.dp)
                        )
                        
                        Text("VOL", fontSize = 7.sp, color = Color.Gray)
                        Knob(
                            value = vocalVolume,
                            onValueChange = { viewModel.setVocalTrackVolume(it) },
                            valueRange = -60f..12f,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PatternSelector(viewModel, activePattern)
            }
        }

        // Playlist view
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PLAYLIST: ", color = Color.Gray, fontSize = 10.sp)
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(playlist) { index, patternId ->
                    val isPlaying = isArrangementMode && index == currentPlaylistIndex
                    Box(
                        modifier = Modifier
                            .background(
                                if (isPlaying) Color(0xFF4CAF50) else Color.DarkGray,
                                RoundedCornerShape(4.dp)
                            )
                            .border(1.dp, if (isPlaying) Color.White else Color.Transparent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("P$patternId", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            IconButton(
                onClick = { viewModel.clearPlaylist() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Red, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun PatternSelector(viewModel: WavetableSynthesizerViewModel, activePattern: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("PATTERN:", color = Color.Gray, fontSize = 10.sp)
        
        // Buttons for patterns 0-3 for start
        (0..3).forEach { id ->
            val isSelected = activePattern == id
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (isSelected) Color(0xFF4CAF50) else Color.DarkGray,
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { viewModel.setActivePattern(id) }
                    .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(id.toString(), color = Color.White, fontSize = 10.sp)
            }
        }

        Spacer(Modifier.width(8.dp))

        IconButton(onClick = { viewModel.addPatternToPlaylist(activePattern) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Add, "Add to playlist", tint = Color.White, modifier = Modifier.size(16.dp))
        }

        IconButton(onClick = { 
            // Copy current to next free or just next for now
            viewModel.copyActivePatternTo((activePattern + 1) % 4)
        }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.ContentCopy, "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
        }

        IconButton(onClick = { viewModel.removePattern(activePattern) }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, "Clear pattern", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}
