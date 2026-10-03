package dev.contratop.contracounter.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Insignia flotante / sombra que muestra el delta acumulado en los últimos 5 segundos
 * y se desvanece tras 3 segundos de inactividad.
 */
@Composable
fun ShadowDeltaBadge(
    delta: Long,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && delta != 0L,
        enter = fadeIn() + scaleIn(initialScale = 0.8f) + slideInVertically { it / 2 },
        exit = fadeOut() + scaleOut(targetScale = 0.85f) + slideOutVertically { -it / 3 },
        modifier = modifier
    ) {
        val isPositive = delta > 0
        val sign = if (isPositive) "+" else ""
        val text = "$sign$delta"

        // Colores Material 3 con brillo y sombra sutil
        val badgeBackground = if (isPositive) {
            Color(0xFF2E7D32).copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        }

        val badgeBorder = if (isPositive) {
            Color(0xFF81C784).copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
        }

        val textColor = if (isPositive) {
            Color(0xFF81C784)
        } else {
            MaterialTheme.colorScheme.error
        }

        Box(
            modifier = Modifier
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = textColor.copy(alpha = 0.5f),
                    ambientColor = textColor.copy(alpha = 0.3f)
                )
                .background(
                    color = badgeBackground,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = 1.dp,
                    color = badgeBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = text,
                transitionSpec = {
                    (fadeIn() + slideInVertically { if (isPositive) -it else it }) togetherWith
                            (fadeOut() + slideOutVertically { if (isPositive) it else -it })
                },
                label = "delta_text_anim"
            ) { targetText ->
                Text(
                    text = targetText,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        shadow = Shadow(
                            color = textColor.copy(alpha = 0.6f),
                            offset = Offset(0f, 2f),
                            blurRadius = 8f
                        )
                    )
                )
            }
        }
    }
}
