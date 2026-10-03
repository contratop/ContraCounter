package dev.contratop.contracounter.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.contratop.contracounter.data.Counter
import dev.contratop.contracounter.ui.theme.CounterAccents

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CounterCard(
    counter: Counter,
    recentDelta: Long,
    isDeltaVisible: Boolean,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onResetRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    onDirectValueRequest: () -> Unit,
    onQuickAdjustRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val accent = CounterAccents.getOrElse(counter.colorIndex) { CounterAccents[0] }

    val cardBorderColor = if (isDark) accent.darkBorder.copy(alpha = 0.4f) else accent.lightBorder.copy(alpha = 0.35f)
    val cardBgColor = if (isDark) {
        Color(0xFF211F26)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorderColor, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // COLUMNA IZQUIERDA: Botones de Sumar y Restar (+ / -)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón SUMAR (+) -> Click normal suma, pulsación prolongada abre mini modal de ajuste rápido
                ActionButton(
                    text = "+ ${counter.step}",
                    icon = Icons.Rounded.Add,
                    tint = Color(0xFF4CAF50),
                    containerColor = if (isDark) Color(0xFF1B3B22) else Color(0xFFE8F5E9),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onIncrement()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuickAdjustRequest()
                    }
                )

                // Botón RESTAR (-) -> Click normal resta, pulsación prolongada abre mini modal de ajuste rápido
                ActionButton(
                    text = "- ${counter.step}",
                    icon = Icons.Rounded.Remove,
                    tint = MaterialTheme.colorScheme.error,
                    containerColor = if (isDark) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f) else Color(0xFFFFEBEE),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDecrement()
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuickAdjustRequest()
                    }
                )
            }

            // COLUMNA CENTRAL: Título, Número Gigante y Shadow Delta (Al mantener pulsado se abre editor directo)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .combinedClickable(
                        onClick = {
                            // Feedback sutil al pulsar en el centro
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDirectValueRequest()
                        }
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Título del contador con tag sutil
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(accent.iconTint)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = counter.title.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Número Grande con animación al cambiar de valor
                    AnimatedContent(
                        targetState = counter.currentValue,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInVertically { -it / 2 } + fadeIn()) togetherWith
                                        (slideOutVertically { it / 2 } + fadeOut())
                            } else {
                                (slideInVertically { it / 2 } + fadeIn()) togetherWith
                                        (slideOutVertically { -it / 2 } + fadeOut())
                            }
                        },
                        label = "counter_number_anim"
                    ) { targetValue ->
                        Text(
                            text = targetValue.toString(),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = if (targetValue.toString().length > 4) 34.sp else 46.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Shadow Delta flotante (acumulado últimos 5s, visible 3s tras pulsar)
                    Box(
                        modifier = Modifier.height(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ShadowDeltaBadge(
                            delta = recentDelta,
                            visible = isDeltaVisible
                        )
                    }
                }
            }

            // COLUMNA DERECHA: Botones de Reset y Eliminar
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón RESET
                MiniActionButton(
                    text = "Reset",
                    icon = Icons.Rounded.RestartAlt,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onResetRequest()
                    }
                )

                // Botón ELIMINAR
                MiniActionButton(
                    text = "Eliminar",
                    icon = Icons.Rounded.DeleteOutline,
                    tint = MaterialTheme.colorScheme.error,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteRequest()
                    }
                )
            }
        }
    }
}

/**
 * Botón ergonómico de sumar o restar en estilo Material 3.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    containerColor: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier
            .width(76.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text.substringAfter(" "),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = tint
            )
        }
    }
}

/**
 * Botón de acción para Reset y Eliminar.
 */
@Composable
private fun MiniActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .width(82.dp)
            .height(54.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = tint
            )
        }
    }
}
