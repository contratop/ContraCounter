package dev.contratop.contracounter.ui.theme

import androidx.compose.ui.graphics.Color

// Material 3 Dark Colors
val MdDarkPrimary = Color(0xFFD0BCFF)
val MdDarkOnPrimary = Color(0xFF381E72)
val MdDarkPrimaryContainer = Color(0xFF4F378B)
val MdDarkOnPrimaryContainer = Color(0xFFEADDFF)

val MdDarkSecondary = Color(0xFFCCC2DC)
val MdDarkOnSecondary = Color(0xFF332D41)
val MdDarkSecondaryContainer = Color(0xFF4A4458)
val MdDarkOnSecondaryContainer = Color(0xFFE8DEF8)

val MdDarkTertiary = Color(0xFFEFB8C8)
val MdDarkOnTertiary = Color(0xFF492532)
val MdDarkTertiaryContainer = Color(0xFF633B48)
val MdDarkOnTertiaryContainer = Color(0xFFFFD8E4)

val MdDarkBackground = Color(0xFF141218)
val MdDarkOnBackground = Color(0xFFE6E0E9)
val MdDarkSurface = Color(0xFF141218)
val MdDarkOnSurface = Color(0xFFE6E0E9)
val MdDarkSurfaceVariant = Color(0xFF49454F)
val MdDarkOnSurfaceVariant = Color(0xFFCAC4D0)
val MdDarkSurfaceContainer = Color(0xFF211F26)
val MdDarkSurfaceContainerHigh = Color(0xFF2B2930)

val MdDarkError = Color(0xFFF2B8B5)
val MdDarkOnError = Color(0xFF601410)
val MdDarkErrorContainer = Color(0xFF8C1D18)
val MdDarkOnErrorContainer = Color(0xFFF9DEDC)

// Material 3 Light Colors
val MdLightPrimary = Color(0xFF6750A4)
val MdLightOnPrimary = Color(0xFFFFFFFF)
val MdLightPrimaryContainer = Color(0xFFEADDFF)
val MdLightOnPrimaryContainer = Color(0xFF21005D)

val MdLightSecondary = Color(0xFF625B71)
val MdLightOnSecondary = Color(0xFFFFFFFF)
val MdLightSecondaryContainer = Color(0xFFE8DEF8)
val MdLightOnSecondaryContainer = Color(0xFF1D192B)

val MdLightTertiary = Color(0xFF7D5260)
val MdLightOnTertiary = Color(0xFFFFFFFF)
val MdLightTertiaryContainer = Color(0xFFFFD8E4)
val MdLightOnTertiaryContainer = Color(0xFF31111D)

val MdLightBackground = Color(0xFFFEF7FF)
val MdLightOnBackground = Color(0xFF1D1B20)
val MdLightSurface = Color(0xFFFEF7FF)
val MdLightOnSurface = Color(0xFF1D1B20)
val MdLightSurfaceVariant = Color(0xFFE7E0EC)
val MdLightOnSurfaceVariant = Color(0xFF49454F)
val MdLightSurfaceContainer = Color(0xFFF3EDF7)
val MdLightSurfaceContainerHigh = Color(0xFFECE6F0)

val MdLightError = Color(0xFFB3261E)
val MdLightOnError = Color(0xFFFFFFFF)
val MdLightErrorContainer = Color(0xFFF9DEDC)
val MdLightOnErrorContainer = Color(0xFF410E0B)

// Accent Colors para personalizar tarjetas de contador
data class CounterAccent(
    val name: String,
    val lightBorder: Color,
    val lightContainer: Color,
    val darkBorder: Color,
    val darkContainer: Color,
    val iconTint: Color
)

val CounterAccents = listOf(
    CounterAccent(
        name = "Estándar M3",
        lightBorder = Color(0xFF79747E),
        lightContainer = Color(0xFFF3EDF7),
        darkBorder = Color(0xFF49454F),
        darkContainer = Color(0xFF211F26),
        iconTint = Color(0xFFD0BCFF)
    ),
    CounterAccent(
        name = "Esmeralda",
        lightBorder = Color(0xFF2E7D32),
        lightContainer = Color(0xFFE8F5E9),
        darkBorder = Color(0xFF81C784),
        darkContainer = Color(0xFF1B3B22),
        iconTint = Color(0xFF81C784)
    ),
    CounterAccent(
        name = "Rubí",
        lightBorder = Color(0xFFC62828),
        lightContainer = Color(0xFFFFEBEE),
        darkBorder = Color(0xFFE57373),
        darkContainer = Color(0xFF3E1F21),
        iconTint = Color(0xFFE57373)
    ),
    CounterAccent(
        name = "Zafiro",
        lightBorder = Color(0xFF1565C0),
        lightContainer = Color(0xFFE3F2FD),
        darkBorder = Color(0xFF64B5F6),
        darkContainer = Color(0xFF192F45),
        iconTint = Color(0xFF64B5F6)
    ),
    CounterAccent(
        name = "Ámbar",
        lightBorder = Color(0xFFEF6C00),
        lightContainer = Color(0xFFFFF3E0),
        darkBorder = Color(0xFFFFB74D),
        darkContainer = Color(0xFF3E2C1A),
        iconTint = Color(0xFFFFB74D)
    ),
    CounterAccent(
        name = "Amatista",
        lightBorder = Color(0xFF6A1B9A),
        lightContainer = Color(0xFFF3E5F5),
        darkBorder = Color(0xFFBA68C8),
        darkContainer = Color(0xFF351B40),
        iconTint = Color(0xFFBA68C8)
    )
)
