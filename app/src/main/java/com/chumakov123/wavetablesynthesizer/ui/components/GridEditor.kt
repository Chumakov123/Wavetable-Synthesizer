package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
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
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridEditor(viewModel: WavetableSynthesizerViewModel) {
    val allEvents by viewModel.patternEvents.observeAsState(emptyList())
    val bpm by viewModel.bpm.observeAsState(120f)
    val isDrumsMode by viewModel.isDrumsMode.observeAsState(false)
    val selectedTrack by viewModel.selectedTrack.observeAsState(0)
    val octave by viewModel.octave.observeAsState(0)
    val editMode by viewModel.gridEditMode.observeAsState(WavetableSynthesizerViewModel.GridEditMode.DRAG)

    val filteredEventsWithIndices = allEvents.mapIndexed { index, event -> index to event }
        .filter { (_, event) ->
            if (isDrumsMode) event.isDrum
            else !event.isDrum && event.trackId == selectedTrack
        }

    val freqRange = remember(filteredEventsWithIndices, octave, isDrumsMode) {
        if (isDrumsMode) {
            -0.5f..2.5f // 3 барабана: 0, 1, 2
        } else {
            val multiplier = 2f.pow(octave)
            val centerFreq = 440f * multiplier
            (centerFreq / 2f)..(centerFreq * 2f)
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
                            val info = findEventAt(
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
                            if (info != null) {
                                viewModel.deleteEvent(info.noteOnIndex)
                            }
                        },
                        onTap = { offset ->
                            if (currentEditMode == WavetableSynthesizerViewModel.GridEditMode.PAINT) {
                                val info = findEventAt(
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
                                if (info != null) {
                                    viewModel.deleteEvent(info.noteOnIndex)
                                } else {
                                    val timestamp = (offset.x / size.width * currentTotalSamples).roundToLong()
                                    val quantizedTimestamp = (timestamp / currentSamplesPer16th.toLong()) * currentSamplesPer16th.toLong()
                                    val frequency = getFrequencyFromY(offset.y, size, currentFreqRange, currentIsDrumsMode)
                                    val duration = currentSamplesPer16th.toLong() * 2 // 1/8 note
                                    viewModel.addNote(quantizedTimestamp, frequency, duration)

                                    if (currentIsDrumsMode) {
                                        when (frequency.toInt()) {
                                            0 -> viewModel.triggerKick()
                                            1 -> viewModel.triggerSnare()
                                            2 -> viewModel.triggerHat()
                                        }
                                    } else {
                                        viewModel.playPreviewNote(frequency)
                                    }
                                }
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    var dragInfo: DragInfo? = null
                    var lastPlayedFreq = -1f
                    
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
                            
                            if (currentEditMode == WavetableSynthesizerViewModel.GridEditMode.PAINT && dragInfo == null) {
                                val timestamp = (offset.x / size.width * currentTotalSamples).roundToLong()
                                val quantizedTimestamp = (timestamp / currentSamplesPer16th.toLong()) * currentSamplesPer16th.toLong()
                                val frequency = getFrequencyFromY(offset.y, size, currentFreqRange, currentIsDrumsMode)
                                
                                val duration = currentSamplesPer16th.toLong() * 2 // 1/8 note
                                viewModel.addNote(quantizedTimestamp, frequency, duration)

                                if (currentIsDrumsMode) {
                                    when (frequency.toInt()) {
                                        0 -> viewModel.triggerKick()
                                        1 -> viewModel.triggerSnare()
                                        2 -> viewModel.triggerHat()
                                    }
                                } else {
                                    viewModel.noteOn(frequency)
                                }
                                lastPlayedFreq = frequency
                                
                                dragInfo = DragInfo(
                                    noteOnIndex = -1,
                                    noteOffIndex = -1,
                                    initialNoteOnTimestamp = quantizedTimestamp,
                                    initialNoteOffTimestamp = quantizedTimestamp + duration,
                                    targetIndex = -1,
                                    isStretching = false,
                                    isNewNote = true,
                                    initialFrequency = frequency
                                )
                            }
                        },
                        onDrag = { change, _ ->
                            dragInfo?.let { info ->
                                val newX = (change.position.x).coerceIn(0f, size.width.toFloat())
                                val newNoteOnTimestamp = (newX / size.width * currentTotalSamples).roundToLong()
                                val quantizedNoteOn = (newNoteOnTimestamp / currentSamplesPer16th.toLong()) * currentSamplesPer16th.toLong()
                                
                                val newFreq = getFrequencyFromY(change.position.y, size, currentFreqRange, currentIsDrumsMode)

                                var activeInfo = info
                                if (info.isNewNote) {
                                    val found = currentFilteredWithIndices.find { (_, event) ->
                                        event.isNoteOn && 
                                        event.frequency == info.initialFrequency && 
                                        event.timestamp == info.initialNoteOnTimestamp
                                    }
                                    if (found != null) {
                                        var offIdx = -1
                                        for (i in found.first + 1 until currentEvents.size) {
                                            val e = currentEvents[i]
                                            if (e.frequency == found.second.frequency && !e.isNoteOn) {
                                                offIdx = i
                                                break
                                            }
                                        }
                                        activeInfo = info.copy(
                                            isNewNote = false, 
                                            noteOnIndex = found.first, 
                                            noteOffIndex = offIdx,
                                            targetIndex = found.first
                                        )
                                        dragInfo = activeInfo
                                    }
                                }

                                if (!activeInfo.isNewNote) {
                                    if (activeInfo.isStretching) {
                                        viewModel.updateEventTimestamp(activeInfo.targetIndex, quantizedNoteOn)
                                    } else {
                                        val delta = quantizedNoteOn - activeInfo.initialNoteOnTimestamp
                                        val newNoteOff = if (activeInfo.noteOffIndex != -1) activeInfo.initialNoteOffTimestamp + delta else -1L
                                        
                                        viewModel.moveNote(
                                            activeInfo.noteOnIndex,
                                            activeInfo.noteOffIndex,
                                            quantizedNoteOn,
                                            newNoteOff
                                        )
                                        
                                        if (currentEditMode == WavetableSynthesizerViewModel.GridEditMode.PAINT || currentEditMode == WavetableSynthesizerViewModel.GridEditMode.DRAG) {
                                            viewModel.updateEventFrequency(activeInfo.noteOnIndex, newFreq)
                                            if (activeInfo.noteOffIndex != -1) viewModel.updateEventFrequency(activeInfo.noteOffIndex, newFreq)
                                            
                                            if (newFreq != lastPlayedFreq) {
                                                if (currentIsDrumsMode) {
                                                    when (newFreq.toInt()) {
                                                        0 -> viewModel.triggerKick()
                                                        1 -> viewModel.triggerSnare()
                                                        2 -> viewModel.triggerHat()
                                                    }
                                                } else {
                                                    viewModel.noteOff(lastPlayedFreq)
                                                    viewModel.noteOn(newFreq)
                                                }
                                                lastPlayedFreq = newFreq
                                            }
                                        }
                                    }
                                } else {
                                    if (newFreq != lastPlayedFreq) {
                                        if (currentIsDrumsMode) {
                                            when (newFreq.toInt()) {
                                                0 -> viewModel.triggerKick()
                                                1 -> viewModel.triggerSnare()
                                                2 -> viewModel.triggerHat()
                                            }
                                        } else {
                                            viewModel.noteOff(lastPlayedFreq)
                                            viewModel.noteOn(newFreq)
                                        }
                                        lastPlayedFreq = newFreq
                                    }
                                }
                            }
                        },
                        onDragEnd = { 
                            dragInfo = null
                            if (lastPlayedFreq != -1f) {
                                viewModel.noteOff(lastPlayedFreq)
                                lastPlayedFreq = -1f
                            }
                        },
                        onDragCancel = { 
                            dragInfo = null
                            if (lastPlayedFreq != -1f) {
                                viewModel.noteOff(lastPlayedFreq)
                                lastPlayedFreq = -1f
                            }
                        }
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
            // Режим Paint
            IconButton(
                onClick = { viewModel.setGridEditMode(WavetableSynthesizerViewModel.GridEditMode.PAINT) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Brush,
                    contentDescription = "Paint",
                    tint = if (editMode == WavetableSynthesizerViewModel.GridEditMode.PAINT) Color(0xFFE91E63) else Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            
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

        if (filteredEventsWithIndices.isEmpty() && editMode != WavetableSynthesizerViewModel.GridEditMode.PAINT) {
            Text("No notes. Use PAINT mode to add.", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.align(Alignment.Center))
        }
    }
}

private data class DragInfo(
    val noteOnIndex: Int,
    val noteOffIndex: Int,
    val initialNoteOnTimestamp: Long,
    val initialNoteOffTimestamp: Long,
    val targetIndex: Int, // Либо NoteOn, либо NoteOff для растягивания
    val isStretching: Boolean,
    val isNewNote: Boolean = false,
    val initialFrequency: Float = 0f
)

private fun snapToMusicalNote(frequency: Float): Float {
    if (frequency <= 0) return frequency
    // n = 69 + 12 * log2(f / 440)
    val midiNote = 69 + 12 * log2((frequency / 440f).toDouble())
    val snappedMidiNote = midiNote.roundToInt()
    return (440f * 2.0.pow((snappedMidiNote - 69) / 12.0)).toFloat()
}

private fun getFrequencyFromY(y: Float, size: androidx.compose.ui.unit.IntSize, range: ClosedFloatingPointRange<Float>, isDrumsMode: Boolean): Float {
    val height = size.height.toFloat()
    return if (isDrumsMode) {
        when {
            y > height * 0.65f -> 0f // Kick
            y > height * 0.35f -> 1f // Snare
            else -> 2f // Hat
        }
    } else {
        val pos = (height - y) / height
        val freq = range.start + pos.coerceIn(0f, 1f) * (range.endInclusive - range.start)
        snapToMusicalNote(freq)
    }
}

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
                if (editMode == WavetableSynthesizerViewModel.GridEditMode.DRAG || editMode == WavetableSynthesizerViewModel.GridEditMode.PAINT) {
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
