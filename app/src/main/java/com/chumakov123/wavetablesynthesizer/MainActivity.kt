package com.chumakov123.wavetablesynthesizer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.chumakov123.wavetablesynthesizer.ui.WavetableSynthesizerApp
import com.chumakov123.wavetablesynthesizer.ui.theme.WavetableSynthesizerTheme

class MainActivity : ComponentActivity() {
    private val synthesizerViewModel: WavetableSynthesizerViewModel by viewModels()
    private val synthesizer = NativeWavetableSynthesizer()

    private val projectsDirLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            synthesizerViewModel.setProjectsFolderUri(it.toString(), this)
        }
    }

    fun requestProjectsFolder() {
        projectsDirLauncher.launch(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        synthesizerViewModel.initStorage(this)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        lifecycle.addObserver(synthesizer)

        synthesizerViewModel.wavetableSynthesizer = synthesizer

        // Trigger SAF if needed
        if (synthesizerViewModel.projectsFolderUri.value == null) {
            // We could auto-trigger here if internal projects exist, 
            // but usually it's better to wait for a user action or show a specific prompt.
        }

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

    override fun onDestroy() {
        super.onDestroy()

        lifecycle.removeObserver(synthesizer)
    }

    override fun onResume() {
        super.onResume()
        synthesizerViewModel.applyParameters()
    }
}
