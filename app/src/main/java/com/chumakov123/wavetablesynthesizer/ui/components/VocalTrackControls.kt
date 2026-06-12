package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun VocalTrackControls(viewModel: WavetableSynthesizerViewModel) {
    val isEnabled by viewModel.isVocalTrackEnabled.observeAsState(true)
    val offset by viewModel.vocalTrackOffset.observeAsState(0f)
    val volume by viewModel.vocalTrackVolume.observeAsState(0f)
    val path by viewModel.vocalTrackPath.observeAsState(null)

    if (path == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF333333), RoundedCornerShape(4.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("VOCALS", fontSize = 10.sp, color = Color.Yellow, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ON", fontSize = 8.sp, color = Color.Gray)
            Switch(
                checked = isEnabled,
                onCheckedChange = { viewModel.setVocalTrackEnabled(it) },
                modifier = Modifier.scale(0.5f)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("OFFSET: ${"%.2f".format(offset)}s", fontSize = 8.sp, color = Color.LightGray)
                Text("VOL: ${volume.toInt()}dB", fontSize = 8.sp, color = Color.LightGray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Knob(
                    value = offset,
                    onValueChange = { viewModel.setVocalTrackOffset(it) },
                    valueRange = -1f..1f,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Knob(
                    value = volume,
                    onValueChange = { viewModel.setVocalTrackVolume(it) },
                    valueRange = -60f..12f,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
