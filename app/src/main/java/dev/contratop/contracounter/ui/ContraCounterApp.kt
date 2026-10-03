package dev.contratop.contracounter.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlusOne
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.contratop.contracounter.data.AppColorTheme
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.ui.components.AboutDialog
import dev.contratop.contracounter.ui.components.AddCounterDialog
import dev.contratop.contracounter.ui.components.CounterCard
import dev.contratop.contracounter.ui.components.DeleteConfirmDialog
import dev.contratop.contracounter.ui.components.QuickAdjustModal
import dev.contratop.contracounter.ui.components.ResetConfirmDialog
import dev.contratop.contracounter.ui.components.SetDirectValueDialog
import dev.contratop.contracounter.ui.components.ThemeSelectorDialog
import dev.contratop.contracounter.ui.theme.ContraCounterTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ContraCounterApp(viewModel: CounterViewModel) {
    val counters by viewModel.counters.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()

    ContraCounterTheme(
        colorTheme = colorTheme,
        darkTheme = isDark // Se adapta automáticamente al sistema
    ) {
        var showAddDialog by remember { mutableStateOf(false) }
        var showThemeDialog by remember { mutableStateOf(false) }
        var showOptionsMenu by remember { mutableStateOf(false) }
        var showAboutDialog by remember { mutableStateOf(false) }
        var counterToReset by remember { mutableStateOf<Counter?>(null) }
        var counterToDelete by remember { mutableStateOf<Counter?>(null) }
        var counterToEditDirectly by remember { mutableStateOf<Counter?>(null) }
        var counterForQuickAdjust by remember { mutableStateOf<Counter?>(null) }
        var showResetAllConfirm by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                // TopAppBar nativo de Google: alineado a la izquierda, al mantener pulsado el título abre Acerca de & Updates
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .combinedClickable(
                                    onClick = { /* Click normal */ },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showAboutDialog = true
                                        viewModel.checkForUpdates()
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ContraCounter",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Botón de temas internos (Material 3, Modo Poke, etc.)
                        IconButton(onClick = { showThemeDialog = true }) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = "Temas",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Menú de opciones (Reiniciar todos los contadores)
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Más opciones",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Reiniciar todos los contadores") },
                                    onClick = {
                                        showOptionsMenu = false
                                        showResetAllConfirm = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.RestartAlt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = {
                        Text(
                            text = "Nuevo Contador",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (counters.isEmpty()) {
                    EmptyCountersState(
                        onAddClick = { showAddDialog = true }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = counters,
                            key = { it.id }
                        ) { counter ->
                            val recentDelta = viewModel.recentDeltas[counter.id] ?: 0L
                            val isVisible = viewModel.isDeltaVisible[counter.id] ?: false

                            CounterCard(
                                counter = counter,
                                recentDelta = recentDelta,
                                isDeltaVisible = isVisible,
                                onIncrement = { viewModel.increment(counter.id) },
                                onDecrement = { viewModel.decrement(counter.id) },
                                onResetRequest = { counterToReset = counter },
                                onDeleteRequest = { counterToDelete = counter },
                                onDirectValueRequest = { counterToEditDirectly = counter },
                                onQuickAdjustRequest = { counterForQuickAdjust = counter },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }

        // MODAL: Selector de temas internos (Material 3, Modo Poke, etc.)
        if (showThemeDialog) {
            ThemeSelectorDialog(
                currentTheme = colorTheme,
                onThemeSelected = { newTheme ->
                    viewModel.setColorTheme(newTheme)
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        // MODAL: Acerca de y Comprobador de Actualizaciones (Long-press en el título)
        if (showAboutDialog) {
            AboutDialog(
                updateStatus = updateStatus,
                onCheckUpdates = { viewModel.checkForUpdates() },
                onDismiss = { showAboutDialog = false }
            )
        }

        // DIÁLOGO: Añadir Contador
        if (showAddDialog) {
            AddCounterDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { title, initialValue, step, colorIndex ->
                    viewModel.addCounter(title, initialValue, step, colorIndex)
                    showAddDialog = false
                }
            )
        }

        // DIÁLOGO: Confirmación de Reset individual
        counterToReset?.let { counter ->
            ResetConfirmDialog(
                counter = counter,
                onConfirm = {
                    viewModel.resetCounter(counter.id)
                    counterToReset = null
                },
                onDismiss = { counterToReset = null }
            )
        }

        // DIÁLOGO: Confirmación de Eliminar
        counterToDelete?.let { counter ->
            DeleteConfirmDialog(
                counter = counter,
                onConfirm = {
                    viewModel.deleteCounter(counter.id)
                    counterToDelete = null
                },
                onDismiss = { counterToDelete = null }
            )
        }

        // DIÁLOGO: Ajustar Valor Directo (al mantener pulsado en el centro del contador)
        counterToEditDirectly?.let { counter ->
            SetDirectValueDialog(
                counter = counter,
                onConfirm = { newValue ->
                    viewModel.setDirectValue(counter.id, newValue)
                    counterToEditDirectly = null
                },
                onDismiss = { counterToEditDirectly = null }
            )
        }

        // MINI MODAL: Ajuste Rápido (+/- 1, 2, 5, 10, Custom) con contador animado al centro
        counterForQuickAdjust?.let { initialCounter ->
            val liveCounter = counters.find { it.id == initialCounter.id } ?: initialCounter
            val recentDelta = viewModel.recentDeltas[liveCounter.id] ?: 0L
            val isVisible = viewModel.isDeltaVisible[liveCounter.id] ?: false

            QuickAdjustModal(
                counter = liveCounter,
                recentDelta = recentDelta,
                isDeltaVisible = isVisible,
                onModify = { delta ->
                    viewModel.modifyValue(liveCounter.id, delta)
                },
                onDismiss = { counterForQuickAdjust = null }
            )
        }

        // DIÁLOGO: Reiniciar todos los contadores
        if (showResetAllConfirm) {
            AlertDialog(
                onDismissRequest = { showResetAllConfirm = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = if (isDark) Color(0xFF26242A) else Color(0xFFF5EEF8),
                icon = {
                    Icon(
                        Icons.Rounded.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("¿Reiniciar TODOS los contadores?") },
                text = {
                    Text("Todos los contadores volverán a sus valores iniciales establecidos. ¿Deseas continuar?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetAllCounters()
                            showResetAllConfirm = false
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Reiniciar todos")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetAllConfirm = false },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

/**
 * Estado vacío cuando no hay ningún contador.
 */
@Composable
private fun EmptyCountersState(
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlusOne,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Sin contadores activos",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crea tu primer contador pulsando el botón inferior para empezar a llevar la cuenta de vida, turnos o juegos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear contador")
            }
        }
    }
}
