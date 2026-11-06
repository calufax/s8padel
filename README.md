# Super 8 Pádel (Android)

Aplicación Android nativa (Kotlin + Jetpack Compose + Material 3) para llevar en vivo un mini torneo **Super 8 / Americano de Pádel** de 8 jugadores en 2 canchas simultáneas.

## Características principales

- **Fixture matemático perfecto (`Wh(8)`)**: 7 rondas en 2 canchas simultáneas (14 partidos). Cada jugador juega **1 vez de compañero** con cada uno de los otros 7 y **2 veces en contra** de cada rival.
- **Rotación equilibrada de canchas**: Asignación balanceada entre Cancha 1 y Cancha 2 (más botón rápido `⇄ Canchas` en cada ronda) y opción de **Sortear orden** de los 8 jugadores.
- **Carga de resultados con 1 toque**: Partidos a **4 games** (`4-0`, `4-1`, `4-2`, `4-3`; en `3-3` se juega el 7° game normal sin tie-break).
- **Tabla de posiciones en vivo + Planilla R1–R7**: Se actualiza instantáneamente con cada partido, incluyendo vista de tabla general, planilla clásica por ronda y botón para compartir resultados por WhatsApp.
- **Sistema de desempate oficial**:
  1. **Partidos Ganados (`PG`)**
  2. **Diferencia de Games (`DG = GF - GC`)**
  3. **Games a Favor (`GF`)**
  4. **Cruce directo como rivales** (balance de victorias y games en los 2 duelos en contra)
- **Persistencia local atómica (`AtomicFile`)**: Funciona 100% offline; si cerrás la app o se reinicia el teléfono, no se pierde ningún dato cargado.

## Compilación

Abrí esta carpeta en **Android Studio** o ejecutá por consola:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

El APK instalable se genera en `app/build/outputs/apk/debug/app-debug.apk`.
