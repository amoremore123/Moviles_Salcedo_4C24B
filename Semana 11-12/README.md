# SmartCatalog — Laboratorio 11-12

Aplicación individual de Programación en Móviles. Paquete: `com.tecsup.smartcatalog`.

## H4: catálogo con datos ficticios

`MainActivity` crea `ListaViewModel` mediante su fábrica e inyecta `ItemRepositoryPrueba`. El ViewModel depende del contrato `ItemRepository`, observa su flujo y expone `StateFlow<UiState<List<Item>>>`. `ListaScreen` recoge el estado con Lifecycle y presenta carga, error con reintento, lista vacía o una `LazyColumn` con tarjetas `ItemCard`.

La colección de cuatro smartphones es ilustrativa. Los precios de ejemplo se muestran en soles (PEN); las ilustraciones son vectores locales con una imagen de respaldo. El repositorio funciona exclusivamente en memoria y recarga la colección estática. No existe persistencia ni sincronización real. La interfaz no ofrece acciones de detalle ni favoritos.

## Herramientas y compilación

- Kotlin 2.2.10, Android Gradle Plugin 8.13.2 y Gradle 8.13.
- Android SDK: compileSdk/targetSdk 36; minSdk 24.
- JDK 17, Jetpack Compose y Material 3.
- Lifecycle 2.7.0 para ViewModel y observación del estado.

Configurar el SDK en `local.properties` (excluido de Git). Desde esta carpeta, en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Las pruebas verifican la colección, el resultado para IDs inexistentes, la recarga en memoria y las transiciones de carga/error/éxito del ViewModel, incluido el reintento.

## Evidencias

Las capturas reales del emulador se guardan en `evidencias/h4/`. Esta carpeta se excluye de Git, al igual que los registros locales de verificación. El proyecto utiliza el repositorio Git del curso, sin un repositorio interno.

## H5: cliente de API preparado, catálogo de H4 conservado

Se configuraron Retrofit/converter-gson 2.11.0, Gson 2.11.0 y OkHttp 4.12.0, junto con DTO, mappers y permiso de Internet. La API todavía no alimenta la pantalla: siguen apareciendo los cuatro smartphones ficticios de H4.

La comprobación HTTP real, el esquema observado, la política de campos ausentes y las pruebas sin Internet se documentan en `docs/H5_API.md`. Ejecutar el mismo comando de compilación y pruebas indicado arriba incluye tanto las pruebas de H4 como las nuevas de H5.
