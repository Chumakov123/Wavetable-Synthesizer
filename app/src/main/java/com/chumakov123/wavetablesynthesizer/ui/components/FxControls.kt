package com.chumakov123.wavetablesynthesizer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel

@Composable
fun FxControls(viewModel: WavetableSynthesizerViewModel) {
    val delayTime by viewModel.delayTime.observeAsState(0.5f)
    val delayFeedback by viewModel.delayFeedback.observeAsState(0.5f)
    val delayWet by viewModel.delayWet.observeAsState(0.0f)

    Row(
        modifier = Modifier
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FxKnob(
            label = "TIME",
            value = delayTime,
            onValueChange = { viewModel.setDelayTime(it) },
            valueRange = 0.01f..2.0f,
            unit = "s"
        )
        FxKnob(
            label = "FEED",
            value = delayFeedback,
            onValueChange = { viewModel.setDelayFeedback(it) },
            valueRange = 0.0f..0.95f
        )
        FxKnob(
            label = "WET",
            value = delayWet,
            onValueChange = { viewModel.setDelayWet(it) },
            valueRange = 0.0f..1.0f
        )
    }
}

@Composable
private fun FxKnob(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = label, fontSize = 10.sp)
        Knob(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.size(40.dp).padding(2.dp),
            valueRange = valueRange
        )
        Text(
            text = "%.1f%s".format(value, unit),
            fontSize = 9.sp
        )
    }
}
