# H10 — Favoritos persistentes con Room

## Implementación

Se reutilizan `ItemEntity.favorito`, la tabla `items`, la base `smartcatalog.db` y el contrato `ItemRepository` existentes. No hay cambios de esquema, dependencias ni versiones. La sincronización de H9 sigue actualizando datos descriptivos sin sobrescribir el favorito local.

`ItemDao.alternarFavorito` ya implementa `UPDATE items SET favorito = NOT favorito WHERE id = :id`. La sentencia lee el valor actual y lo cambia de forma atómica; no se calcula un estado optimista desde Compose ni se realizan solicitudes HTTP. Un ID ausente no crea filas. `ItemRepositoryImpl` mantiene su operación IO existente, verificada mediante pruebas reales de Room.

`ListaViewModel` ejecuta la intención en `viewModelScope` y maneja errores conservando la lista. `FavoritosViewModel` solo depende de `ItemRepository`, observa `favoritos` como `StateFlow<UiState<List<Item>>>`, muestra vacío y permite quitar. Un mutex y una nueva lectura local dentro de cada eliminación evitan que varias pulsaciones para quitar vuelvan a agregar el producto.

`DetalleViewModel` conserva la consulta al segundo endpoint. El contenido remoto se combina únicamente con el valor favorito observado en `repository.items`. Si HTTP falla, una emisión local no transforma ese fallo en éxito ni restaura un detalle remoto anterior. Si el ID no existe localmente, el corazón queda deshabilitado. Los errores de lectura y escritura se manejan sin cerrar la aplicación.

`BotonFavorito` usa `IconToggleButton` de Material 3, con iconos vacío/relleno y descripciones accesibles que identifican el producto. `ItemCard` solo recibe callbacks y no conoce Room. El botón interior consume su propio toque; el resto de la tarjeta abre el detalle.

La nueva pantalla `Mis favoritos` reutiliza `ItemCard` y `EstadoVacio`. La ruta `favoritos` tiene su ViewModel asociado a la entrada de navegación. Se puede abrir el detalle y regresar a favoritos, o volver al catálogo. Se utilizan `launchSingleTop`, controles de ruta/ciclo de vida y estado de lista guardable para evitar destinos duplicados y conservar desplazamiento al regresar.

## Archivos

Creados:

- `ui/components/BotonFavorito.kt`.
- `ui/favoritos/FavoritosViewModel.kt`.
- `ui/favoritos/FavoritosScreen.kt`.
- `res/drawable/ic_favorito.xml` e `ic_favorito_vacio.xml`.
- `src/test/java/com/tecsup/smartcatalog/ui/favoritos/FavoritosViewModelsTest.kt`.
- `src/androidTest/java/com/tecsup/smartcatalog/data/repository/FavoritosPersistenciaTest.kt`.
- Este informe.

Modificados:

- `AppContainer.kt`.
- `ui/components/ItemCard.kt`.
- `ui/lista/ListaScreen.kt` y `ListaViewModel.kt`.
- `ui/detalle/DetalleScreen.kt` y `DetalleViewModel.kt`.
- `ui/navigation/AppNav.kt`.
- `README.md`.

Las rutas Kotlin y de recursos de producción corresponden a `app/src/main/java/com/tecsup/smartcatalog/` y `app/src/main/res/`; las rutas de pruebas corresponden a `app/`.

## Verificación

Comando ejecutado desde la raíz de SmartCatalog:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --console=plain
```

Resultado final: **BUILD SUCCESSFUL en 1 min 27 s**, 73 tareas, 1 ejecutada y 72 al día. El APK y el código de pruebas ya habían sido compilados en la ejecución inicial; la ejecución final verificó los mismos archivos sin cambios de código.

- **65 pruebas unitarias aprobadas**, 0 fallos, errores u omitidas: las 53 anteriores intactas y 12 nuevas de ViewModels. Cubren estado vacío, Flow, eliminación repetida, errores de escritura, pulsaciones rápidas, sincronización entre las tres pantallas, ausencia de nuevas consultas HTTP y errores remotos que no se ocultan con datos locales.
- **23 pruebas instrumentadas aprobadas**, 0 fallos, errores u omitidas: las 20 anteriores intactas y 3 nuevas. Las nuevas verifican favorito tras recrear repositorio y reabrir la base, sincronización posterior sin perderlo, 51 alternancias concurrentes desde dos repositorios sin HTTP y un observador continuo del Flow de favoritos al marcar/quitar.
- Las pruebas automatizadas utilizan API falsa y no dependen de Internet. Las nuevas bases de persistencia tienen nombres únicos `h10-prueba-<UUID>.db` y se cierran y eliminan al finalizar. No utilizan `smartcatalog.db` del usuario.
- La primera ejecución instrumentada no llegó a ejecutar casos: el registro `lowmemorykiller` confirmó que Android terminó el proceso durante el arranque por presión de memoria. Se conservó el diagnóstico y se repitió la suite con el emulador ya iniciado, sin cambiar código ni versiones; los 23 casos pasaron.

Reportes reales: `app/build/test-results/testDebugUnitTest/` y `app/build/outputs/androidTest-results/connected/debug/`. Registro final: `.h1-verification/h10/build-final.log`. La ejecución aislada de compilación y pruebas unitarias también terminó con `BUILD SUCCESSFUL` en 18 s.

## Ejecución real y capturas

AVD `Medium_Phone`, Android 17, `emulator-5554`, ejecutado sin ventana. Se utilizó la API real de DummyJSON para la verificación visual, después de terminar las pruebas automatizadas con API falsa.

1. Se abrió `Mis favoritos` sin productos marcados y se comprobó el mensaje amigable de estado vacío.
2. Se marcaron **iPhone 5s (121)** e **iPhone 6 (122)** desde el catálogo. Ambos corazones cambiaron a relleno y tocar esos botones no abrió el detalle.
3. Ambos aparecieron en `Mis favoritos`. Al quitar iPhone 5s, desapareció de esa pantalla y su corazón quedó vacío en el catálogo.
4. Se abrió el detalle real de iPhone 6 desde favoritos. Su corazón reflejó el valor local y el botón de regreso devolvió a favoritos, conservando la navegación.
5. Se abrió el detalle real de iPhone 5s desde catálogo y se marcó allí. Catálogo y favoritos reflejaron el cambio automáticamente.
6. Se detuvo por completo el proceso, se volvió a abrir la aplicación y ambos favoritos permanecieron.
7. Se reinició **todo el emulador** mediante `adb reboot`. Se comprobó `boot_completed=1` y un `boot_id` diferente, sin reinstalar ni borrar datos. Los dos favoritos permanecieron en catálogo y favoritos.
8. Se desactivaron Wi-Fi y datos móviles del emulador, se cerró y abrió otra vez la app y se consultó `Mis favoritos`: ambos productos y corazones siguieron disponibles desde Room. Las imágenes usaron el respaldo existente porque no hay caché persistente de imágenes.
9. Se restauró Internet y se actualizó manualmente el catálogo. Desapareció el aviso de conexión del catálogo y los favoritos se conservaron.
10. Una copia de verificación de SQLite, consultada en modo de solo lectura tras detener el proceso, confirmó **16 filas, 16 IDs distintos y 2 favoritos**, IDs **121 y 122**, tanto antes del reinicio como al finalizar.

Durante el arranque tras reiniciar el emulador aparecieron avisos de `System UI isn't responding`; se resolvieron con `Wait` antes de validar las capturas de la app. No se observaron cierres de SmartCatalog durante la verificación visual. La conectividad quedó con los mismos valores originales de Wi-Fi, datos y modo avión, y se cerró únicamente el emulador iniciado para este hito.

Capturas reales principales, excluidas de Git:

| Ruta dentro de SmartCatalog | Evidencia |
| --- | --- |
| `evidencias/h10/01-favoritos-vacio.png` | Estado vacío |
| `evidencias/h10/02-catalogo-dos-corazones.png` | Dos corazones marcados sin abrir detalle |
| `evidencias/h10/03-dos-favoritos.png` | Pantalla Mis favoritos con ambos productos |
| `evidencias/h10/04-favorito-quitado.png` | Favorito eliminado de la lista |
| `evidencias/h10/05-detalle-desde-favoritos.png` | Detalle remoto con corazón local y regreso a favoritos |
| `evidencias/h10/06-regreso-favoritos.png` | Regreso al destino correcto |
| `evidencias/h10/08-marcado-desde-detalle.png` | Favorito marcado desde el detalle |
| `evidencias/h10/09-catalogo-actualizado.png` | Estado actualizado en catálogo |
| `evidencias/h10/10-favoritos-desde-detalle.png` | Producto del detalle presente en favoritos |
| `evidencias/h10/11-persistencia-app.png` | Persistencia tras cerrar y abrir la app |
| `evidencias/h10/12-persistencia-emulador.png` | Persistencia tras reiniciar el emulador completo |
| `evidencias/h10/13-favoritos-sin-red.png` | Consulta de favoritos sin Internet |
| `evidencias/h10/14-sincronizacion-con-favoritos.png` | Nueva sincronización con favoritos conservados |
| `evidencias/h10/15-favoritos-tras-sincronizar.png` | Favoritos después de recuperar Internet y sincronizar |

Las jerarquías XML, comprobaciones de pantalla, reportes de pruebas, registros del diagnóstico inicial, identificadores de arranque, estados de red y copias SQLite de verificación permanecen excluidos en `.h1-verification/h10/`. No se versionan bases SQLite, capturas ni archivos generados de compilación.

## Referencias y alcance

Referencias oficiales consultadas: [consumo de gestos de componentes Compose](https://developer.android.com/develop/ui/compose/touch-input/pointer-input/understand-gestures) y [estado y claves de listas Compose](https://developer.android.com/develop/ui/compose/lists).

Solo H10. Sin indicadores completos de modo offline, Firebase, login, roles, administración ni notificaciones. No se hace push ni se avanza a H11.
