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

## H8: base de datos Room preparada, sin integración con el catálogo

Se añade Room 2.7.2 con Runtime, KTX, Compiler y soporte de pruebas. KSP 2.2.10-2.0.2 corresponde a Kotlin 2.2.10 y se ejecuta con KSP2; no se cambian Kotlin, AGP, Gradle ni JDK.

`ItemEntity` representa la tabla `items` con clave primaria `id` y todos los campos del dominio, incluido `favorito`. `ItemDao` expone Flow de todos los productos y favoritos, búsqueda por ID, inserción/actualización, cambio de favorito, conteos y borrado explícito. La inserción/actualización es transaccional: se insertan los IDs nuevos y se actualizan solo los datos descriptivos de los existentes. Su estado favorito permanece intacto; se modifica explícitamente mediante actualizarFavorito. No se borran y recrean las filas para actualizar sus datos.

`AppDatabase` tiene versión 1, nombre `smartcatalog.db` y exportSchema=true. El esquema JSON exportado en `app/schemas/` se versiona para preparar futuras migraciones; no se versionan las clases generadas por KSP en build. No hay migración destructiva ni borrado al iniciar.

AppContainer proporciona la base de datos y el DAO mediante propiedades lazy con contexto de aplicación. El catálogo y el detalle siguen usando exclusivamente ItemRepositoryRemoto: Room no se abre ni consulta desde la interfaz en H8. Los mappers locales conservan favorito en ambos sentidos y no alteran el mapeo remoto existente.

Las pruebas instrumentadas usan Room.inMemoryDatabaseBuilder y cierran la base al finalizar cada prueba, sin tocar una base real ni consultar Internet. Cubren inserción, búsqueda, favoritos, conteos, borrado, cambios de Flow y preservación de favoritos al actualizar. Se mantienen las 40 pruebas anteriores y se añaden pruebas unitarias de mappers locales.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

En H8 no se conecta Retrofit con Room, no se añade persistencia al catálogo ni un modo offline persistente, y no se implementan favoritos en la interfaz, Firebase, login, roles, administración ni notificaciones.

## H9: catálogo respaldado por Room

El catálogo de ejecución utiliza ahora `ItemRepositoryImpl`, inyectado por `AppContainer` en los ViewModels de la actividad. La lista y los favoritos del contrato proceden exclusivamente de los Flow de `ItemDao`, convertidos con los mappers locales. No se publican respuestas HTTP directamente en Compose.

La carga inicial observa inmediatamente los datos guardados y ejecuta una única sincronización. La actualización manual conserva el diseño existente. Si falla la red con datos locales, estos siguen visibles junto al aviso de error ya existente; una primera emisión vacía no se considera una carga finalizada.

La sincronización valida todo el lote antes de escribir y usa `insertarTodos`, cuya transacción conserva el favorito de cada ID existente. Un error no borra registros y un fallo durante la escritura revierte todo el lote. Una lista vacía válida devuelve éxito con cantidad cero y conserva los datos existentes; tampoco se eliminan automáticamente IDs ausentes de una respuesta posterior. No se interpreta esta API como una orden de borrado.

Los conteos, la alternancia atómica de favorito y `limpiarCache()` operan sobre Room. No tienen nuevos controles en la UI. El detalle sigue consultando `products/{id}` y devuelve un fallo si esa consulta falla, sin sustitución local. Los repositorios anteriores se conservan para las pruebas de regresión existentes y no se inyectan en la aplicación.

No se han cambiado versiones, esquema ni versión de la base de datos. Las pruebas de integración utilizan Room en memoria y una API falsa, sin Internet. La verificación real de H9 se documenta en `docs/H9_VERIFICACION.md`.
