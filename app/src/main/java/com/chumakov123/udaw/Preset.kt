package com.chumakov123.udaw

data class Preset(
    val name: String,
    val wavetable: Wavetable = Wavetable.SINE,
    val attack: Float = 0.01f,
    val decay: Float = 0.1f,
    val sustain: Float = 0.7f,
    val release: Float = 0.3f,
    val lfoRate: Float = 5.0f,
    val lfoDepth: Float = 0.0f,
    val tremoloDepth: Float = 0.0f
) {
    companion object {
        val defaultPresets = listOf(
            Preset(
                name = "Sine Soft",
                wavetable = Wavetable.SINE,
                attack = 0.1f,
                decay = 0.2f,
                sustain = 0.8f,
                release = 0.5f
            ),
            Preset(
                name = "Saw Lead",
                wavetable = Wavetable.SAW,
                attack = 0.01f,
                decay = 0.1f,
                sustain = 0.6f,
                release = 0.2f,
                lfoRate = 6.0f,
                lfoDepth = 0.05f
            ),
            Preset(
                name = "Square Bass",
                wavetable = Wavetable.SQUARE,
                attack = 0.005f,
                decay = 0.3f,
                sustain = 0.4f,
                release = 0.1f
            ),
            Preset(
                name = "Tremolo Pad",
                wavetable = Wavetable.TRIANGLE,
                attack = 0.5f,
                decay = 0.5f,
                sustain = 1.0f,
                release = 1.0f,
                lfoRate = 4.0f,
                tremoloDepth = 0.3f
            )
        )
    }
}
