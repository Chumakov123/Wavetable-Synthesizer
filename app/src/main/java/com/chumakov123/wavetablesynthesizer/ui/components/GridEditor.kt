package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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

@Composable
fun GridEditor(viewModel: WavetableSynthesizerViewModel) {
    val allEvents by viewModel.patternEvents.observeAsState(emptyList())
    val bpm by viewModel.bpm.observeAsState(120f)
    val isDrumsMode by viewModel.isDrumsMode.observeAsState(false)
    val selectedTrack by viewModel.selectedTrack.observeAsState(0)

    // Фильтруем события для текущего инструмента
    val filteredEventsWithIndices = allEvents.mapIndexed { index, event -> index to event }
        .filter { (_, event) ->
            if (isDrumsMode) event.isDrum
            else !event.isDrum && event.trackId == selectedTrack
        }

    val events = filteredEventsWithIndices.map { it.second }
    
    // Константы для отрисовки
    val sampleRate = 48000f
    val samplesPerBeat = sampleRate * 60f / bpm
    val samplesPer16th = samplesPerBeat / 4f
    val totalSamples = samplesPerBeat * 4f * 4f // 4 такта
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Column {
            Text(
                text = if (isDrumsMode) "DRUMS GRID" else "TRACK ${selectedTrack + 1} GRID",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Box(modifier = Modifier.weight(1f)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredEventsWithIndices, bpm) {
                            detectTapGestures(
                                onLongPress = { offset ->
                                    val clickedEventPair = findEventAt(offset, filteredEventsWithIndices, size, totalSamples)
                                    if (clickedEventPair != null) {
                                        viewModel.deleteEvent(clickedEventPair.first)
                                    }
                                }
                            )
                        }
                        .pointerInput(filteredEventsWithIndices, bpm) {
                            var draggedEventPair: Pair<Int, MidiEventData>? = null
                            detectDragGestures(
                                onDragStart = { offset ->
                                    draggedEventPair = findEventAt(offset, filteredEventsWithIndices, size, totalSamples)
                                },
                                onDrag = { change, _ ->
                                    draggedEventPair?.let { (originalIndex, _) ->
                                        val newX = (change.position.x).coerceIn(0f, size.width.toFloat())
                                        val newTimestamp = (newX / size.width * totalSamples).roundToLong()
                                        
                                        // Квантование при перетаскивании
                                        val quantizedTimestamp = (newTimestamp / samplesPer16th.toLong()) * samplesPer16th.toLong()
                                        
                                        viewModel.updateEventTimestamp(originalIndex, quantizedTimestamp)
                                    }
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // 1. Рисуем сетку (вертикальные линии 1/16)
                    for (i in 0..64) {
                        val x = (i / 64f) * width
                        val color = when {
                            i % 16 == 0 -> Color.Gray // Начало такта
                            i % 4 == 0 -> Color.DarkGray // Доля
                            else -> Color(0xFF222222) // 1/16
                        }
                        drawLine(color, Offset(x, 0f), Offset(x, height), strokeWidth = 1f)
                    }
                    
                    // 2. Рисуем "ноты"
                    filteredEventsWithIndices.forEach { (originalIndex, event) ->
                        if (event.isNoteOn) {
                            val x = (event.timestamp.toFloat() / totalSamples) * width
                            
                            // Поиск соответствующего Note Off для определения длины
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
                                height - (event.frequency / 2000f).coerceIn(0f, 1f) * height
                            }
                            
                            drawRect(
                                color = if (event.isDrum) Color(0xFFE91E63) else Color(0xFF2196F3),
                                topLeft = Offset(x, y - 5f),
                                size = Size(maxOf(noteWidth, 5f), 10f)
                            )
                        }
                    }
                }
                
                if (filteredEventsWithIndices.isEmpty()) {
                    Text("No notes for this instrument.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

private fun findEventAt(
    offset: Offset, 
    indexedEvents: List<Pair<Int, MidiEventData>>, 
    size: androidx.compose.ui.unit.IntSize, 
    totalSamples: Float
): Pair<Int, MidiEventData>? {
    val width = size.width.toFloat()
    val height = size.height.toFloat()
    
    indexedEvents.forEach { (index, event) ->
        if (event.isNoteOn) {
            val x = (event.timestamp.toFloat() / totalSamples) * width
            val y = if (event.isDrum) {
                when (event.frequency.toInt()) {
                    0 -> height * 0.8f
                    1 -> height * 0.5f
                    2 -> height * 0.2f
                    else -> height * 0.5f
                }
            } else {
                height - (event.frequency / 2000f).coerceIn(0f, 1f) * height
            }
            
            if (offset.x in (x - 20f)..(x + 40f) && offset.y in (y - 30f)..(y + 30f)) {
                return index to event
            }
        }
    }
    return null
}
