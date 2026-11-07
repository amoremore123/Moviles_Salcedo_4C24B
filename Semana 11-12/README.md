# SmartCatalog — Laboratorio 11-12

Aplicación individual de Programación en Móviles. Paquete: `com.tecsup.smartcatalog`.

## H6: catálogo conectado a DummyJSON

`MainActivity` utiliza `AppContainer` para obtener la fábrica de `ListaViewModel`. El contenedor proporciona `ApiService`, `ItemRepositoryRemoto` y un `ImageLoader` compartido. El ViewModel solo conoce `ItemRepository`: inicia una consulta, observa los productos y expone `StateFlow<UiState<List<Item>>>`, progreso y un mensaje de actualización.

El repositorio consulta `GET products/category/smartphones`, convierte DTO con los mappers de H5 y retiene el resultado únicamente en memoria. El número de modelos se calcula a partir de la lista recibida y validada, sin fijar una cantidad. Una respuesta vacía correcta produce el estado vacío. Un fallo inicial muestra Error y Reintentar; un fallo al actualizar conserva los productos visibles y muestra un aviso con reintento. La consulta se protege frente a solicitudes simultáneas. No se ejecutan solicitudes por recomposición.

Los IDs inválidos y repetidos se filtran. Un contenedor sin `products` o una lista no vacía formada solo por DTO inválidos produce un error controlado y conserva los datos anteriores. `obtenerDetalle` solo busca en memoria en H6; no hay pantalla de detalle. Favoritos, conteos y limpieza cumplen el contrato temporalmente en memoria y no implican persistencia.

## Imágenes y precios

Coil Compose 2.7.0 carga las URL reales con proporciones conservadas, imagen provisional y respaldo en caso de error. Los recursos locales de los ejemplos siguen siendo compatibles. El contenedor desactiva la caché en disco de Coil y las solicitudes también la deshabilitan: no se implementa almacenamiento persistente ni un modo offline real.

Los precios se presentan como números bajo «Precio de referencia», sin símbolo de soles, sin atribuir una moneda a DummyJSON y sin realizar conversiones. El encabezado identifica la fuente y el pie aclara que son importes de la API sin conversión de moneda.

## Herramientas y verificación

Kotlin 2.2.10, AGP 8.13.2, Gradle 8.13, JDK 17, Android compileSdk/targetSdk 36 y minSdk 24. Se conservan Retrofit/Gson/OkHttp y Lifecycle configurados en hitos anteriores. Configurar el SDK en `local.properties`, excluido de Git.

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

Las 15 pruebas anteriores permanecen y se añaden pruebas de repositorio remoto y ViewModel con API y repositorio falsos, sin Internet. Cubren carga, éxito, error, reintento, respuesta vacía, cancelación, IDs inválidos, consultas simultáneas y conservación de datos ante una actualización fallida.

Las evidencias reales de H6 se guardan en `evidencias/h6/`, excluidas de Git. `docs/H5_API.md` conserva el registro histórico de H5, cuando la API aún no estaba conectada. No se incorpora Room, Firebase, login, detalle, favoritos persistentes, administración ni notificaciones.
