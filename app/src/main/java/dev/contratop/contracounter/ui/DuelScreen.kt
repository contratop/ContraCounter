package dev.contratop.contracounter.ui

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SportsKabaddi
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.contratop.contracounter.ui.components.TabletopToolsDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class DuelPreset(val label: String, val startingLife: Long, val description: String)

val DUEL_PRESETS = listOf(
    DuelPreset("20", 20L, "Magic Estándar / Duelo rápido"),
    DuelPreset("40", 40L, "Commander / EDH"),
    DuelPreset("30", 30L, "Star Wars Unlimited / Hearthstone"),
    DuelPreset("8000", 8000L, "Yu-Gi-Oh!"),
    DuelPreset("6", 6L, "Premios Pokémon")
)

@Composable
fun DuelScreen(
    hapticsEnabled: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    // Mantener la pantalla encendida durante el duelo en la mesa
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BackHandler(onBack = onBack)

    var startingLife by remember { mutableLongStateOf(20L) }

    // Estado Jugador 1 (Arriba, 180°)
    var p1Name by remember { mutableStateOf("JUGADOR 1") }
    var p1Life by remember { mutableLongStateOf(20L) }
    var p1Poison by remember { mutableIntStateOf(0) }
    var p1Delta by remember { mutableLongStateOf(0L) }
    var p1DeltaVisible by remember { mutableStateOf(false) }
    var p1Job by remember { mutableStateOf<Job?>(null) }

    // Estado Jugador 2 (Abajo, 0°)
    var p2Name by remember { mutableStateOf("JUGADOR 2") }
    var p2Life by remember { mutableLongStateOf(20L) }
    var p2Poison by remember { mutableIntStateOf(0) }
    var p2Delta by remember { mutableLongStateOf(0L) }
    var p2DeltaVisible by remember { mutableStateOf(false) }
    var p2Job by remember { mutableStateOf<Job?>(null) }

    // Modales y diálogos
    var showTabletopTools by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var playerEditingName by remember { mutableStateOf<Int?>(null) }
    var showCustomLifeDialog by remember { mutableStateOf(false) }
    var customLifeInput by remember { mutableStateOf("") }
    var winnerAnnouncement by remember { mutableStateOf<String?>(null) }
    var showSecondaryCounters by remember { mutableStateOf(false) }

    fun triggerHaptic(strong: Boolean = false) {
        if (!hapticsEnabled) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(
                    VibrationEffect.createPredefined(
                        if (strong) VibrationEffect.EFFECT_HEAVY_CLICK else VibrationEffect.EFFECT_CLICK
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(if (strong) 50L else 25L)
            }
        } else {
            haptic.performHapticFeedback(if (strong) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
        }
    }

    fun modifyP1(amount: Long) {
        p1Life += amount
        triggerHaptic()
        p1Delta += amount
        p1DeltaVisible = true
        p1Job?.cancel()
        p1Job = scope.launch {
            delay(3000L)
            p1DeltaVisible = false
            p1Delta = 0L
        }

        if (p1Life <= 0) {
            winnerAnnouncement = "$p2Name GANA LA PARTIDA 🏆"
            triggerHaptic(strong = true)
        }
    }

    fun modifyP2(amount: Long) {
        p2Life += amount
        triggerHaptic()
        p2Delta += amount
        p2DeltaVisible = true
        p2Job?.cancel()
        p2Job = scope.launch {
            delay(3000L)
            p2DeltaVisible = false
            p2Delta = 0L
        }

        if (p2Life <= 0) {
            winnerAnnouncement = "$p1Name GANA LA PARTIDA 🏆"
            triggerHaptic(strong = true)
        }
    }

    fun resetMatch(newStartingLife: Long = startingLife) {
        startingLife = newStartingLife
        p1Life = newStartingLife
        p2Life = newStartingLife
        p1Poison = 0
        p2Poison = 0
        p1Delta = 0L
        p2Delta = 0L
        p1DeltaVisible = false
        p2DeltaVisible = false
        winnerAnnouncement = null
        triggerHaptic(strong = true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121114))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ==========================================
            // JUGADOR 1 (SUPERIOR, ROTADO 180 GRADOS)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .graphicsLayer(rotationZ = 180f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2D1217),
                                Color(0xFF1E0E12)
                            )
                        )
                    )
            ) {
                PlayerDuelSection(
                    playerName = p1Name,
                    life = p1Life,
                    delta = p1Delta,
                    deltaVisible = p1DeltaVisible,
                    poison = p1Poison,
                    showSecondary = showSecondaryCounters,
                    accentColor = Color(0xFFFF5252),
                    onModify = { modifyP1(it) },
                    onModifyPoison = { change ->
                        p1Poison = (p1Poison + change).coerceAtLeast(0)
                        triggerHaptic()
                        if (p1Poison >= 10) {
                            winnerAnnouncement = "$p2Name GANA POR VENENO ☠️"
                            triggerHaptic(strong = true)
                        }
                    },
                    onEditName = { playerEditingName = 1 }
                )
            }

            // ==========================================
            // BARRA CENTRAL DE CONTROL DIVISORIA
            // ==========================================
            DuelCenterBar(
                startingLife = startingLife,
                showSecondary = showSecondaryCounters,
                onToggleSecondary = {
                    showSecondaryCounters = !showSecondaryCounters
                    triggerHaptic()
                },
                onOpenTabletopTools = {
                    triggerHaptic()
                    showTabletopTools = true
                },
                onReset = {
                    triggerHaptic()
                    showResetConfirm = true
                },
                onSelectPreset = { presetLife ->
                    triggerHaptic()
                    resetMatch(presetLife)
                },
                onOpenCustomPreset = {
                    triggerHaptic()
                    customLifeInput = startingLife.toString()
                    showCustomLifeDialog = true
                },
                onBack = onBack
            )

            // ==========================================
            // JUGADOR 2 (INFERIOR, ORIENTACIÓN NORMAL 0°)
            // ==========================================
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F1E2E),
                                Color(0xFF132A42)
                            )
                        )
                    )
            ) {
                PlayerDuelSection(
                    playerName = p2Name,
                    life = p2Life,
                    delta = p2Delta,
                    deltaVisible = p2DeltaVisible,
                    poison = p2Poison,
                    showSecondary = showSecondaryCounters,
                    accentColor = Color(0xFF448AFF),
                    onModify = { modifyP2(it) },
                    onModifyPoison = { change ->
                        p2Poison = (p2Poison + change).coerceAtLeast(0)
                        triggerHaptic()
                        if (p2Poison >= 10) {
                            winnerAnnouncement = "$p1Name GANA POR VENENO ☠️"
                            triggerHaptic(strong = true)
                        }
                    },
                    onEditName = { playerEditingName = 2 }
                )
            }
        }

        // ==========================================
        // BANNER DE VICTORIA / K.O. FLOTANTE
        // ==========================================
        AnimatedVisibility(
            visible = winnerAnnouncement != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            winnerAnnouncement?.let { text ->
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF221F28),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD700)),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            ),
                            color = Color(0xFFFFD700),
                            textAlign = TextAlign.Center
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { resetMatch() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Revancha", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { winnerAnnouncement = null },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Cerrar")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Utilidades de Mesa (Dados y Moneda)
    if (showTabletopTools) {
        TabletopToolsDialog(
            hapticsEnabled = hapticsEnabled,
            onDismiss = { showTabletopTools = false }
        )
    }

    // Diálogo de confirmación para reiniciar duelo
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("¿Reiniciar partida?", fontWeight = FontWeight.Bold) },
            text = { Text("Las vidas de ambos jugadores volverán a $startingLife y se reiniciarán los contadores de veneno.") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        resetMatch()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reiniciar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para editar nombre de jugador
    if (playerEditingName != null) {
        val playerNum = playerEditingName!!
        var currentInput by remember { mutableStateOf(if (playerNum == 1) p1Name else p2Name) }

        AlertDialog(
            onDismissRequest = { playerEditingName = null },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Nombre del Jugador $playerNum", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { currentInput = it.take(20) },
                    singleLine = true,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = currentInput.trim()
                        if (trimmed.isNotEmpty()) {
                            if (playerNum == 1) p1Name = trimmed else p2Name = trimmed
                        }
                        playerEditingName = null
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playerEditingName = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de vida inicial personalizada
    if (showCustomLifeDialog) {
        AlertDialog(
            onDismissRequest = { showCustomLifeDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Vida inicial personalizada", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customLifeInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 6) {
                            customLifeInput = input
                        }
                    },
                    singleLine = true,
                    label = { Text("Puntos de vida") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = customLifeInput.toLongOrNull()
                        if (value != null && value > 0) {
                            resetMatch(value)
                        }
                        showCustomLifeDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aplicar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomLifeDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Sección de cada jugador (vida gigante, botones táctiles y sub-contadores)
 */
@Composable
private fun PlayerDuelSection(
    playerName: String,
    life: Long,
    delta: Long,
    deltaVisible: Boolean,
    poison: Int,
    showSecondary: Boolean,
    accentColor: Color,
    onModify: (Long) -> Unit,
    onModifyPoison: (Int) -> Unit,
    onEditName: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Cabecera del jugador: Nombre
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEditName() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Editar nombre",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Puntuación Central Gigante con Delta flotante
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = life.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (life.toString().length > 3) 72.sp else 96.sp
                    ),
                    color = if (life <= 5) Color(0xFFFF5252) else Color.White
                )

                // Shadow Delta flotante (+3 / -5)
                AnimatedVisibility(
                    visible = deltaVisible && delta != 0L,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    val isPositive = delta > 0
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPositive) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (isPositive) "+$delta" else "$delta",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Contadores secundarios (Veneno)
        if (showSecondary) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "☠️ Veneno: $poison/10",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (poison >= 7) Color(0xFFFF5252) else Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onModifyPoison(-1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        IconButton(
                            onClick = { onModifyPoison(1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        // Fila de botones de ajuste rápido (-5, -1, +1, +5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // -5
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onModify(-5L) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "-5",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // -1 (Grande)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.14f),
                modifier = Modifier
                    .weight(1.3f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onModify(-1L) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "-1",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // +1 (Grande)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accentColor.copy(alpha = 0.28f),
                modifier = Modifier
                    .weight(1.3f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onModify(1L) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+1",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // +5
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = accentColor.copy(alpha = 0.16f),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onModify(5L) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+5",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * Barra divisoria central con accesos rápidos a dados, presets, reset y salida
 */
@Composable
private fun DuelCenterBar(
    startingLife: Long,
    showSecondary: Boolean,
    onToggleSecondary: () -> Unit,
    onOpenTabletopTools: () -> Unit,
    onReset: () -> Unit,
    onSelectPreset: (Long) -> Unit,
    onOpenCustomPreset: () -> Unit,
    onBack: () -> Unit
) {
    var showPresetMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1E1D22),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Salir del modo duelo
            IconButton(onClick = onBack, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Salir del duelo",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Toggle de contadores secundarios (Veneno)
            IconButton(
                onClick = onToggleSecondary,
                modifier = Modifier.size(38.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (showSecondary) Color.White.copy(alpha = 0.15f) else Color.Transparent
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = "Sub-contadores",
                    tint = if (showSecondary) Color(0xFFFFD700) else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Utilidades de mesa (Dados y Moneda)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenTabletopTools() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Casino,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mesa 🎲",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Selector de formato de vidas iniciales
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showPresetMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$startingLife HP",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }

                DropdownMenu(
                    expanded = showPresetMenu,
                    onDismissRequest = { showPresetMenu = false }
                ) {
                    DUEL_PRESETS.forEach { preset ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${preset.label} Vidas",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = preset.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                showPresetMenu = false
                                onSelectPreset(preset.startingLife)
                            }
                        )
                    }

                    Divider()

                    DropdownMenuItem(
                        text = { Text("Personalizado...") },
                        onClick = {
                            showPresetMenu = false
                            onOpenCustomPreset()
                        }
                    )
                }
            }

            // Reiniciar partida
            IconButton(onClick = onReset, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = "Reiniciar partida",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
