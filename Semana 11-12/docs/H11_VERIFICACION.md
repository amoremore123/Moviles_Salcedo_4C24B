# H11 — Modo sin conexión, origen local y recuperación

## Comportamiento

Room sigue siendo la fuente de verdad del catálogo y de favoritos. No se cambian tabla, esquema, versión de base de datos, Kotlin, AGP, Gradle ni dependencias. No se usa ConnectivityManager como prueba de que el servidor responde.

`ListaViewModel` conserva el contrato visual anterior y añade `EstadoSincronizacion`: inicial, actualización (con fallo anterior opcional), correcta y fallo. Junto con el Flow local permite distinguir primera carga, datos disponibles, progreso, fallo con datos guardados y fallo sin respaldo.

El aviso «No se pudo actualizar el catálogo. Mostrando productos guardados.» requiere productos locales y un fallo real de sincronización. Se mantiene durante el reintento, con el botón deshabilitado mientras hay consulta activa, y desaparece tras un éxito. Las lecturas normales de Room no activan el aviso. Los errores de favorito no se convierten en errores de sincronización. Sin registros locales se muestra ErrorView, Reintentar y la indicación de que se necesita Internet para descargar productos por primera vez. Una lista vacía válida conserva su estado de éxito; sigue vigente la política de H9 de no borrar registros ante una respuesta vacía.

`DetalleConOrigen` distingue `REMOTO` y `LOCAL`. La nueva operación `obtenerDetalleConRespaldo()` intenta primero el endpoint real `products/{id}` con su validación existente y, solo si falla, busca el ID mediante el DAO. Si hay registro devuelve origen local; sin registro devuelve fallo. La cancelación se propaga y no produce un respaldo artificial.

El método anterior `obtenerDetalle()` conserva su contrato remoto, reutilizado por la nueva operación; así las implementaciones y pruebas existentes siguen siendo compatibles. Los repositorios sin política Room explícita conservan por defecto la consulta anterior. La aplicación utiliza `ItemRepositoryImpl`, que sí implementa el respaldo local.

El detalle guardado muestra «Detalle guardado. No se pudo consultar la versión actual.» y Reintentar. Un reintento vuelve al endpoint y solo un resultado remoto elimina ese origen local. El favorito sigue observando Room; una ficha local también refleja los cambios de su fila. Una emisión local por sí sola no transforma un error sin respaldo en éxito ni oculta un reintento fallido.

`SmartCatalogApplication` mantiene una única instancia de `AppContainer` por proceso. La actividad y sus fábricas comparten el mismo repositorio y base al rotar, evitando mezclar un ViewModel retenido con otro contenedor y otra instancia de Room. Los ViewModels no conocen Context, Room ni Retrofit. No hay otra capa de caché.

Favoritos conserva la implementación de H10: consulta y alternancia local, sin HTTP, incluso sin Internet. La sincronización posterior conserva esos cambios mediante la transacción existente.

## Archivos

Nuevos:

- `app/src/main/java/com/tecsup/smartcatalog/SmartCatalogApplication.kt`.
- `app/src/main/java/com/tecsup/smartcatalog/domain/model/DetalleConOrigen.kt`.
- `app/src/main/java/com/tecsup/smartcatalog/ui/lista/EstadoSincronizacion.kt`.
- `app/src/main/java/com/tecsup/smartcatalog/ui/components/AvisoDatosGuardados.kt`.
- `app/src/main/res/drawable/ic_datos_guardados.xml`.
- `app/src/test/java/com/tecsup/smartcatalog/ui/offline/ModoOfflineViewModelsTest.kt`.
- `app/src/androidTest/java/com/tecsup/smartcatalog/data/repository/ModoOfflineRepositoryTest.kt`.
- `app/src/androidTest/java/com/tecsup/smartcatalog/RotacionSinRecargaTest.kt`.
- `app/src/androidTest/java/com/tecsup/smartcatalog/testing/SmartCatalogTestRunner.kt`.
- `app/src/androidTest/java/com/tecsup/smartcatalog/testing/SmartCatalogTestApplication.kt`.
- Este informe.

Modificados: README, `app/build.gradle.kts` (solo runner de pruebas), AndroidManifest (Application), MainActivity, AppContainer, ItemRepository, ItemRepositoryImpl, ListaViewModel, ListaScreen, DetalleViewModel y DetalleScreen.

## Verificación técnica

Comando ejecutado desde la raíz del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --console=plain
```

Resultado: **BUILD SUCCESSFUL en 12 min 33 s**, 73 tareas, 33 ejecutadas y 40 al día.

- **76 pruebas unitarias aprobadas**, sin fallos, errores ni omitidas: las 65 anteriores intactas y 11 nuevas. Verifican primera carga fallida sin caché, datos locales en carga normal, fallo conservado durante reintento, recuperación, éxito vacío, solicitudes simultáneas, separación entre fallo de favorito y sincronización, emisiones locales tardías y los orígenes del detalle.
- **29 pruebas instrumentadas aprobadas**, sin fallos, errores ni omitidas: las 23 anteriores intactas, 5 nuevas de repositorio con Room real en memoria y 1 de recreación de la actividad real.
- Las pruebas nuevas de repositorio verifican detalle remoto, respaldo local con favorito, error sin respaldo, quitar favorito sin red y conservarlo al sincronizar, y propagación de cancelación sin convertirla en respaldo.
- `RotacionSinRecargaTest` recreó MainActivity con una consulta pendiente: comprobó la misma instancia de ListaViewModel y **una sola consulta**. Tras completar la carga, un cambio de favorito en Room actualizó la UI sin nuevas consultas de lista ni detalle.
- Ninguna prueba automatizada depende de Internet. El esquema de Room y las fuentes de todas las pruebas anteriores permanecen sin cambios.

Reportes: `app/build/test-results/testDebugUnitTest/` y `app/build/outputs/androidTest-results/connected/debug/`. Registro completo: `.h1-verification/h11/build.log`.

El runner personalizado solo sustituye la Application dentro del proceso instrumentado: utiliza API falsa y Room en memoria para la actividad. La APK debug instalada normalmente sigue utilizando DummyJSON y la base persistente. Las pruebas antiguas mantienen sus bases aisladas y APIs falsas, sin cambios de fuente.

## Ejecución y capturas

AVD `Medium_Phone`, Android 17, ejecutado sin ventana. Se instaló la APK debug normal, sin la Application falsa del runner. Antes de abrirla se confirmó que no existía `smartcatalog.db`.

1. **Primera instalación sin caché y sin red:** Wi-Fi y datos móviles desactivados. Se mostró el error de conexión, la indicación de necesitar Internet para la primera descarga y Reintentar. No hubo productos ficticios ni aviso de datos guardados.
2. **Recuperación de la primera descarga:** al restaurar Internet y reintentar se descargaron **16 smartphones reales**. El catálogo normal no mostró aviso de caché. Se marcó iPhone 5s como favorito.
3. **Persistencia tras cerrar y abrir sin red:** los 16 productos y el favorito permanecieron visibles desde Room. Al fallar la nueva sincronización apareció el aviso de productos guardados y siguieron disponibles navegación y reintento.
4. **Favoritos sin Internet:** se consultó Mis favoritos y se quitó iPhone 5s. Desapareció de favoritos y su corazón quedó vacío en catálogo. El aviso de sincronización fallida no se borró por ese cambio local.
5. **Detalle local:** la consulta al segundo endpoint falló sin red y se mostró el registro de Room, ID 121, con el aviso explícito de detalle guardado. Se conservó el favorito local desmarcado.
6. **Recuperación del detalle:** se restauró Internet y se pulsó Reintentar; el endpoint remoto respondió y desapareció el aviso local.
7. **Recuperación del catálogo:** al volver desde detalle todavía se conservó el fallo anterior de la lista, pese a que Internet ya funcionaba. Solo al pulsar Reintentar y completar su propia sincronización desapareció el aviso. El favorito quitado offline no reapareció.
8. **Rotación real:** se giró el emulador a horizontal y se restauró su orientación original. El catálogo conservó 16 productos y el favorito desmarcado. La ausencia de solicitudes duplicadas se comprobó con el contador de la API falsa en la prueba instrumentada de recreación.
9. **Consulta final SQLite:** una copia de solo lectura confirmó **16 filas, 16 IDs distintos y 0 favoritos**; el registro 121 permaneció con `favorito=0`, coherente con haber quitado el único favorito durante la prueba offline.

El detalle sin respaldo local y el éxito vacío válido se comprobaron mediante las pruebas automatizadas, incluyendo Room vacío en el emulador para el detalle. No se modificaron filas de producción artificialmente para obtener esas capturas.

En la primera apertura apareció un aviso de `System UI isn't responding` y el inicio de actividad demoró; se resolvió con `Wait` antes de validar las capturas de SmartCatalog. No se observaron cierres de la app durante la verificación visual. Se restauraron los valores originales de red, modo avión y orientación, y se cerró únicamente el emulador iniciado para H11.

Capturas reales principales, excluidas de Git:

| Ruta dentro de SmartCatalog | Evidencia |
| --- | --- |
| `evidencias/h11/01-primera-carga-sin-red.png` | Error inicial sin caché y ayuda para primera descarga |
| `evidencias/h11/02-primera-descarga-recuperada.png` | Primera descarga recuperada, sin aviso incorrecto |
| `evidencias/h11/03-catalogo-con-favorito.png` | Catálogo normal y favorito guardado |
| `evidencias/h11/04-catalogo-datos-guardados.png` | Catálogo persistido con aviso tras fallo |
| `evidencias/h11/05-favoritos-sin-red.png` | Favoritos disponibles sin Internet |
| `evidencias/h11/06-favorito-quitado-sin-red.png` | Cambio de favorito local sin conexión |
| `evidencias/h11/07-catalogo-cambio-local.png` | Corazón actualizado, aviso todavía vigente |
| `evidencias/h11/08-detalle-guardado.png` | Detalle local claramente identificado |
| `evidencias/h11/09-detalle-remoto-recuperado.png` | Reintento remoto sin aviso local |
| `evidencias/h11/10-catalogo-antes-reintento.png` | Estado propio de sincronización de la lista conservado |
| `evidencias/h11/11-catalogo-recuperado.png` | Reintento correcto que retira el aviso |
| `evidencias/h11/12-favorito-quitado-conservado.png` | Sincronización conserva el cambio offline |
| `evidencias/h11/13-rotacion-horizontal.png` | Catálogo durante rotación |
| `evidencias/h11/14-rotacion-restaurada.png` | Datos conservados al restaurar orientación |

Las jerarquías XML, comprobaciones, reportes de pruebas, estados de red y orientación, registro de ejecución y copia SQLite se conservan en `.h1-verification/h11/`, excluida de Git. No se versionan capturas, bases de datos ni archivos generados.

## Referencias y alcance

Referencias oficiales consultadas: [inyección manual y contenedor de aplicación](https://developer.android.com/training/dependency-injection/manual) y [AndroidJUnitRunner y contexto de aplicación](https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner).

Solo H11. Sin Firebase, login, roles, panel Admin ni notificaciones. No se hace push ni se avanza a H12.
