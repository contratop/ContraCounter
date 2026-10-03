# ContraCounter 🔢⚡

> Aplicación de contadores para Android diseñada en **Material Design 3 (Material You)** nativo, ultra reactiva, ergonómica y optimizada especialmente para dispositivos **Google Pixel** y Android moderno.

---

## 🚀 ¿Por qué ContraCounter?

La mayoría de apps de contadores parecen sacadas de 2012: cuadrículas negras muertas, botones toscos y cero información sobre qué cojones acabas de pulsar si le das rápido en una partida de cartas o juego de mesa.

**ContraCounter** reconstruye la experiencia desde los cimientos en **Jetpack Compose + Material 3**:
- 🎨 **Material You Nativo:** Soporte dinámico de colores del sistema (`DynamicColor`) que sincroniza toda la interfaz con la paleta de tu fondo de pantalla (Monet) en Android 12+ (brutal en el Pixel 10a).
- 🌓 **Temas Claro, Oscuro y Sistema:** Selector rápido de temas accesible directamente desde la barra superior.
- 👻 **Shadow Delta Tracker (5s / 3s):** Si pulsas repetidamente sobre `+` o `-`, un distintivo flotante con brillo y sombra suave acumula el total sumado o restado en los últimos 5 segundos y permanece visible durante 3 segundos tras la última pulsación para que jamás dudes de cuánto has variado el contador.
- 🎯 **Ajuste Directo por Pulsación Larga:** Mantén pulsado el centro de cualquier contador para abrir el diálogo modal M3 y escribir directamente la cifra o usar los chips rápidos (`+10`, `+5`, `-5`, `-10`, restaurar al inicial).
- 🛡️ **Reset y Borrado Protegidos con Confirmación:** Diálogos Material 3 con elevación tonal para evitar pulsaciones accidentales en mitad de una partida crucial.
- ➕ **Nuevo Contador con Cantidad Inicial y Paso:** Diálogo de creación con selector de cantidad inicial, tamaño de paso (`step`), paletas de acento visual (Esmeralda, Rubí, Zafiro, Ámbar, Amatista) y plantillas predefinidas (Vida MTG 20/40, Rondas, etc.).
- 💾 **Persistencia Instantánea:** Todos tus contadores, estados, pasos y colores se guardan localmente para que continúes exactamente donde lo dejaste.
- 📳 **Haptic Feedback:** Respuesta háptica táctil refinada en cada interacción y pulsación larga.

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Kotlin 1.9+
- **UI Framework:** Jetpack Compose (BOM 2023.10.01 / M3)
- **Design System:** Material Design 3 con `dynamicLightColorScheme` y `dynamicDarkColorScheme`
- **Gestión de Estado:** Android ViewModel + Kotlin Coroutines + `StateFlow`
- **Persistencia:** SharedPreferences + Gson
- **Target SDK:** Android 34 (soporta desde Android 8.0 Oreo - API 26 hasta Android 15/16)

---

## 📁 Estructura del Código

```text
ContraCounter/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/dev/contratop/contracounter/
│   │   │   ├── MainActivity.kt
│   │   │   ├── data/
│   │   │   │   ├── Counter.kt
│   │   │   │   └── CounterRepository.kt
│   │   │   └── ui/
│   │   │       ├── ContraCounterApp.kt
│   │   │       ├── CounterViewModel.kt
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt
│   │   │       │   ├── Theme.kt
│   │   │       │   └── Type.kt
│   │   │       └── components/
│   │   │           ├── CounterCard.kt
│   │   │           ├── ShadowDeltaBadge.kt
│   │   │           ├── AddCounterDialog.kt
│   │   │           ├── SetDirectValueDialog.kt
│   │   │           ├── ResetConfirmDialog.kt
│   │   │           └── DeleteConfirmDialog.kt
│   │   └── res/
│   │       ├── drawable/app_icon.xml
│   │       └── values/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 📦 Compilación y Generación del APK

El proyecto utiliza el wrapper de Gradle con JDK 17 configurado en `gradle.properties`.

### 1. Compilar APK Debug (Listo para instalar directamente)
```bash
./gradlew assembleDebug
```
El APK generado se encuentra en:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Instalar en tu Pixel o dispositivo conectado por USB/ADB
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Compilar APK Release
```bash
./gradlew assembleRelease
```
El APK se genera en:
`app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 👥 Desarrollado por
Creado por **ContratopDev** & **Conexor**.
