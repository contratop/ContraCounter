package dev.contratop.contracounter.ui.components

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.ui.theme.CounterAccents

/**
 * Mini modal interactivo de ajuste rápido que se despliega al mantener pulsado
 * el botón verde (+1) o rojo (-1).
 *
 * Dispone de:
 * - Columna izquierda: Añadir 1, 2, 5, 10 y Custom (+).
 * - Columna central: Animación hacia arriba del contador grande, shadow delta en tiempo real y botón Listo.
 * - Columna derecha: Restar 1, 2, 5, 10 y Custom (-).
 */
@Composable
fun QuickAdjustModal(
    counter: Counter,
    recentDelta: Long,
    isDeltaVisible: Boolean,
    onModify: (delta: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val accent = CounterAccents.getOrElse(counter.colorIndex) { CounterAccents[0] }

    var customAdjustIsAdd by remember { mutableStateOf<Boolean?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val containerBg = if (isDark) Color(0xFF222027) else Color(0xFFF7F3F8)
        val borderColor = if (isDark) accent.darkBorder.copy(alpha = 0.5f) else accent.lightBorder.copy(alpha = 0.4f)

        Surface(
            shape = RoundedCornerShape(32.dp),
            color = containerBg,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(1.5.dp, borderColor, RoundedCornerShape(32.dp))
                .padding(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CABECERA: Título con etiqueta de color y botón cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(accent.iconTint)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = counter.title.uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Ajuste rápido",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CUERPO PRINCIPAL: 3 COLUMNAS (Añadir | Contador Animado Up | Restar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // COLUMNA IZQUIERDA: Añadir 1, 2, 5, 10 y Custom
                    Column(
                        modifier = Modifier.width(74.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val addGreen = Color(0xFF4CAF50)
                        val addBg = if (isDark) Color(0xFF1B3B22) else Color(0xFFE8F5E9)

                        QuickButton(
                            text = "+1",
                            tint = addGreen,
                            bgColor = addBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(1L)
                            }
                        )
                        QuickButton(
                            text = "+2",
                            tint = addGreen,
                            bgColor = addBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(2L)
                            }
                        )
                        QuickButton(
                            text = "+5",
                            tint = addGreen,
                            bgColor = addBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(5L)
                            }
                        )
                        QuickButton(
                            text = "+10",
                            tint = addGreen,
                            bgColor = addBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(10L)
                            }
                        )
                        QuickButton(
                            text = "+Custom",
                            isCustom = true,
                            tint = addGreen,
                            bgColor = addBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                customAdjustIsAdd = true
                            }
                        )
                    }

                    // COLUMNA CENTRAL: ANIMACIÓN TO GUAPA DEL CONTADOR HACIA ARRIBA
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Contador central con animación de odómetro hacia arriba
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
                            label = "quick_adjust_number_up_anim"
                        ) { targetVal ->
                            Text(
                                text = targetVal.toString(),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = if (targetVal.toString().length > 4) 40.sp else 52.sp,
                                    letterSpacing = (-1).sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Badge del Shadow Delta acumulado en los últimos 5s
                        Box(
                            modifier = Modifier.height(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ShadowDeltaBadge(
                                delta = recentDelta,
                                visible = isDeltaVisible
                            )
                        }

                        Text(
                            text = "Inicial: ${counter.initialValue}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Botón de confirmar / listo
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = "Listo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // COLUMNA DERECHA: Restar 1, 2, 5, 10 y Custom
                    Column(
                        modifier = Modifier.width(74.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val subRed = MaterialTheme.colorScheme.error
                        val subBg = if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else Color(0xFFFFEBEE)

                        QuickButton(
                            text = "-1",
                            tint = subRed,
                            bgColor = subBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(-1L)
                            }
                        )
                        QuickButton(
                            text = "-2",
                            tint = subRed,
                            bgColor = subBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(-2L)
                            }
                        )
                        QuickButton(
                            text = "-5",
                            tint = subRed,
                            bgColor = subBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(-5L)
                            }
                        )
                        QuickButton(
                            text = "-10",
                            tint = subRed,
                            bgColor = subBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onModify(-10L)
                            }
                        )
                        QuickButton(
                            text = "-Custom",
                            isCustom = true,
                            tint = subRed,
                            bgColor = subBg,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                customAdjustIsAdd = false
                            }
                        )
                    }
                }
            }
        }
    }

    // SUB-DIÁLOGO: Cantidad personalizada para sumar o restar
    customAdjustIsAdd?.let { isAdd ->
        CustomAmountDialog(
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
 * Botón compacto y estilizado para cada acción rápida (+/-).
 */
@Composable
private fun QuickButton(
    text: String,
    tint: Color,
    bgColor: Color,
    onClick: () -> Unit,
    isCustom: Boolean = false
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
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
                        fontSize = 18.sp
                    ),
                    color = tint
                )
            }
        }
    }
}

/**
 * Diálogo modal para introducir una cantidad personalizada a sumar o restar.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CustomAmountDialog(
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

                // Chips de cantidades rápidas
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
