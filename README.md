# ContraCounter 🔢⚡

> Aplicación de contadores para Android diseñada en **Material Design 3 (Material You)** nativo, ultra reactiva, ergonómica y optimizada especialmente para dispositivos **Google Pixel** y Android moderno.

---

## 🚀 ¿Por qué ContraCounter?

La mayoría de apps de contadores parecen sacadas de 2012: cuadrículas negras muertas, botones toscos y cero información sobre qué cojones acabas de pulsar si le das rápido en una partida de cartas o juego de mesa.

**ContraCounter** reconstruye la experiencia desde los cimientos en **Jetpack Compose + Material 3**:
- 🎨 **Estilo Nativo de Google:** Barra superior limpia con el título *ContraCounter* alineado a la izquierda y sin icono molesto, siguiendo al dedillo el diseño de las apps oficiales de Google Pixel.
- 🌓 **Adaptación Automática al Sistema:** Olvídate de conmutadores manuales cutres; si tu Android está en modo oscuro se pone oscuro, y si está en claro, en claro.
- 🎭 **Selector de Temas Internos (Modal M3):** Pulsando el icono de la paleta en la barra superior se despliega un selector de paletas visuales:
  - **Material 3 (Pixel):** Dynamic Colors de Monet según tu fondo de pantalla.
  - **Modo Poke 💕:** Fancy chic, tonos rosita pastel, blush, rose gold y glamour en honor a Laura Poke.
  - **Cyberpunk ⚡:** Vibras Night City con neón cian y acentos amarillo eléctrico.
  - **Matcha Esmeralda 🌿:** Tonos verdes botánicos y salvia ultra relajantes.
- 👻 **Shadow Delta Tracker (5s / 3s):** Si pulsas repetidamente sobre `+` o `-`, un distintivo flotante con brillo y sombra suave acumula el total sumado o restado en los últimos 5 segundos y permanece visible durante 3 segundos tras la última pulsación para que jamás dudes de cuánto has variado el contador.
- 🎯 **Ajuste Directo por Pulsación Larga:** Mantén pulsado el centro de cualquier contador para abrir el diálogo modal M3 y escribir directamente la cifra o usar los chips rápidos (`+10`, `+5`, `-5`, `-10`, restaurar al inicial).
- 🛡️ **Reset y Borrado Protegidos con Confirmación:** Diálogos Material 3 con elevación tonal para evitar pulsaciones accidentales en mitad de una partida crucial.
- ➕ **Nuevo Contador con Opción de Paso 1 en 1:** Switch integrado para fijar el paso siempre de 1 en 1 (activado por defecto) o definir un paso personalizado, cantidad inicial, plantillas rápidas (Vida MTG 20/40, Rondas, etc.) y selector de paletas de acento visual.
- 🚀 **Mini Modal de Ajuste Rápido (+/- 1, 2, 5, 10 y Custom):** Mantén pulsado el botón verde (`+1`) o el botón rojo (`-1`) de cualquier contador para abrir el panel de control rápido:
  - **Columna izquierda:** Botones instantáneos para sumar `+1`, `+2`, `+5`, `+10` y `+ Custom`.
  - **Columna central:** Contador gigante con animación vertical de odómetro hacia arriba con física elástica, shadow delta acumulado en tiempo real y botón de confirmación.
  - **Columna derecha:** Botones instantáneos para restar `-1`, `-2`, `-5`, `-10` y `- Custom`.
  - **Diálogo Custom:** Permite introducir cualquier valor a medida o usar chips rápidos (+5, +15, +20, +25, +50, +100).
- 💾 **Persistencia Instantánea:** Todos tus contadores, estados, pasos y colores se guardan localmente para que continúes exactamente donde lo dejaste.
- 📳 **Haptic Feedback:** Respuesta háptica táctil refinada en cada interacción y pulsación larga.
- 👾 **Easter Egg & Comprobador de Actualizaciones GitHub:** Mantén pulsado el título *ContraCounter* en la barra superior para abrir el modal exclusivo con el logo de **ContratopDev**, versión actual y comprobación en tiempo real contra los releases de GitHub para notificarte y redirigirte a descargar actualizaciones al instante.

---

## 📸 Capturas de Pantalla

| Vista Principal (Modo Poke 💕) | Shadow Delta Tracker (+3) | Ajuste Rápido (+/- 1,2,5,10,Cust) | Selector de Temas M3 | Programado por ContratopDev |
| :---: | :---: | :---: | :---: | :---: |
| <img src="screenshots/contracounter_01_main_poke.png" width="160" alt="Vista Principal Modo Poke" /> | <img src="screenshots/contracounter_02_shadow_delta.png" width="160" alt="Shadow Delta Tracker" /> | <img src="screenshots/contracounter_quick_adjust_modal.png" width="160" alt="Ajuste Rápido Modal" /> | <img src="screenshots/contracounter_03_temas_visuales.png" width="160" alt="Selector de Temas M3" /> | <img src="screenshots/contracounter_about_modal.png" width="160" alt="Programado por ContratopDev y Updates" /> |

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
│   │   │           ├── DeleteConfirmDialog.kt
│   │   │           ├── ThemeSelectorDialog.kt
│   │   │           ├── AboutDialog.kt
│   │   │           └── QuickAdjustModal.kt
│   │   └── res/
│   │       ├── drawable/
│   │       │   ├── app_icon.xml
│   │       │   └── contratop_logo.png
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
