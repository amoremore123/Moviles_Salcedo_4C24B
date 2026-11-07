# SmartCatalog — Laboratorio 11-12

Proyecto base individual de Programación en Móviles (H1).

- Aplicación: SmartCatalog
- Paquete: com.tecsup.smartcatalog
- Kotlin: 2.2.10
- Android Gradle Plugin: 8.13.2
- Gradle: 8.13
- Android SDK: compileSdk/targetSdk 36; minSdk 24
- Interfaz: Jetpack Compose y Material 3

## Compilar

Usar JDK 17 (instalado en este equipo) y Android SDK 36. Configurar la ruta del SDK en `local.properties`, que no se incluye en Git.

Desde esta carpeta, en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :app:assembleDebug
```

El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

## Alcance de H1

Pantalla inicial con los textos SmartCatalog, Catálogo de smartphones y Bienvenido; tema claro/oscuro y recursos básicos. La API DummyJSON se integrará en un hito posterior. Este proyecto utiliza el repositorio Git del curso y no contiene un repositorio Git interno.
