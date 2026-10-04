package dev.contratop.contracounter.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class DiceType(val label: String, val iconSymbol: String, val sides: Int, val count: Int = 1) {
    D4("D4", "▲", 4, 1),
    D6("D6", "■", 6, 1),
    TWO_D6("2×D6", "⚂⚂", 6, 2),
    D8("D8", "◆", 8, 1),
    D10("D10", "⬠", 10, 1),
    D12("D12", "⬟", 12, 1),
    D20("D20", "⬡", 20, 1),
    D100("D100", "⚪", 100, 1)
}

enum class CoinSide(val label: String, val emoji: String) {
    HEADS("Cara", "👑"),
    TAILS("Cruz", "⚔️")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabletopToolsDialog(
    hapticsEnabled: Boolean,
    onDismiss: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var selectedTab by remember { mutableIntStateOf(0) }

    fun triggerVibe(strong: Boolean = false) {
        if (!hapticsEnabled) return
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = if (strong) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(if (strong) 50L else 20L)
            }
        } else {
            haptic.performHapticFeedback(if (strong) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = if (isDark) Color(0xFF232228) else Color(0xFFF9F7FA),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Casino,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Utilidades de Mesa",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        triggerVibe()
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Selector de pestañas: Dados / Moneda / Quién empieza
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            triggerVibe()
                            selectedTab = 0
                        },
                        text = { Text("Dados", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Rounded.Casino, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            triggerVibe()
                            selectedTab = 1
                        },
                        text = { Text("Moneda", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Rounded.MonetizationOn, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            triggerVibe()
                            selectedTab = 2
                        },
                        text = { Text("Turno", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Rounded.People, contentDescription = null, modifier = Modifier.size(20.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> DiceTabContent(isDark = isDark, onTriggerVibe = ::triggerVibe)
                    1 -> CoinTabContent(isDark = isDark, onTriggerVibe = ::triggerVibe)
                    2 -> FirstPlayerTabContent(isDark = isDark, onTriggerVibe = ::triggerVibe)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Listo", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Genera la forma geométrica polyédrica según el tipo de dado (D4, D6, D8, D10, D12, D20, D100)
 */
fun getPolyhedralShape(type: DiceType): Shape {
    return when (type) {
        DiceType.D4 -> GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            // Triángulo equilátero / tetraedro
            moveTo(w * 0.5f, h * 0.04f)
            lineTo(w * 0.96f, h * 0.92f)
            lineTo(w * 0.04f, h * 0.92f)
            close()
        }
        DiceType.D6, DiceType.TWO_D6 -> RoundedCornerShape(18.dp)
        DiceType.D8 -> GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            // Rombo / diamante (octaedro)
            moveTo(w * 0.5f, h * 0.03f)
            lineTo(w * 0.97f, h * 0.5f)
            lineTo(w * 0.5f, h * 0.97f)
            lineTo(w * 0.03f, h * 0.5f)
            close()
        }
        DiceType.D10 -> GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            // Trapezoedro pentagonal (escudo de 5 vértices)
            moveTo(w * 0.5f, h * 0.03f)
            lineTo(w * 0.96f, h * 0.40f)
            lineTo(w * 0.74f, h * 0.97f)
            lineTo(w * 0.26f, h * 0.97f)
            lineTo(w * 0.04f, h * 0.40f)
            close()
        }
        DiceType.D12 -> GenericShape { size, _ ->
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.48f
            // Pentágono regular (dodecaedro)
            for (i in 0 until 5) {
                val angle = Math.toRadians((i * 72.0 - 90.0)).toFloat()
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        DiceType.D20 -> GenericShape { size, _ ->
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = min(size.width, size.height) * 0.49f
            // Hexágono regular (proyección icosaédrica del D20)
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60.0 - 30.0)).toFloat()
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        DiceType.D100 -> CircleShape
    }
}

/**
 * Dibuja las líneas de facetas interiores 3D según la geometría de cada dado
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFacets(
    type: DiceType,
    isDark: Boolean,
    accentColor: Color
) {
    val w = size.width
    val h = size.height
    val strokeColor = if (accentColor != Color.White) {
        accentColor.copy(alpha = 0.45f)
    } else if (isDark) {
        Color.White.copy(alpha = 0.22f)
    } else {
        Color.Black.copy(alpha = 0.14f)
    }
    val strokeWidth = 1.5.dp.toPx()

    when (type) {
        DiceType.D4 -> {
            val center = Offset(w * 0.5f, h * 0.63f)
            drawLine(strokeColor, center, Offset(w * 0.5f, h * 0.04f), strokeWidth)
            drawLine(strokeColor, center, Offset(w * 0.96f, h * 0.92f), strokeWidth)
            drawLine(strokeColor, center, Offset(w * 0.04f, h * 0.92f), strokeWidth)
        }
        DiceType.D6, DiceType.TWO_D6 -> {
            val pad = 8.dp.toPx()
            drawRoundRect(
                color = strokeColor.copy(alpha = strokeColor.alpha * 0.7f),
                topLeft = Offset(pad, pad),
                size = Size(w - pad * 2, h - pad * 2),
                cornerRadius = CornerRadius(10.dp.toPx()),
                style = Stroke(width = strokeWidth)
            )
        }
        DiceType.D8 -> {
            drawLine(strokeColor, Offset(w * 0.03f, h * 0.5f), Offset(w * 0.97f, h * 0.5f), strokeWidth)
            drawLine(strokeColor, Offset(w * 0.5f, h * 0.03f), Offset(w * 0.5f, h * 0.97f), strokeWidth)
        }
        DiceType.D10 -> {
            val center = Offset(w * 0.5f, h * 0.52f)
            drawLine(strokeColor, center, Offset(w * 0.5f, h * 0.03f), strokeWidth)
            drawLine(strokeColor, center, Offset(w * 0.96f, h * 0.40f), strokeWidth)
            drawLine(strokeColor, center, Offset(w * 0.74f, h * 0.97f), strokeWidth)
            drawLine(strokeColor, center, Offset(w * 0.26f, h * 0.97f), strokeWidth)
            drawLine(strokeColor, center, Offset(w * 0.04f, h * 0.40f), strokeWidth)
        }
        DiceType.D12 -> {
            val cx = w / 2f
            val cy = h / 2f
            val innerR = min(w, h) * 0.25f
            val path = Path()
            for (i in 0 until 5) {
                val angle = Math.toRadians((i * 72.0 + 90.0)).toFloat()
                val x = cx + innerR * cos(angle)
                val y = cy + innerR * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, strokeColor, style = Stroke(width = strokeWidth))
        }
        DiceType.D20 -> {
            val cx = w / 2f
            val cy = h / 2f
            val r = min(w, h) * 0.49f
            val path = Path()
            val angles = listOf(-30.0, 90.0, 210.0)
            angles.forEachIndexed { i, deg ->
                val angle = Math.toRadians(deg).toFloat()
                val x = cx + r * cos(angle)
                val y = cy + r * sin(angle)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, strokeColor, style = Stroke(width = strokeWidth))
        }
        DiceType.D100 -> {
            val cx = w / 2f
            val cy = h / 2f
            val r = min(w, h) * 0.38f
            drawCircle(strokeColor, radius = r, center = Offset(cx, cy), style = Stroke(width = strokeWidth))
        }
    }
}

/**
 * Componente visual de Dado Polihédrico con sombreado M3, facetas y transformación 3D
 */
@Composable
private fun PolyhedralDieView(
    type: DiceType,
    value: Int,
    isRolling: Boolean,
    isDark: Boolean,
    scale: Float,
    rotZ: Float,
    rotX: Float,
    rotY: Float,
    shakeX: Float,
    shakeY: Float,
    elevation: Float,
    glowAlpha: Float
) {
    val shape = remember(type) { getPolyhedralShape(type) }
    val dieSize = if (type == DiceType.TWO_D6) 74.dp else 94.dp

    val isD20Crit = type == DiceType.D20 && value == 20 && !isRolling
    val isD20Fumble = type == DiceType.D20 && value == 1 && !isRolling
    val isMaxRoll = !isRolling && type.count == 1 && value == type.sides && type != DiceType.D20

    // Gradiente dinámico según resultado (oro para crítico, rojo para pifia, m3 normal)
    val baseColors = when {
        isD20Crit -> listOf(Color(0xFFFFF176), Color(0xFFFFB300), Color(0xFFFF8F00))
        isD20Fumble -> listOf(Color(0xFFFF8A80), Color(0xFFD32F2F), Color(0xFF880E4F))
        isMaxRoll -> listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        )
        isDark -> listOf(
            Color(0xFF383542),
            Color(0xFF26242E),
            Color(0xFF1E1C24)
        )
        else -> listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
            MaterialTheme.colorScheme.surfaceVariant
        )
    }

    val textColor = when {
        isD20Crit -> Color(0xFF3E2723)
        isD20Fumble -> Color.White
        isDark -> Color(0xFFF0EDF5)
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val borderColor = when {
        isD20Crit -> Color(0xFFFFD700)
        isD20Fumble -> Color(0xFFFF5252)
        glowAlpha > 0f -> MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha)
        isRolling -> MaterialTheme.colorScheme.primary
        isDark -> Color.White.copy(alpha = 0.25f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .size(dieSize)
            .graphicsLayer {
                rotationZ = rotZ
                rotationX = rotX
                rotationY = rotY
                scaleX = scale
                scaleY = scale
                translationX = shakeX
                translationY = shakeY
                cameraDistance = 16f * density
            }
            .shadow(
                elevation = elevation.dp,
                shape = shape,
                clip = false
            )
            .clip(shape)
            .background(Brush.radialGradient(colors = baseColors))
            .border(
                width = if (isD20Crit || isD20Fumble || isRolling) 2.5.dp else 1.5.dp,
                color = borderColor,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Facetas internas del poliedro
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawFacets(
                type = type,
                isDark = isDark,
                accentColor = if (isD20Crit) Color(0xFFB57C00) else Color.White
            )
        }

        // Posicionamiento geométrico de texto (D4 y D10 tienen centro de masa desplazado)
        val textModifier = when (type) {
            DiceType.D4 -> Modifier.padding(top = 18.dp)
            DiceType.D10 -> Modifier.padding(top = 4.dp)
            else -> Modifier
        }

        Text(
            text = value.toString(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = when {
                    type == DiceType.D100 -> 24.sp
                    type == DiceType.TWO_D6 -> 28.sp
                    value >= 100 -> 24.sp
                    value >= 10 -> 32.sp
                    else -> 38.sp
                }
            ),
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = textModifier
        )
    }
}

/**
 * Pestaña del Tirador de Dados (Dice Roller con animación Material 3 Expressive y dados polihédricos)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiceTabContent(
    isDark: Boolean,
    onTriggerVibe: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedDiceType by remember { mutableStateOf(DiceType.D6) }
    var diceResults by remember { mutableStateOf(listOf(6)) }
    var isRolling by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<String>() }

    // Animaciones Material 3 Expressive Motion
    val scaleAnim = remember { Animatable(1f) }
    val rotZAnim = remember { Animatable(0f) }
    val rotXAnim = remember { Animatable(0f) }
    val rotYAnim = remember { Animatable(0f) }
    val shakeXAnim = remember { Animatable(0f) }
    val shakeYAnim = remember { Animatable(0f) }
    val elevationAnim = remember { Animatable(4f) }
    val glowAnim = remember { Animatable(0f) }

    // Segundo dado para 2xD6
    val rotZAnim2 = remember { Animatable(0f) }
    val rotXAnim2 = remember { Animatable(0f) }
    val rotYAnim2 = remember { Animatable(0f) }

    fun roll() {
        if (isRolling) return
        scope.launch {
            isRolling = true
            val count = selectedDiceType.count
            val sides = selectedDiceType.sides

            // Curva de deceleración orgánica tipo física real (13 frames)
            val delays = listOf(28L, 32L, 38L, 46L, 56L, 68L, 82L, 100L, 125L, 155L, 195L, 245L, 310L, 380L)

            elevationAnim.animateTo(16f, tween(120))
            scaleAnim.animateTo(1.12f, tween(120))

            for ((index, frameDelay) in delays.withIndex()) {
                val progress = index.toFloat() / delays.size
                val temp = List(count) { Random.nextInt(1, sides + 1) }
                diceResults = temp
                onTriggerVibe(false)

                // Efecto de volteo 3D y sacudida con amortiguación
                val decay = 1f - (progress * 0.55f)
                rotZAnim.snapTo((Random.nextFloat() * 40f - 20f) * decay)
                rotXAnim.snapTo((Random.nextFloat() * 50f - 25f) * decay)
                rotYAnim.snapTo((Random.nextFloat() * 50f - 25f) * decay)
                shakeXAnim.snapTo((Random.nextFloat() * 14f - 7f) * decay)
                shakeYAnim.snapTo((Random.nextFloat() * 14f - 7f) * decay)

                if (count > 1) {
                    rotZAnim2.snapTo((Random.nextFloat() * 40f - 20f) * decay)
                    rotXAnim2.snapTo((Random.nextFloat() * 50f - 25f) * decay)
                    rotYAnim2.snapTo((Random.nextFloat() * 50f - 25f) * decay)
                }

                delay(frameDelay)
            }

            // Resultado final
            val finalResults = List(count) { Random.nextInt(1, sides + 1) }
            diceResults = finalResults
            isRolling = false
            onTriggerVibe(true)

            // Aterrizaje elástico suave a posición de reposo
            launch { rotZAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { rotXAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { rotYAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { shakeXAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { shakeYAnim.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            launch { elevationAnim.animateTo(4f, tween(200)) }

            if (count > 1) {
                launch { rotZAnim2.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
                launch { rotXAnim2.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
                launch { rotYAnim2.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) }
            }

            // Destello de impacto y rebote elástico M3
            launch {
                glowAnim.snapTo(1f)
                glowAnim.animateTo(0f, tween(450))
            }
            scaleAnim.animateTo(1.22f, tween(70))
            scaleAnim.animateTo(1.0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))

            val total = finalResults.sum()
            val text = when {
                count > 1 -> "${selectedDiceType.iconSymbol} ${selectedDiceType.label}: [${finalResults.joinToString(" + ")}] = $total"
                selectedDiceType == DiceType.D20 && total == 20 -> "${selectedDiceType.iconSymbol} D20: 20 (¡CRÍTICO! ⚡)"
                selectedDiceType == DiceType.D20 && total == 1 -> "${selectedDiceType.iconSymbol} D20: 1 (¡PIFIA! 💀)"
                selectedDiceType == DiceType.D100 && total == 100 -> "${selectedDiceType.iconSymbol} D100: 100 (¡PERFECTO! 👑)"
                else -> "${selectedDiceType.iconSymbol} ${selectedDiceType.label}: $total"
            }
            history.add(0, text)
            if (history.size > 5) {
                history.removeLast()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector de tipo de dado
        Text(
            text = "Selecciona dado:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DiceType.values().forEach { type ->
                val isSelected = type == selectedDiceType
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else if (isDark) {
                        Color(0xFF2E2C34)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!isRolling) {
                                selectedDiceType = type
                                onTriggerVibe(false)
                                diceResults = List(type.count) { type.sides }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = type.iconSymbol,
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Caja visual del resultado con dados polihédricos
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF1E1C22) else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRolling) MaterialTheme.colorScheme.primary else Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable { roll() }
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Fila de dados (1 o 2 dados)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedDiceType == DiceType.TWO_D6) {
                        PolyhedralDieView(
                            type = DiceType.D6,
                            value = diceResults.getOrElse(0) { 6 },
                            isRolling = isRolling,
                            isDark = isDark,
                            scale = scaleAnim.value,
                            rotZ = rotZAnim.value,
                            rotX = rotXAnim.value,
                            rotY = rotYAnim.value,
                            shakeX = shakeXAnim.value,
                            shakeY = shakeYAnim.value,
                            elevation = elevationAnim.value,
                            glowAlpha = glowAnim.value
                        )
                        PolyhedralDieView(
                            type = DiceType.D6,
                            value = diceResults.getOrElse(1) { 6 },
                            isRolling = isRolling,
                            isDark = isDark,
                            scale = scaleAnim.value,
                            rotZ = rotZAnim2.value,
                            rotX = rotXAnim2.value,
                            rotY = rotYAnim2.value,
                            shakeX = -shakeXAnim.value,
                            shakeY = -shakeYAnim.value,
                            elevation = elevationAnim.value,
                            glowAlpha = glowAnim.value
                        )
                    } else {
                        PolyhedralDieView(
                            type = selectedDiceType,
                            value = diceResults.firstOrNull() ?: selectedDiceType.sides,
                            isRolling = isRolling,
                            isDark = isDark,
                            scale = scaleAnim.value,
                            rotZ = rotZAnim.value,
                            rotX = rotXAnim.value,
                            rotY = rotYAnim.value,
                            shakeX = shakeXAnim.value,
                            shakeY = shakeYAnim.value,
                            elevation = elevationAnim.value,
                            glowAlpha = glowAnim.value
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total de 2xD6
                if (diceResults.size > 1) {
                    Text(
                        text = "Total: ${diceResults.sum()}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Badges de Crítico, Pifia o Máximo
                val firstVal = diceResults.firstOrNull() ?: 0
                if (!isRolling) {
                    when {
                        selectedDiceType == DiceType.D20 && firstVal == 20 -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "¡CRÍTICO! ⚡",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        selectedDiceType == DiceType.D20 && firstVal == 1 -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFF5252).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252))
                            ) {
                                Text(
                                    text = "¡PIFIA! 💀",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFFFF5252),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        selectedDiceType == DiceType.D100 && firstVal == 100 -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                            ) {
                                Text(
                                    text = "¡100 PERFECTO! 👑",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = Color(0xFFFFD700),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        selectedDiceType.count == 1 && firstVal == selectedDiceType.sides && selectedDiceType != DiceType.D20 && selectedDiceType != DiceType.D100 -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "¡MÁXIMO! 🔥",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                Text(
                    text = if (isRolling) "¡Rulando los dados...! 🎲" else "Toca para tirar dado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón de tirar
        Button(
            onClick = { roll() },
            enabled = !isRolling,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.Casino, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRolling) "Rulando..." else "Tirar ${selectedDiceType.label}",
                fontWeight = FontWeight.Bold
            )
        }

        // Historial de tiradas
        if (history.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Últimas tiradas:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                history.take(3).forEach { item ->
                    Text(
                        text = "• $item",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Pestaña del Lanzador de Moneda (Coin Flipper)
 */
@Composable
private fun CoinTabContent(
    isDark: Boolean,
    onTriggerVibe: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var currentSide by remember { mutableStateOf(CoinSide.HEADS) }
    var isFlipping by remember { mutableStateOf(false) }
    var headsCount by remember { mutableIntStateOf(0) }
    var tailsCount by remember { mutableIntStateOf(0) }

    val rotation = remember { Animatable(0f) }

    fun flip() {
        if (isFlipping) return
        scope.launch {
            isFlipping = true
            onTriggerVibe(false)

            val newSide = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
            val targetRotation = rotation.value + 1440f

            // Efecto háptico intermedio
            launch {
                repeat(5) {
                    delay(100L)
                    onTriggerVibe(false)
                }
            }

            // Cambiar cara a mitad del giro
            launch {
                delay(350L)
                currentSide = newSide
            }

            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )

            currentSide = newSide
            if (newSide == CoinSide.HEADS) headsCount++ else tailsCount++
            isFlipping = false
            onTriggerVibe(true)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Moneda visual interactiva con rotación 3D
        Box(
            modifier = Modifier
                .size(110.dp)
                .graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 12f * density
                }
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFDF7A),
                            Color(0xFFDAA520),
                            Color(0xFFB8860B)
                        )
                    )
                )
                .border(4.dp, Color(0xFFFFE082), CircleShape)
                .clickable { flip() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = currentSide.emoji,
                    fontSize = 38.sp
                )
                Text(
                    text = currentSide.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    ),
                    color = Color(0xFF3E2723)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resultado texto en grande
        Text(
            text = if (isFlipping) "Girando en el aire..." else "${currentSide.emoji} ¡${currentSide.label.uppercase()}!",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Estadísticas de caras y cruces
        val total = headsCount + tailsCount
        Text(
            text = if (total == 0) "Pulsa la moneda o el botón para lanzar" else "Caras: $headsCount | Cruces: $tailsCount (Total: $total)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { flip() },
            enabled = !isFlipping,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.MonetizationOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isFlipping) "Girando..." else "Lanzar Moneda",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Pestaña de Quién Empieza (First Player Picker)
 */
@Composable
private fun FirstPlayerTabContent(
    isDark: Boolean,
    onTriggerVibe: (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedPlayer by remember { mutableStateOf<String?>(null) }
    var isDeciding by remember { mutableStateOf(false) }

    fun decide() {
        if (isDeciding) return
        scope.launch {
            isDeciding = true
            val players = listOf("Jugador 1", "Jugador 2")

            for (i in 0 until 10) {
                selectedPlayer = players[i % 2]
                onTriggerVibe(false)
                delay(50L + (i * 15L))
            }

            selectedPlayer = if (Random.nextBoolean()) "Jugador 1" else "Jugador 2"
            isDeciding = false
            onTriggerVibe(true)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "¿Quién inicia la partida?",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Resuelve quién tiene el primer turno al azar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDark) Color(0xFF1E1C22) else MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (selectedPlayer != null) MaterialTheme.colorScheme.primary else Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isDeciding) "Eligiendo..." else selectedPlayer?.let { "¡Empieza $it!" } ?: "Toca para decidir",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (selectedPlayer != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    textAlign = TextAlign.Center
                )

                if (selectedPlayer != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedPlayer == "Jugador 1") "🔴 Jugador 1 juega primero" else "🔵 Jugador 2 juega primero",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { decide() },
            enabled = !isDeciding,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.People, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isDeciding) "Sorteando..." else "Sortear Primer Turno",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
