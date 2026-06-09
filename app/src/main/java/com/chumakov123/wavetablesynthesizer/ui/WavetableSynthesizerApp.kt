package com.chumakov123.wavetablesynthesizer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.wavetablesynthesizer.WavetableSynthesizerViewModel
import com.chumakov123.wavetablesynthesizer.ui.components.ControlsPanel
import com.chumakov123.wavetablesynthesizer.ui.components.WavetableSelectionPanel

@Composable
fun WavetableSynthesizerApp(
    modifier: Modifier,
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        WavetableSelectionPanel(synthesizerViewModel)
        ControlsPanel(synthesizerViewModel)
    }
}
