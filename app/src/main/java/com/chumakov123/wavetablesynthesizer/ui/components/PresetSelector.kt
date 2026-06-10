package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun PresetSelector(
    synthesizerViewModel: WavetableSynthesizerViewModel,
    modifier: Modifier = Modifier
) {
    val presets by synthesizerViewModel.presets.observeAsState(emptyList())
    val selectedIndex by synthesizerViewModel.selectedPresetIndex.observeAsState(0)
    
    val currentPresetName = presets.getOrNull(selectedIndex)?.name ?: "Manual"

    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { 
                val newIndex = if (selectedIndex > 0) selectedIndex - 1 else presets.size - 1
                synthesizerViewModel.loadPreset(newIndex)
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous Preset",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = currentPresetName.uppercase(),
            color = Color(0xFFFFB74D),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        IconButton(
            onClick = { 
                val newIndex = (selectedIndex + 1) % presets.size
                synthesizerViewModel.loadPreset(newIndex)
            },
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next Preset",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
