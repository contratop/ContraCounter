package dev.contratop.contracounter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.contratop.contracounter.data.AppColorTheme

// --- PALETAS MATERIAL 3 ---
private val DarkColorScheme = darkColorScheme(
    primary = MdDarkPrimary,
    onPrimary = MdDarkOnPrimary,
    primaryContainer = MdDarkPrimaryContainer,
    onPrimaryContainer = MdDarkOnPrimaryContainer,
    secondary = MdDarkSecondary,
    onSecondary = MdDarkOnSecondary,
    secondaryContainer = MdDarkSecondaryContainer,
    onSecondaryContainer = MdDarkOnSecondaryContainer,
    tertiary = MdDarkTertiary,
    onTertiary = MdDarkOnTertiary,
    tertiaryContainer = MdDarkTertiaryContainer,
    onTertiaryContainer = MdDarkOnTertiaryContainer,
    background = MdDarkBackground,
    onBackground = MdDarkOnBackground,
    surface = MdDarkSurface,
    onSurface = MdDarkOnSurface,
    surfaceVariant = MdDarkSurfaceVariant,
    onSurfaceVariant = MdDarkOnSurfaceVariant,
    error = MdDarkError,
    onError = MdDarkOnError,
    errorContainer = MdDarkErrorContainer,
    onErrorContainer = MdDarkOnErrorContainer
)

private val LightColorScheme = lightColorScheme(
    primary = MdLightPrimary,
    onPrimary = MdLightOnPrimary,
    primaryContainer = MdLightPrimaryContainer,
    onPrimaryContainer = MdLightOnPrimaryContainer,
    secondary = MdLightSecondary,
    onSecondary = MdLightOnSecondary,
    secondaryContainer = MdLightSecondaryContainer,
    onSecondaryContainer = MdLightOnSecondaryContainer,
    tertiary = MdLightTertiary,
    onTertiary = MdLightOnTertiary,
    tertiaryContainer = MdLightTertiaryContainer,
    onTertiaryContainer = MdLightOnTertiaryContainer,
    background = MdLightBackground,
    onBackground = MdLightOnBackground,
    surface = MdLightSurface,
    onSurface = MdLightOnSurface,
    surfaceVariant = MdLightSurfaceVariant,
    onSurfaceVariant = MdLightOnSurfaceVariant,
    error = MdLightError,
    onError = MdLightOnError,
    errorContainer = MdLightErrorContainer,
    onErrorContainer = MdLightOnErrorContainer
)

// --- PALETAS MODO POKE (FANCY CHIC) ---
private val PokeDarkColorScheme = darkColorScheme(
    primary = PokeDarkPrimary,
    onPrimary = PokeDarkOnPrimary,
    primaryContainer = PokeDarkPrimaryContainer,
    onPrimaryContainer = PokeDarkOnPrimaryContainer,
    secondary = PokeDarkSecondary,
    onSecondary = PokeDarkOnSecondary,
    secondaryContainer = PokeDarkSecondaryContainer,
    onSecondaryContainer = PokeDarkOnSecondaryContainer,
    tertiary = PokeDarkTertiary,
    onTertiary = PokeDarkOnTertiary,
    tertiaryContainer = PokeDarkTertiaryContainer,
    onTertiaryContainer = PokeDarkOnTertiaryContainer,
    background = PokeDarkBackground,
    onBackground = PokeDarkOnBackground,
    surface = PokeDarkSurface,
    onSurface = PokeDarkOnSurface,
    surfaceVariant = PokeDarkSurfaceVariant,
    onSurfaceVariant = PokeDarkOnSurfaceVariant,
    error = MdDarkError,
    onError = MdDarkOnError
)

private val PokeLightColorScheme = lightColorScheme(
    primary = PokeLightPrimary,
    onPrimary = PokeLightOnPrimary,
    primaryContainer = PokeLightPrimaryContainer,
    onPrimaryContainer = PokeLightOnPrimaryContainer,
    secondary = PokeLightSecondary,
    onSecondary = PokeLightOnSecondary,
    secondaryContainer = PokeLightSecondaryContainer,
    onSecondaryContainer = PokeLightOnSecondaryContainer,
    tertiary = PokeLightTertiary,
    onTertiary = PokeLightOnTertiary,
    tertiaryContainer = PokeLightTertiaryContainer,
    onTertiaryContainer = PokeLightOnTertiaryContainer,
    background = PokeLightBackground,
    onBackground = PokeLightOnBackground,
    surface = PokeLightSurface,
    onSurface = PokeLightOnSurface,
    surfaceVariant = PokeLightSurfaceVariant,
    onSurfaceVariant = PokeLightOnSurfaceVariant,
    error = MdLightError,
    onError = MdLightOnError
)

// --- PALETAS CYBERPUNK ---
private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberDarkPrimary,
    onPrimary = CyberDarkOnPrimary,
    primaryContainer = CyberDarkPrimaryContainer,
    onPrimaryContainer = CyberDarkOnPrimaryContainer,
    secondary = CyberDarkSecondary,
    onSecondary = CyberDarkOnSecondary,
    secondaryContainer = CyberDarkSecondaryContainer,
    onSecondaryContainer = CyberDarkOnSecondaryContainer,
    background = CyberDarkBackground,
    onBackground = CyberDarkOnBackground,
    surface = CyberDarkSurface,
    onSurface = CyberDarkOnSurface,
    surfaceVariant = CyberDarkSurfaceVariant,
    onSurfaceVariant = CyberDarkOnSurfaceVariant,
    error = MdDarkError,
    onError = MdDarkOnError
)

private val CyberLightColorScheme = lightColorScheme(
    primary = CyberLightPrimary,
    onPrimary = CyberLightOnPrimary,
    primaryContainer = CyberLightPrimaryContainer,
    onPrimaryContainer = CyberLightOnPrimaryContainer,
    secondary = CyberLightSecondary,
    onSecondary = CyberLightOnSecondary,
    secondaryContainer = CyberLightSecondaryContainer,
    onSecondaryContainer = CyberLightOnSecondaryContainer,
    background = CyberLightBackground,
    onBackground = CyberLightOnBackground,
    surface = CyberLightSurface,
    onSurface = CyberLightOnSurface,
    surfaceVariant = CyberLightSurfaceVariant,
    onSurfaceVariant = CyberLightOnSurfaceVariant,
    error = MdLightError,
    onError = MdLightOnError
)

// --- PALETAS MATCHA ESMERALDA ---
private val EmeraldDarkColorScheme = darkColorScheme(
    primary = EmeraldDarkPrimary,
    onPrimary = EmeraldDarkOnPrimary,
    primaryContainer = EmeraldDarkPrimaryContainer,
    onPrimaryContainer = EmeraldDarkOnPrimaryContainer,
    secondary = EmeraldDarkSecondary,
    onSecondary = EmeraldDarkOnSecondary,
    secondaryContainer = EmeraldDarkSecondaryContainer,
    onSecondaryContainer = EmeraldDarkOnSecondaryContainer,
    background = EmeraldDarkBackground,
    onBackground = EmeraldDarkOnBackground,
    surface = EmeraldDarkSurface,
    onSurface = EmeraldDarkOnSurface,
    surfaceVariant = EmeraldDarkSurfaceVariant,
    onSurfaceVariant = EmeraldDarkOnSurfaceVariant,
    error = MdDarkError,
    onError = MdDarkOnError
)

private val EmeraldLightColorScheme = lightColorScheme(
    primary = EmeraldLightPrimary,
    onPrimary = EmeraldLightOnPrimary,
    primaryContainer = EmeraldLightPrimaryContainer,
    onPrimaryContainer = EmeraldLightOnPrimaryContainer,
    secondary = EmeraldLightSecondary,
    onSecondary = EmeraldLightOnSecondary,
    secondaryContainer = EmeraldLightSecondaryContainer,
    onSecondaryContainer = EmeraldLightOnSecondaryContainer,
    background = EmeraldLightBackground,
    onBackground = EmeraldLightOnBackground,
    surface = EmeraldLightSurface,
    onSurface = EmeraldLightOnSurface,
    surfaceVariant = EmeraldLightSurfaceVariant,
    onSurfaceVariant = EmeraldLightOnSurfaceVariant,
    error = MdLightError,
    onError = MdLightOnError
)

@Composable
fun ContraCounterTheme(
    colorTheme: AppColorTheme = AppColorTheme.MATERIAL_3,
    darkTheme: Boolean = isSystemInDarkTheme(), // Sigue el sistema automáticamente
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme: ColorScheme = when (colorTheme) {
        AppColorTheme.MATERIAL_3 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        AppColorTheme.POKE -> {
            if (darkTheme) PokeDarkColorScheme else PokeLightColorScheme
        }
        AppColorTheme.CYBERPUNK -> {
            if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme
        }
        AppColorTheme.EMERALD -> {
            if (darkTheme) EmeraldDarkColorScheme else EmeraldLightColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.surface.toArgb()
                window.navigationBarColor = colorScheme.surface.toArgb()
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
