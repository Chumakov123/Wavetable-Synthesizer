package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.MidiEventData
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridEditor(viewModel: WavetableSynthesizerViewModel) {
    val allEvents by viewModel.patternEvents.observeAsState(emptyList())
    val bpm by viewModel.bpm.observeAsState(120f)
    val isDrumsMode by viewModel.isDrumsMode.observeAsState(false)
    val selectedTrack by viewModel.selectedTrack.observeAsState(0)
    val editMode by viewModel.gridEditMode.observeAsState(WavetableSynthesizerViewModel.GridEditMode.DRAG)

    val filteredEventsWithIndices = allEvents.mapIndexed { index, event -> index to event }
        .filter { (_, event) ->
            if (isDrumsMode) event.isDrum
            else !event.isDrum && event.trackId == selectedTrack
        }

    val freqRange = remember(filteredEventsWithIndices) {
        if (isDrumsMode || filteredEventsWithIndices.isEmpty()) {
            0f..2000f
        } else {
            val freqs = filteredEventsWithIndices.map { it.second.frequency }
            val min = freqs.minOrNull() ?: 200f
            val max = freqs.maxOrNull() ?: 1000f

            val padding = (max - min) * 0.1f
            (min - padding)..(max + padding)
        }
    }

    val sampleRate = 48000f
    val samplesPerBeat = sampleRate * 60f / bpm
    val samplesPer16th = samplesPerBeat / 4f
    val totalSamples = samplesPerBeat * 4f * 4f // 4 такта

    val currentEvents by rememberUpdatedState(allEvents)
    val currentFilteredWithIndices by rememberUpdatedState(filteredEventsWithIndices)
    val currentFreqRange by rememberUpdatedState(freqRange)
    val currentIsDrumsMode by rememberUpdatedState(isDrumsMode)
    val currentTotalSamples by rememberUpdatedState(totalSamples)
    val currentSamplesPer16th by rememberUpdatedState(samplesPer16th)
    val currentEditMode by rememberUpdatedState(editMode)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A), RoundedCornerShape(8.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { offset ->
                            val dragInfo = findEventAt(
                                offset,
                                currentFilteredWithIndices,
                                currentEvents,
                                size,
                                currentTotalSamples,
                                currentFreqRange,
                                currentIsDrumsMode,
                                currentSamplesPer16th,
                                currentEditMode
                            )
                            if (dragInfo != null) {
                                viewModel.deleteEvent(dragInfo.noteOnIndex)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    var dragInfo: DragInfo? = null
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragInfo = findEventAt(
                                offset,
                                currentFilteredWithIndices,
                                currentEvents,
                                size,
                                currentTotalSamples,
                                currentFreqRange,
                                currentIsDrumsMode,
                                currentSamplesPer16th,
                                currentEditMode
                            )
                        },
                        onDrag = { change, _ ->
                            dragInfo?.let { info ->
                                val newX = (change.position.x).coerceIn(0f, size.width.toFloat())
                                val newNoteOnTimestamp = (newX / size.width * currentTotalSamples).roundToLong()

                                val quantizedNoteOn = (newNoteOnTimestamp / currentSamplesPer16th.toLong()) * currentSamplesPer16th.toLong()

                                if (info.isStretching) {
                                    viewModel.updateEventTimestamp(info.targetIndex, quantizedNoteOn)
                                } else {
                                    val delta = quantizedNoteOn - info.initialNoteOnTimestamp
                                    val newNoteOff = if (info.noteOffIndex != -1) info.initialNoteOffTimestamp + delta else -1L

                                    viewModel.moveNote(
                                        info.noteOnIndex,
                                        info.noteOffIndex,
                                        quantizedNoteOn,
                                        newNoteOff
                                    )
                                }
                            }
                        },
                        onDragEnd = { dragInfo = null },
                        onDragCancel = { dragInfo = null }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            for (i in 0..64) {
                val x = (i / 64f) * width
                val color = when {
                    i % 16 == 0 -> Color.Gray // Начало такта
                    i % 4 == 0 -> Color.DarkGray // Доля
                    else -> Color(0xFF1A1A1A) // 1/16
                }
                drawLine(color, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
            }

            filteredEventsWithIndices.forEach { (originalIndex, event) ->
                if (event.isNoteOn) {
                    val x = (event.timestamp.toFloat() / totalSamples) * width

                    val nextEvent = allEvents.drop(originalIndex + 1).find {
                        it.frequency == event.frequency && !it.isNoteOn && it.trackId == event.trackId
                    }
                    val duration = if (nextEvent != null) nextEvent.timestamp - event.timestamp else samplesPer16th.toLong()
                    val noteWidth = (duration.toFloat() / totalSamples) * width

                    val y = if (event.isDrum) {
                        when (event.frequency.toInt()) {
                            0 -> height * 0.8f // Kick
                            1 -> height * 0.5f // Snare
                            2 -> height * 0.2f // Hat
                            else -> height * 0.5f
                        }
                    } else {
                        val pos = (event.frequency - freqRange.start) / (freqRange.endInclusive - freqRange.start)
                        height - pos.coerceIn(0f, 1f) * height
                    }

                    drawRect(
                        color = if (event.isDrum) Color(0xFFE91E63) else Color(0xFF2196F3),
                        topLeft = Offset(x, y - 8f), // Увеличили толщину
                        size = Size(maxOf(noteWidth, 8f), 16f)
                    )
                }
            }
        }

        // Компактные кнопки управления в углу сетки
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Режим Drag
            IconButton(
                onClick = { viewModel.setGridEditMode(WavetableSynthesizerViewModel.GridEditMode.DRAG) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.OpenWith,
                    contentDescription = "Drag",
                    tint = if (editMode == WavetableSynthesizerViewModel.GridEditMode.DRAG) Color(0xFF2196F3) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Режим Size
            IconButton(
                onClick = { viewModel.setGridEditMode(WavetableSynthesizerViewModel.GridEditMode.STRETCH) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Straighten,
                    contentDescription = "Size",
                    tint = if (editMode == WavetableSynthesizerViewModel.GridEditMode.STRETCH) Color(0xFF2196F3) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Кнопка Квантования
            IconButton(
                onClick = { viewModel.quantizeActivePattern() },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.AutoFixHigh,
                    contentDescription = "Quantize",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        if (filteredEventsWithIndices.isEmpty()) {
            Text("No notes. REC to start.", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.Center))
        }
    }
}

private data class DragInfo(
    val noteOnIndex: Int,
    val noteOffIndex: Int,
    val initialNoteOnTimestamp: Long,
    val initialNoteOffTimestamp: Long,
    val targetIndex: Int, // Либо NoteOn, либо NoteOff для растягивания
    val isStretching: Boolean
)

private fun findEventAt(
    offset: Offset, 
    indexedEvents: List<Pair<Int, MidiEventData>>, 
    allEvents: List<MidiEventData>,
    size: androidx.compose.ui.unit.IntSize,
    totalSamples: Float,
    freqRange: ClosedFloatingPointRange<Float>,
    isDrumsMode: Boolean,
    samplesPer16th: Float,
    editMode: WavetableSynthesizerViewModel.GridEditMode
): DragInfo? {
    val width = size.width.toFloat()
    val height = size.height.toFloat()
    
    indexedEvents.forEach { (index, event) ->
        if (event.isNoteOn) {
            val x = (event.timestamp.toFloat() / totalSamples) * width

            var noteOffIndex = -1
            var noteOffTimestamp = -1L
            for (i in index + 1 until allEvents.size) {
                val e = allEvents[i]
                if (e.frequency == event.frequency && !e.isNoteOn && e.trackId == event.trackId) {
                    noteOffIndex = i
                    noteOffTimestamp = e.timestamp
                    break
                }
            }
            
            val duration = if (noteOffIndex != -1) {
                noteOffTimestamp - event.timestamp
            } else {
                samplesPer16th.toLong()
            }
            
            val noteWidth = (duration.toFloat() / totalSamples) * width
            val xEnd = x + noteWidth
            
            val y = if (isDrumsMode) {
                when (event.frequency.toInt()) {
                    0 -> height * 0.8f // Kick
                    1 -> height * 0.5f // Snare
                    2 -> height * 0.2f // Hat
                    else -> height * 0.5f
                }
            } else {
                val pos = (event.frequency - freqRange.start) / (freqRange.endInclusive - freqRange.start)
                height - pos.coerceIn(0f, 1f) * height
            }
            
            if (offset.y in (y - 40f)..(y + 40f)) {
                if (editMode == WavetableSynthesizerViewModel.GridEditMode.DRAG) {
                    if (offset.x in (x - 30f)..(xEnd + 30f)) {
                        return DragInfo(index, noteOffIndex, event.timestamp, noteOffTimestamp, index, false)
                    }
                } else if (editMode == WavetableSynthesizerViewModel.GridEditMode.STRETCH) {
                    if (noteOffIndex != -1) {
                        if (offset.x in (x - 30f)..(xEnd + 30f)) {
                            return DragInfo(index, noteOffIndex, event.timestamp, noteOffTimestamp, noteOffIndex, true)
                        }
                    } else {
                        if (offset.x in (x - 30f)..(xEnd + 30f)) {
                            return DragInfo(index, noteOffIndex, event.timestamp, noteOffTimestamp, index, false)
                        }
                    }
                }
            }
        }
    }
    return null
}
