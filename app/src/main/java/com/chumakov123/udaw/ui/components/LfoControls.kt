package com.chumakov123.udaw.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.udaw.MainViewModel
import java.util.Locale

@Composable
fun LfoControls(
    synthesizerViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val rate = synthesizerViewModel.lfoRate.observeAsState(5.0f)
    val vibratoDepth = synthesizerViewModel.lfoDepth.observeAsState(0.0f)
    val tremoloDepth = synthesizerViewModel.tremoloDepth.observeAsState(0.0f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LfoKnob(
            label = "RATE",
            value = rate.value,
            onValueChange = { synthesizerViewModel.setLfoRate(it) },
            valueRange = 0.1f..20.0f,
            unit = "Hz"
        )
        LfoKnob(
            label = "VIBRATO",
            value = vibratoDepth.value,
            onValueChange = { synthesizerViewModel.setLfoDepth(it) },
            valueRange = 0.0f..0.5f,
            unit = ""
        )
        LfoKnob(
            label = "TREMOLO",
            value = tremoloDepth.value,
            onValueChange = { synthesizerViewModel.setTremoloDepth(it) },
            valueRange = 0.0f..0.5f,
            unit = ""
        )
    }
}

@Composable
private fun LfoKnob(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, fontSize = 10.sp)
        Knob(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.size(50.dp).padding(4.dp),
            valueRange = valueRange
        )
        Text(
            text = String.format(Locale.US, "%.1f%s", value, unit),
            fontSize = 9.sp
        )
    }
}
