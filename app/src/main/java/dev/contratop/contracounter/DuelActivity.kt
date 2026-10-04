package dev.contratop.contracounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import dev.contratop.contracounter.ui.CounterViewModel
import dev.contratop.contracounter.ui.DuelScreen
import dev.contratop.contracounter.ui.theme.ContraCounterTheme

/**
 * Actividad dedicada para el Modo Duelo (2 Jugadores cara a cara).
 * Ocupa la pantalla completa con la mitad superior rotada 180° para el rival en la mesa.
 */
class DuelActivity : ComponentActivity() {

    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            val colorTheme by viewModel.colorTheme.collectAsState()
            val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
            val isDark = isSystemInDarkTheme()

            ContraCounterTheme(
                colorTheme = colorTheme,
                darkTheme = isDark,
                hapticsEnabled = hapticsEnabled
            ) {
                DuelScreen(
                    hapticsEnabled = hapticsEnabled,
                    onBack = { finish() }
                )
            }
        }
    }
}
