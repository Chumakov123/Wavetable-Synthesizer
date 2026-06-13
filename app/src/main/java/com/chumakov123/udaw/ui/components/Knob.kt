package com.chumakov123.udaw.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI

@Composable
fun Knob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    knobColor: Color = MaterialTheme.colorScheme.primary,
    indicatorColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentValue by rememberUpdatedState(value)
    
    var dragAccumulator by remember {
        mutableFloatStateOf((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)) 
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(valueRange) {
                    detectDragGestures(
                        onDragStart = {
                            dragAccumulator = (currentValue - valueRange.start) / (valueRange.endInclusive - valueRange.start)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            
                            val sensitivity = 1f / 350f
                            val delta = -dragAmount.y * sensitivity
                            
                            dragAccumulator = (dragAccumulator + delta).coerceIn(0f, 1f)
                            
                            val newValue = valueRange.start + dragAccumulator * (valueRange.endInclusive - valueRange.start)
                            currentOnValueChange(newValue)
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2
            val strokeWidth = radius * 0.1f

            drawArc(
                color = knobColor.copy(alpha = 0.3f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val normalizedValue = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
            drawArc(
                color = knobColor,
                startAngle = 135f,
                sweepAngle = normalizedValue * 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            drawCircle(
                color = knobColor,
                radius = radius * 0.8f,
                center = center
            )

            val indicatorAngleRad = (normalizedValue * 270f + 135f) * PI.toFloat() / 180f
            val indicatorStart = Offset(
                center.x + (radius * 0.4f) * kotlin.math.cos(indicatorAngleRad),
                center.y + (radius * 0.4f) * kotlin.math.sin(indicatorAngleRad)
            )
            val indicatorEnd = Offset(
                center.x + (radius * 0.7f) * kotlin.math.cos(indicatorAngleRad),
                center.y + (radius * 0.7f) * kotlin.math.sin(indicatorAngleRad)
            )

            drawLine(
                color = indicatorColor,
                start = indicatorStart,
                end = indicatorEnd,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
