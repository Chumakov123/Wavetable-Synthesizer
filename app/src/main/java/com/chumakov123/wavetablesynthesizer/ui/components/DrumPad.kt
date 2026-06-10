package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun DrumPad(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF444444)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            onClick()
        }
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .fillMaxHeight()
            .background(
                if (isPressed) color.copy(alpha = 0.7f) else color,
                RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Click is handled by LaunchedEffect(isPressed) */ },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun DrumSection(viewModel: WavetableSynthesizerViewModel) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        DrumPad(
            label = "KICK",
            onClick = { viewModel.triggerKick() },
            modifier = Modifier.weight(1f),
            color = Color(0xFF6200EE)
        )
        DrumPad(
            label = "SNARE",
            onClick = { viewModel.triggerSnare() },
            modifier = Modifier.weight(1f),
            color = Color(0xFFE91E63)
        )
        DrumPad(
            label = "HAT",
            onClick = { viewModel.triggerHat() },
            modifier = Modifier.weight(1f),
            color = Color(0xFFFFEB3B)
        )
        DrumPad(label = "FX", onClick = { }, modifier = Modifier.weight(1f))
    }
}
