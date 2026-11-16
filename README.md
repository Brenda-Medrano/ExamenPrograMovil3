# Examen de Programación Móvil 3 — 100 puntos

Proyecto basado en `ExamenPrograMovil3`, Kotlin + Jetpack Compose, AGP 9.5.1 y Gradle 9.6.0.

## Combinación elegida

- **E50-1 (50 pts)**: Firebase Remote Config `home_payload` → `RemoteSyncWorker` → Room (`features`, `banners`, `remote_sync`) → UI reactiva con caché offline y gesto pull-to-refresh.
- **E50-2 (50 pts)**: lista en Firestore `tareas` + `addSnapshotListener`; contador `stats/completadas` en Realtime Database + `ValueEventListener`; transacciones e idempotencia por ID, recuperación y contador animado.

**Integrantes:** completar con los integrantes reales del grupo.

## Requisitos

- Android Studio actualizado con SDK 37; emulador API 37 (con Google Play Services).
- Gradle Wrapper 9.6.0, JDK 25 conforme al proyecto original.
- Proyecto Firebase con paquete Android `com.example.exaprogramolvil3`.
- Copiar `google-services.json` a `app/google-services.json` (está en `.gitignore`).
- Habilitar Authentication → Anonymous, Firestore, Realtime Database y Remote Config.
- Publicar parámetro Remote Config `home_payload` como **String** con el contenido JSON de `firebase/remote-config-ejemplo.json`.
- Cargar reglas de prueba desde `firebase/firestore.rules` y `firebase/database.rules.json`, e inicializar `stats/completadas` con `0`.

## Ejecutar en Windows PowerShell

```powershell
.\gradlew.bat clean assembleDebug
.\gradlew.bat testDebugUnitTest
```

Luego ejecutar `app` en Android Studio.

## Prueba de E50-1

Con red: abrir E50-1 y sincronizar. Cambiar `home_payload` en Firebase Console, publicar y arrastrar para refrescar. Desconectar Internet, cerrar y abrir la app: la pantalla muestra los registros de Room y el indicador «Datos cacheados · offline».

## Prueba de E50-2

Abrir E50-2, comprobar login anónimo, agregar una tarea, marcarla y desmarcarla. Revisar `/stats/completadas` en RTDB y el cambio en otra sesión. Para probar manejo de errores, bloquear temporalmente una escritura RTDB y observar la compensación de Firestore; restaurar reglas después.

## Limitación de consistencia

Firestore y RTDB no tienen una transacción atómica compartida. La implementación usa transacción Firestore + transacción RTDB con marcador idempotente `stats/applied/{opId}`, rollback lógico y recuperación de pendientes. Esto es una solución demostrativa: para garantías fuertes de producción se necesita reconciliación de servidor (p. ej. Cloud Functions) y reglas más restrictivas.

## Estructura

- `remote/data`: entidades, DAO y parser JSON.
- `remote/sync`: Worker de actualización.
- `remote/ui`: pantalla, ViewModel y detección de conectividad.
- `tasks/data`: listeners y transacciones con Firebase.
- `tasks/ui`: pantalla, ViewModel y contador animado.
- `firebase/`: datos de ejemplo y reglas de prueba.

Ver la **guía paso a paso** incluida fuera del proyecto para ramas, commits, archivos y pruebas.
