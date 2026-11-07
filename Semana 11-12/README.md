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

## H7: detalle remoto y navegación

`AppNav` utiliza Navigation Compose 2.7.7 con los destinos `lista` y `detalle/{id}`. Solo se pasa el ID numérico. Cada entrada de detalle dispone de su propio `DetalleViewModel`, creado con una fábrica de AppContainer; este depende únicamente de ItemRepository e inicia la consulta una sola vez. El botón superior y Atrás del dispositivo permiten regresar. Las pulsaciones rápidas se filtran por destino y estado RESUMED, además de launchSingleTop.

`obtenerDetalle` llama realmente a `GET products/{id}` mediante Retrofit en Dispatchers.IO, valida y mapea la respuesta, sin alterar la lista ni utilizarla como respaldo si falla la red. Los IDs inválidos, los errores HTTP y las respuestas que pertenecen a otro ID producen un Result.failure. La cancelación se propaga.

El ViewModel de la lista se conserva en la actividad y el estado de desplazamiento se conserva mediante Navigation Compose, evitando recargas al regresar. DetalleScreen reutiliza Cargando y ErrorView y mantiene siempre visible la opción de regresar. Coil 2.7.0 presenta la imagen grande con progreso, respaldo local y ContentScale.Fit, sin caché en disco. Los precios mantienen el formato neutral de H6.

Se conservan las 28 pruebas anteriores y se añaden pruebas de DetalleViewModel y del repositorio de detalle mediante objetos falsos, sin Internet. Las capturas reales de H7 se guardan en `evidencias/h7/`, excluidas de Git. El registro histórico de H6 permanece en `docs/H6_VERIFICACION.md`.
