# H6 — Verificación de integración remota

## Resultado

Compilación `:app:assembleDebug` y pruebas `:app:testDebugUnitTest`: **BUILD SUCCESSFUL**. Los informes XML contienen **28 pruebas, 0 fallos y 0 errores**: se conservan las 15 anteriores y se añaden 13.

La consulta HTTP desde el equipo respondió 200 y la aplicación recibió y mostró 16 productos durante esta verificación. La cantidad no está fija en producción: el repositorio devuelve el tamaño de la lista válida y la pantalla muestra su tamaño actual.

## Casos comprobados

| Caso | Comprobación | Resultado |
|---|---|---|
| A. Con Internet | Emulador, consulta real e imágenes de la API | Catálogo real e imágenes visibles; botón Actualizar y desplazamiento operativos |
| B. Sin Internet al iniciar | Wi-Fi/datos desactivados y modo avión, después reinicio del proceso de la app | Mensaje de conexión y botón Reintentar; sin productos persistentes ni cierre inesperado |
| C. Internet recuperado | Restauración de la red y pulsación de Reintentar en la misma pantalla | Catálogo remoto recuperado con imágenes reales |
| D. Respuesta vacía | Pruebas unitarias con ApiService y repositorio falsos, sin Internet | Result.success(0) y UiState.Exito con lista vacía; ListaScreen selecciona EstadoVacio |
| E. Error al actualizar | Pérdida real de red del emulador después de una carga correcta | Aviso de error con reintento, mientras se conserva la colección visible en memoria |

La respuesta vacía se verifica con pruebas unitarias; el servidor público devolvió productos durante la prueba real. No se presenta una captura falsa de una respuesta vacía de DummyJSON.

## Evidencias reales (excluidas de Git)

- `evidencias/h6/01-catalogo-real.png`
- `evidencias/h6/02-error-actualizacion.png`
- `evidencias/h6/03-error-inicial.png`
- `evidencias/h6/04-recuperado.png`
- `evidencias/h6/05-desplazamiento.png`

Las jerarquías XML, los informes HTTP y las comprobaciones de integridad permanecen en `.h1-verification/h6/`, también excluida. Solo se alteró la conectividad del emulador; sus valores originales de Wi-Fi, datos y modo avión se restauraron. No se modificó la conectividad del equipo anfitrión.

## Arquitectura y límites de H6

`MainActivity -> AppContainer -> ListaViewModel.Factory -> ListaViewModel -> ItemRepository -> ItemRepositoryRemoto -> ApiService`.

El repositorio utiliza Dispatchers.IO y un Mutex. Publica la nueva lista solo tras completar correctamente HTTP y el mapeo, conserva la colección ante fallos y propaga las cancelaciones. Los IDs inválidos o duplicados se filtran; un contenedor inválido no se confunde con una lista vacía válida. El ViewModel observa Flow, expone StateFlow y evita consultas simultáneas sin depender de Retrofit ni de ApiService. La recomposición no inicia consultas.

Coil Compose 2.7.0 recibe un ImageLoader compartido con caché en disco desactivada, utiliza respaldo local y mantiene ContentScale.Fit. El almacenamiento del catálogo y de las imágenes es temporal en memoria; no hay Room ni un modo offline persistente. Los precios se muestran sin símbolo monetario como «Precio de referencia», sin conversión ni atribución de soles a DummyJSON.

`ItemRepositoryPrueba` y las 15 pruebas de H4/H5 se conservan. No se implementan pantallas de detalle, favoritos persistentes, login, administración ni notificaciones.
