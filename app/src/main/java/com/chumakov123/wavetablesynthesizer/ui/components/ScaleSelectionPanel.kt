package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.MusicalKey
import com.chumakov123.wavetablesynthesizer.Scale
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun ScaleSelectionPanel(
    synthesizerViewModel: WavetableSynthesizerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedScale by synthesizerViewModel.selectedScale.observeAsState(Scale.CHROMATIC)
    val selectedKey by synthesizerViewModel.selectedKey.observeAsState(MusicalKey.C)
    
    val buttonHeight = 24.dp

    Column(
        modifier = modifier.padding(vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Key selection
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(MusicalKey.entries) { key ->
                val isSelected = key == selectedKey
                Box(
                    modifier = Modifier
                        .height(buttonHeight)
                        .background(
                            if (isSelected) Color.Gray else Color(0xFF333333),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { synthesizerViewModel.setSelectedKey(key) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key.label,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Scale selection
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(Scale.entries) { scale ->
                val isSelected = scale == selectedScale
                Box(
                    modifier = Modifier
                        .height(buttonHeight)
                        .background(
                            if (isSelected) Color.Gray else Color(0xFF333333),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { synthesizerViewModel.setScale(scale) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = scale.label,
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}
