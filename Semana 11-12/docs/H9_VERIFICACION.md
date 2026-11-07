# H9 — Retrofit y Room como fuente de verdad

## Archivos

Creados:

- `app/src/main/java/com/tecsup/smartcatalog/data/repository/ItemRepositoryImpl.kt`.
- `app/src/test/java/com/tecsup/smartcatalog/data/repository/ItemRepositoryImplContractTest.kt`.
- `app/src/test/java/com/tecsup/smartcatalog/ui/lista/ListaViewModelLocalTest.kt`.
- `app/src/androidTest/java/com/tecsup/smartcatalog/data/repository/ItemRepositoryImplTest.kt`.
- Este informe.

Modificados:

- `AppContainer.kt`: inyecta `ItemRepositoryImpl(apiService, itemDao)` utilizando la base creada con contexto de aplicación.
- `MainActivity.kt`: documenta que sus fábricas compartidas proceden de esa integración.
- `data/local/ItemDao.kt`: incorpora una sentencia SQL atómica para alternar favorito.
- `ui/lista/ListaViewModel.kt`: muestra datos locales desde la primera emisión y los conserva ante errores de sincronización.
- `ui/lista/ListaViewModelTest.kt`: prepara vacía la caché de la prueba de error inicial; conserva sus comprobaciones de error y recuperación.
- `README.md`: documenta H9 y su política de sincronización.

## Repositorio y sincronización

`ItemRepositoryImpl` depende de `ApiService` y `ItemDao`, sin referencias a Compose ni ViewModels. `items` y `favoritos` transforman exclusivamente los Flow del DAO mediante los mappers locales. La colección HTTP nunca se publica directamente en la interfaz.

`refrescar()` ejecuta la consulta y valida todo el lote antes de escribir: requiere `products`, DTO no nulos, IDs positivos y únicos. Las ausencias de campos descriptivos conservan la política de los mappers existentes. Un registro inválido impide escribir todo el lote. La sincronización usa un mutex y un dispatcher IO; propaga cancelación y devuelve `Result.failure` ante fallos de consulta o guardado.

La escritura utiliza la transacción `insertarTodos` de H8: inserta IDs nuevos y actualiza únicamente los campos descriptivos de IDs existentes, sin sobrescribir `favorito`, tanto si es verdadero como falso. No borra la tabla antes de cargar. Una excepción al guardar revierte el lote completo, comprobado mediante un trigger SQL de prueba que aborta la segunda inserción.

**Política de respuesta vacía:** lista vacía válida significa `Result.success(0)` sin modificar registros existentes. Tampoco se eliminan automáticamente IDs ausentes de cargas posteriores: esta API se utiliza como actualización de datos, no como orden de borrado. La cantidad devuelta corresponde al lote sincronizado; los conteos del contrato consultan las filas reales de Room.

`alternarFavorito()` usa una sola sentencia `UPDATE ... SET favorito = NOT favorito`; un ID inexistente no crea registros. `limpiarCache()` elimina mediante el DAO, coordinándose con la sincronización. No se agregan controles de favoritos ni administración.

`obtenerDetalle(id)` sigue consultando `products/{id}`, valida el ID de la respuesta y no modifica el catálogo. Un fallo remoto se devuelve como fallo, incluso si el producto está en Room; no hay sustitución local silenciosa.

Los repositorios anteriores se conservan porque las pruebas de regresión los utilizan. La aplicación solo inyecta la implementación con Room. No se cambian versiones, modelo de dominio ni esquema: Kotlin 2.2.10, AGP 8.13.2, Gradle 8.13, JDK 17, Room 2.7.2, KSP 2.2.10-2.0.2, base versión 1.

## Estado de pantalla

El ViewModel depende únicamente de `ItemRepository`. Observa los datos guardados inmediatamente, ejecuta una sincronización inicial y protege la consulta frente a reintentos simultáneos. Después de una sincronización correcta obtiene una emisión local actual del mismo contrato; nunca utiliza DTO como estado visual.

Una emisión inicial vacía mantiene la carga hasta que termine la sincronización. Datos locales disponibles se muestran durante la actualización. Un error con datos conserva la lista y utiliza el aviso de reintento existente. Un error sin datos muestra el estado de error; una emisión vacía tardía no lo sustituye por un éxito falso. También se prueban emisiones locales que llegan después del fallo de red.

`ListaScreen`, `ItemCard`, navegación y diseño se conservan sin cambios. No se añaden indicadores específicos de modo offline. La caché persistente de imágenes sigue deshabilitada como en H7: sin red aparecen los recursos de respaldo existentes.

## Compilación y pruebas reales

Comando ejecutado desde la raíz de SmartCatalog:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --console=plain
```

Resultado final: **BUILD SUCCESSFUL en 3 min 20 s**, 73 tareas, 8 ejecutadas y 65 actualizadas.

- **53 pruebas unitarias aprobadas**, 0 fallos, 0 errores, 0 omitidas: las 43 anteriores y 10 nuevas (5 del contrato del repositorio y 5 del estado local del ViewModel).
- **20 pruebas instrumentadas aprobadas**, 0 fallos, 0 errores, 0 omitidas: las 10 anteriores de `ItemDaoTest` y 10 nuevas de `ItemRepositoryImplTest`.
- Room real en memoria, API falsa y ningún acceso a Internet en las pruebas automatizadas. Se verifican guardado, Flow local, segunda carga, favoritos, errores HTTP, respuesta vacía, conteos, limpieza, validación del lote, rollback SQL, detalle remoto y alternancias concurrentes.
- Las 43 pruebas previas se mantienen. Una prueba de H4 esperaba error pese a tener cuatro productos iniciales, comportamiento incompatible con H9. Se vació su caché de preparación con una línea antes de crear el ViewModel; no se eliminaron pruebas ni sus aserciones de error y recuperación. Las pruebas nuevas cubren explícitamente el caso con productos locales.
- KSP recompiló el DAO. El esquema JSON de versión 1 permanece idéntico a H8.

Los reportes reales se encuentran en `app/build/test-results/testDebugUnitTest/` y `app/build/outputs/androidTest-results/connected/debug/`. El registro de la ejecución final es `.h1-verification/h9/build-final.log`.

## Verificación real del emulador

AVD `Medium_Phone`, Android 17, `emulator-5554`, ejecutado sin ventana. Antes de abrir la instalación de verificación se comprobó que no existía `smartcatalog.db`.

1. Primera apertura: consulta real a DummyJSON; catálogo de **16 smartphones**. Una copia de la base, consultada con SQLite en modo de solo lectura tras detener el proceso, contiene **16 filas y 16 IDs distintos**.
2. Reinicio del proceso: catálogo visible nuevamente.
3. Wi-Fi y datos móviles desactivados solo en el emulador, seguido de otro reinicio: los **16 smartphones persistidos** permanecen visibles con el aviso de conexión existente.
4. Actualización manual sin red: mantiene la lista. Consulta de la base: **16 filas y 16 IDs distintos**.
5. Conectividad restaurada y actualización manual: desaparece el aviso y se muestra el catálogo. Consulta de la base después de la recuperación: **16 filas y 16 IDs distintos**, sin duplicados.
6. Detalle real de **iPhone 5s, ID 121**, y botón de regreso: funcionan y conservan la lista.
7. No se observaron errores AndroidRuntime. Los valores originales de Wi-Fi, datos móviles y modo avión quedaron restaurados.

Capturas reales, excluidas de Git:

| Ruta dentro de SmartCatalog | Evidencia |
| --- | --- |
| `evidencias/h9/01-primera-carga.png` | Catálogo tras primera sincronización |
| `evidencias/h9/02-reinicio.png` | Catálogo después de reiniciar |
| `evidencias/h9/03-reinicio-sin-red.png` | Datos de Room tras reiniciar sin Internet |
| `evidencias/h9/04-actualizacion-sin-red.png` | Actualización fallida con lista conservada |
| `evidencias/h9/05-red-restaurada.png` | Catálogo con conectividad recuperada |
| `evidencias/h9/06-actualizacion-recuperada.png` | Actualización manual correcta |
| `evidencias/h9/07-detalle-remoto.png` | Segundo endpoint real |
| `evidencias/h9/08-regreso.png` | Regreso con lista conservada |

Las jerarquías XML, las copias de verificación de SQLite y sus conteos, los estados de red y la auditoría de Git permanecen excluidos en `.h1-verification/h9/`. No se versionan bases SQLite ni archivos generados de compilación.

## Referencias y límite

Referencias oficiales consultadas: [consultas observables y asíncronas de Room](https://developer.android.com/training/data-storage/room/async-queries) y [transacciones Room](https://developer.android.com/reference/androidx/room/Transaction).

Solo H9. No se implementan botones o pantalla de favoritos, indicadores completos de modo offline, Firebase, login, roles, administración ni notificaciones. No se hace push ni se avanza a H10.
