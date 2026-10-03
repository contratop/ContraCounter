package dev.contratop.contracounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import dev.contratop.contracounter.ui.ContraCounterApp
import dev.contratop.contracounter.ui.CounterViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            ContraCounterApp(viewModel = viewModel)
        }
    }
}
