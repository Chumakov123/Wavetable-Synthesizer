package com.chumakov123.wavetablesynthesizer

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.chumakov123.wavetablesynthesizer.ui.theme.WavetableSynthesizerTheme

class MainActivity : ComponentActivity() {
    private val synthesizerViewModel: WavetableSynthesizerViewModel by viewModels()
    private val synthesizer = LoggingWavetableSynthesizer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        synthesizerViewModel.wavetableSynthesizer = synthesizer

        setContent {
            WavetableSynthesizerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WavetableSynthesizerApp(
                        modifier = Modifier.padding(innerPadding),
                        synthesizerViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        synthesizerViewModel.applyParameters()
    }
}

@Composable
fun WavetableSynthesizerApp(
    modifier : Modifier,
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

 @Composable
 fun WavetableSelectionPanel(
     synthesizerViewModel: WavetableSynthesizerViewModel
 ) {
     Column(
         modifier = Modifier
             .fillMaxWidth()
             .fillMaxHeight(0.5f),
         verticalArrangement = Arrangement.SpaceEvenly,
         horizontalAlignment = Alignment.CenterHorizontally
     ) {
         Text(stringResource(R.string.wavetable))
         WavetableSelectionButtons(synthesizerViewModel)
     }
 }

 @Composable
 fun WavetableSelectionButtons(
     synthesizerViewModel: WavetableSynthesizerViewModel
 ) {
     Row(
         modifier = Modifier.fillMaxWidth(),
         horizontalArrangement = Arrangement.SpaceEvenly
     ) {
         for (wavetable in Wavetable.entries) {
             WavetableButton(
                 label = stringResource(wavetable.toResourceString()),
                 onClick = {
                     synthesizerViewModel.setWavetable(wavetable)
                 })
         }
     }
 }

 @Composable
 fun WavetableButton(
     label: String,
     onClick: () -> Unit
 ) {
     Button(
         onClick = onClick,
         modifier = Modifier.padding(4.dp)
     ) {
         Text(label)
     }
 }

 @Composable
 fun ControlsPanel(
     synthesizerViewModel: WavetableSynthesizerViewModel
 ) {
     Row(
         modifier = Modifier
             .fillMaxWidth()
             .fillMaxHeight(),
         horizontalArrangement = Arrangement.Center,
         verticalAlignment = Alignment.CenterVertically
     ) {
         Column(
             modifier = Modifier
                 .fillMaxHeight()
                 .fillMaxWidth(0.6f),
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             PitchControl(synthesizerViewModel)
             PlayControl(synthesizerViewModel)
         }
         Column(
             modifier = Modifier.fillMaxSize(),
             verticalArrangement = Arrangement.SpaceEvenly,
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             VolumeControl(synthesizerViewModel)
         }
     }
 }

 @Composable
 fun PitchControl(
     synthesizerViewModel: WavetableSynthesizerViewModel
 ) {
     //val frequency = rememberSaveable{ mutableFloatStateOf(300f) } //Сохраняется даже после перекомпоновки
     val frequency = synthesizerViewModel.frequency.observeAsState()
     PitchControlContent(
         pitchControlLabel = stringResource(R.string.frequency), //Для локализации
         value = synthesizerViewModel.sliderPositionFromFrequencyInHz(frequency.value!!),
         onValueChange = {
             synthesizerViewModel.setFrequencySliderPosition(it)
         },
         valueRange = 0f..1f,
         frequencyValueLabel = stringResource(R.string.frequency_value, frequency.value!!)
     )
 }
// state hoisting
 @Composable
 fun PitchControlContent(
     pitchControlLabel: String,
     value: Float,
     onValueChange: (Float) -> Unit,
     valueRange: ClosedFloatingPointRange<Float>,
     frequencyValueLabel: String
 ) {
     Text(
         text = pitchControlLabel,
     )
     Slider(
         value = value,
         onValueChange = onValueChange,
         valueRange = valueRange,
     )
     Text(
         text = frequencyValueLabel
     )
 }

 @Composable
 fun PlayControl(
     synthesizerViewModel: WavetableSynthesizerViewModel
 ) {
     val playButtonLabel = synthesizerViewModel.playButtonLabel.observeAsState()
     Button(onClick = {synthesizerViewModel.playClicked()}) {
         Text(stringResource(playButtonLabel.value!!))
     }
 }

@Composable
fun VolumeControl(
    synthesizerViewModel: WavetableSynthesizerViewModel
) {
    val volume = synthesizerViewModel.volume.observeAsState()

    VolumeControlContent(
        value = volume.value!!,
        onValueChange = {
            synthesizerViewModel.setVolume(it)
        },
        valueRange = synthesizerViewModel.volumeRange
    )
}

@Composable
fun VolumeControlContent(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp
    val sliderHeight = screenHeight / 4

    Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.width(sliderHeight.dp).rotate(270f).padding(8.dp),
        valueRange = valueRange
    )
    Icon(imageVector = Icons.AutoMirrored.Filled.VolumeMute, contentDescription = null)
}