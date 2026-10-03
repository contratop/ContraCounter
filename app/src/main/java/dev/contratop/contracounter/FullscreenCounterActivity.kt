package dev.contratop.contracounter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import dev.contratop.contracounter.ui.CounterViewModel
import dev.contratop.contracounter.ui.FullscreenCounterScreen
import dev.contratop.contracounter.ui.theme.ContraCounterTheme

/**
 * Actividad dedicada para el contador a pantalla completa.
 * Al ser una Activity independiente, Android OS ejecuta la transición nativa del sistema
 * (apertura de ventana, gesto predictive back de Android 14/15 en Pixel, etc.).
 */
class FullscreenCounterActivity : ComponentActivity() {

    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val counterId = intent.getStringExtra(EXTRA_COUNTER_ID) ?: run {
            finish()
            return
        }

        setContent {
            val counters by viewModel.counters.collectAsState()
            val colorTheme by viewModel.colorTheme.collectAsState()
            val isDark = isSystemInDarkTheme()

            ContraCounterTheme(
                colorTheme = colorTheme,
                darkTheme = isDark
            ) {
                val activeCounter = counters.find { it.id == counterId }
                if (activeCounter != null) {
                    val recentDelta = viewModel.recentDeltas[activeCounter.id] ?: 0L
                    val isVisible = viewModel.isDeltaVisible[activeCounter.id] ?: false

                    FullscreenCounterScreen(
                        counter = activeCounter,
                        recentDelta = recentDelta,
                        isDeltaVisible = isVisible,
                        onModify = { delta ->
                            viewModel.modifyValue(activeCounter.id, delta)
                        },
                        onSetDirectValue = { newValue ->
                            viewModel.setDirectValue(activeCounter.id, newValue)
                        },
                        onReset = {
                            viewModel.resetCounter(activeCounter.id)
                        },
                        onBack = {
                            finish()
                        }
                    )
                } else {
                    finish()
                }
            }
        }
    }

    companion object {
        const val EXTRA_COUNTER_ID = "extra_counter_id"

        fun start(context: Context, counterId: String) {
            val intent = Intent(context, FullscreenCounterActivity::class.java).apply {
                putExtra(EXTRA_COUNTER_ID, counterId)
            }
            context.startActivity(intent)
        }
    }
}
