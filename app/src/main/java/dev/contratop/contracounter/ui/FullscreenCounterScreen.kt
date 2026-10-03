package dev.contratop.contracounter.ui

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.ui.components.ResetConfirmDialog
import dev.contratop.contracounter.ui.components.SetDirectValueDialog
import dev.contratop.contracounter.ui.components.ShadowDeltaBadge
import dev.contratop.contracounter.ui.theme.CounterAccents

/**
 * Pantalla completa del contador seleccionado.
 *
 * Características:
 * - Número en tamaño colosal (80-100sp) con animación hacia arriba elástica.
 * - Botones gigantes de sumar 1 y restar 1 para pulsar con los pulgares con máxima comodidad.
 * - Filas de ajuste rápido (+2, +5, +10, +Custom y -2, -5, -10, -Custom).
 * - Agregados útiles:
 *   - Modo Mantener Pantalla Encendida (KeepScreenOn) para partidas largas sin que se apague el móvil.
 *   - Edición directa del valor mediante diálogo modal numérico.
 *   - Reinicio protegido con confirmación a su valor inicial.
 *   - Badge Shadow Delta flotante en vivo (acumulado 5s / visibilidad 3s).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullscreenCounterScreen(
    counter: Counter,
    recentDelta: Long,
    isDeltaVisible: Boolean,
    onModify: (delta: Long) -> Unit,
    onSetDirectValue: (newValue: Long) -> Unit,
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = context as? Activity
    val isDark = isSystemInDarkTheme()
    val accent = CounterAccents.getOrElse(counter.colorIndex) { CounterAccents[0] }

    // Control de pantalla siempre encendida
    var keepScreenOn by remember { mutableStateOf(true) }

    DisposableEffect(keepScreenOn) {
        if (keepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Gestionar el botón atrás físico de Android
    BackHandler(onBack = onBack)

    var showEditDirectDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var customAdjustIsAdd by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Volver a la lista de contadores",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(accent.iconTint)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = counter.title.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // Botón Mantener Pantalla Encendida (Keep Screen On)
                    IconButton(
                        onClick = {
                            keepScreenOn = !keepScreenOn
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (keepScreenOn) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (keepScreenOn) Icons.Rounded.WbSunny else Icons.Rounded.LockOpen,
                            contentDescription = if (keepScreenOn) "Pantalla siempre encendida (Activo)" else "Pantalla encendida desactivada",
                            tint = if (keepScreenOn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Botón Reset
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showResetConfirmDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Reiniciar contador",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Botón Editar directo
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showEditDirectDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "Editar valor directamente",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ZONA SUPERIOR / CENTRAL: NÚMERO GIGANTE Y SHADOW DELTA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Odómetro gigante con animación hacia arriba
                    AnimatedContent(
                        targetState = counter.currentValue,
                        transitionSpec = {
                            (slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically(
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ) { height -> -height } + fadeOut())
                        },
                        label = "fullscreen_number_odometer"
                    ) { targetVal ->
                        Text(
                            text = targetVal.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (targetVal.toString().length > 4) 74.sp else 104.sp,
                                letterSpacing = (-2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badge Shadow Delta flotante
                    Box(
                        modifier = Modifier.height(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ShadowDeltaBadge(
                            delta = recentDelta,
                            visible = isDeltaVisible
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Valor inicial: ${counter.initialValue}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // ZONA INFERIOR: BOTONES DE ACCIÓN RÁPIDA Y HERO BUTTONS (+1 / -1) GIGANTES
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // FILA DE AJUSTE RÁPIDO PARA SUMAR (+2, +5, +10, +Custom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val addGreen = Color(0xFF4CAF50)
                    val addBg = if (isDark) Color(0xFF1B3B22) else Color(0xFFE8F5E9)

                    FullscreenQuickChip(
                        text = "+2",
                        tint = addGreen,
                        bgColor = addBg,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(2L)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FullscreenQuickChip(
                        text = "+5",
                        tint = addGreen,
                        bgColor = addBg,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(5L)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FullscreenQuickChip(
                        text = "+10",
                        tint = addGreen,
                        bgColor = addBg,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(10L)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FullscreenQuickChip(
                        text = "+Custom",
                        tint = addGreen,
                        bgColor = addBg,
                        isCustom = true,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            customAdjustIsAdd = true
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                // FILA DE AJUSTE RÁPIDO PARA RESTAR (-2, -5, -10, -Custom)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val subRed = MaterialTheme.colorScheme.error
                    val subBg = if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else Color(0xFFFFEBEE)

                    FullscreenQuickChip(
                        text = "-2",
                        tint = subRed,
                        bgColor = subBg,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(-2L)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FullscreenQuickChip(
                        text = "-5",
                        tint = subRed,
                        bgColor = subBg,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(-5L)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FullscreenQuickChip(
                        text = "-10",
                        tint = subRed,
                        bgColor = subBg,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(-10L)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FullscreenQuickChip(
                        text = "-Custom",
                        tint = subRed,
                        bgColor = subBg,
                        isCustom = true,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            customAdjustIsAdd = false
                        },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // LOS DOS BOTONES GIGANTES HERO (+1 / -1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // BOTÓN GIGANTE SUMAR 1 (+1)
                    val addGreen = Color(0xFF4CAF50)
                    val addBg = if (isDark) Color(0xFF1B3B22) else Color(0xFFE8F5E9)
                    val addBorder = if (isDark) Color(0xFF2E7D32) else Color(0xFFA5D6A7)

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(1L)
                        },
                        shape = RoundedCornerShape(26.dp),
                        color = addBg,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(105.dp)
                            .border(2.dp, addBorder, RoundedCornerShape(26.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Sumar 1",
                                tint = addGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "+ 1",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 34.sp
                                ),
                                color = addGreen
                            )
                        }
                    }

                    // BOTÓN GIGANTE RESTAR 1 (-1)
                    val subRed = MaterialTheme.colorScheme.error
                    val subBg = if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else Color(0xFFFFEBEE)
                    val subBorder = if (isDark) MaterialTheme.colorScheme.error.copy(alpha = 0.6f) else Color(0xFFEF9A9A)

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onModify(-1L)
                        },
                        shape = RoundedCornerShape(26.dp),
                        color = subBg,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .weight(1f)
                            .height(105.dp)
                            .border(2.dp, subBorder, RoundedCornerShape(26.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove,
                                contentDescription = "Restar 1",
                                tint = subRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "- 1",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 34.sp
                                ),
                                color = subRed
                            )
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO: Modificar valor directamente
    if (showEditDirectDialog) {
        SetDirectValueDialog(
            counter = counter,
            onConfirm = { newValue ->
                onSetDirectValue(newValue)
                showEditDirectDialog = false
            },
            onDismiss = { showEditDirectDialog = false }
        )
    }

    // DIÁLOGO: Confirmar reinicio
    if (showResetConfirmDialog) {
        ResetConfirmDialog(
            counter = counter,
            onConfirm = {
                onReset()
                showResetConfirmDialog = false
            },
            onDismiss = { showResetConfirmDialog = false }
        )
    }

    // SUB-DIÁLOGO: Cantidad personalizada para sumar o restar
    customAdjustIsAdd?.let { isAdd ->
        FullscreenCustomAmountDialog(
            isAdd = isAdd,
            onConfirm = { customValue ->
                val delta = if (isAdd) customValue else -customValue
                onModify(delta)
                customAdjustIsAdd = null
            },
            onDismiss = { customAdjustIsAdd = null }
        )
    }
}

/**
 * Chip para los botones secundarios de ajuste rápido en pantalla completa.
 */
@Composable
private fun FullscreenQuickChip(
    text: String,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCustom: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier.height(46.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            if (isCustom) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (text.startsWith("+")) Icons.Rounded.Add else Icons.Rounded.Remove,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Custom",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = tint
                    )
                }
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = tint
                )
            }
        }
    }
}

/**
 * Diálogo para introducir una cantidad personalizada en pantalla completa.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FullscreenCustomAmountDialog(
    isAdd: Boolean,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf("20") }
    val isDark = isSystemInDarkTheme()

    fun submit() {
        val amount = amountText.toLongOrNull()?.coerceAtLeast(1L) ?: return
        onConfirm(amount)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = if (isDark) Color(0xFF26242A) else Color(0xFFF5EEF8),
        icon = {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = if (isAdd) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = if (isAdd) "Sumar cantidad personalizada" else "Restar cantidad personalizada",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val quickOptions = listOf(5L, 15L, 20L, 25L, 50L, 100L)
                    quickOptions.forEach { opt ->
                        FilterChip(
                            selected = amountText == opt.toString(),
                            onClick = { amountText = opt.toString() },
                            label = {
                                Text(
                                    text = "${if (isAdd) "+" else "-"}$opt",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancelar")
            }
        }
    )
}
