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
import dev.contratop.contracounter.ui.HistoryScreen
import dev.contratop.contracounter.ui.theme.ContraCounterTheme

/**
 * Actividad independiente que muestra el historial de modificaciones del contador,
 * agrupado por día, con la puntuación sumada o restada y la hora exacta (HH:mm:ss).
 */
class HistoryActivity : ComponentActivity() {

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
            val allHistory by viewModel.historyFlow.collectAsState()
            val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
            val isDark = isSystemInDarkTheme()

            ContraCounterTheme(
                colorTheme = colorTheme,
                darkTheme = isDark,
                hapticsEnabled = hapticsEnabled
            ) {
                val counter = counters.find { it.id == counterId }
                if (counter != null) {
                    val counterHistory = allHistory.filter { it.counterId == counterId }

                    HistoryScreen(
                        counter = counter,
                        history = counterHistory,
                        onClearHistory = {
                            viewModel.clearHistory(counter.id)
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
            val intent = Intent(context, HistoryActivity::class.java).apply {
                putExtra(EXTRA_COUNTER_ID, counterId)
            }
            context.startActivity(intent)
        }
    }
}
