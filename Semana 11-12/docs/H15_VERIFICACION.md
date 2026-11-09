# H15 — Notificaciones locales de SmartCatalog

El panel administrativo puede avisar mediante Android cuando una sincronización manual guarda productos en Room. La notificación muestra la cantidad confirmada por el repositorio y no contiene datos de la cuenta.

## Flujo

El panel solicita la sincronización al repositorio Retrofit y Room existente. El repositorio valida y guarda el lote mediante la transacción de H14, que conserva los favoritos. Después del resultado exitoso, el ViewModel actualiza el panel y entrega una vez la cantidad al notificador. El ViewModel evita otro envío mientras está ocupado. La publicación ocurre dentro de la operación y no depende de observar StateFlow ni de recomponer la pantalla.

Los errores de conexión, API o almacenamiento, las cancelaciones y los lotes vacíos no generan avisos. El lote vacío sigue siendo un éxito sin cambios. Las actualizaciones del catálogo fuera del panel siguen su flujo habitual sin avisos. Cada sincronización manual posterior puede publicar de nuevo. El ID fijo del aviso reemplaza la entrada anterior en la bandeja.

## Canal y permiso

Android 8 o superior utiliza el canal `smartcatalog_catalog_updates`, «Actualizaciones del catálogo», con importancia predeterminada. Crear el mismo canal conserva su identidad y la configuración del usuario. Android anterior usa NotificationCompat con prioridad predeterminada. El proyecto mantiene minSdk 24 y targetSdk 36.

En Android 13 o superior, la tarjeta «Avisos de sincronización» explica el permiso y «Activar avisos» lanza el diálogo del sistema por acción explícita. La app registra esa primera solicitud para evitar repeticiones. Si Android bloquea los avisos, el usuario puede abrir los ajustes mediante el botón. La sincronización funciona independientemente del permiso. Las versiones anteriores a Android 13 no muestran una solicitud de permiso en tiempo de ejecución.

El aviso usa un icono monocromático y un PendingIntent explícito e inmutable. Al tocarlo, Android abre MainActivity con una pila nueva. SmartCatalog valida la sesión y el rol mediante el flujo existente. Una sesión administrativa abre el panel, una sesión de usuario abre el catálogo y sin sesión se abre el login. El Intent no lleva una ruta privilegiada.

## Archivos

| Archivo | Cambio |
| --- | --- |
| `AndroidManifest.xml` | Declaración del permiso de notificaciones. |
| `data/notificaciones/NotificacionesCatalogo.kt` | Canal, permisos, publicación y PendingIntent. |
| `res/drawable/ic_aviso_catalogo.xml` y `res/values/notificaciones.xml` | Icono, textos del canal y pluralización del aviso. |
| `AppContainer.kt` y `ui/navigation/AppNav.kt` | Inyección en el panel administrativo protegido. |
| `ui/admin/PanelAdminScreen.kt` | Preferencia de avisos y solicitud explícita del permiso. |
| `ui/admin/PanelAdminViewModel.kt` | Emisión única después del guardado confirmado. |
| `PanelAdminViewModelTest.kt` y `PermisoAvisosTest.kt` | Pruebas de resultados, duplicados y versiones de Android. |
| `NotificacionesCatalogoTest.kt` y `SmartCatalogTestApplication.kt` | Pruebas del canal, permiso, persistencia y apertura con datos controlados. |
| `docs/H15_VERIFICACION.md` | Informe de verificación. |

## Verificación

La compilación de la app y el APK instrumentado terminó con `BUILD SUCCESSFUL`. Pasaron 130 pruebas unitarias, incluidas ocho nuevas.

En Android 17 pasaron 45 pruebas instrumentadas únicas y una se omitió por requerir ausencia de configuración Firebase. Se ejecutó la suite sin permiso y se completó por separado el caso que exige permiso concedido. El estado inicial del permiso se restauró. Los casos nuevos comprobaron el diálogo, el canal, la escritura de Room, la cantidad publicada, la ausencia de aviso por error o permiso denegado y la apertura con rol de usuario desde un aviso publicado como admin. Los niveles anteriores a Android 13 se cubrieron con pruebas unitarias de la política.

El APK real se verificó con una sesión de rol Usuario. Abrió el catálogo de 16 productos y conservó el favorito iPhone 5s, con el panel fuera de su navegación. Capturas: usuario-catalogo.png y usuario-favoritos.png.
Con la cuenta real de administrador, «Activar avisos» abrió el diálogo de Android. Se denegó el permiso y DummyJSON devolvió 16 productos. El panel confirmó el éxito y la lista de avisos activos de Android quedó sin una entrada de SmartCatalog. Al conceder temporalmente el permiso, una nueva sincronización publicó una sola entrada «Catálogo actualizado» con el texto «Se guardaron 16 productos correctamente».

El aviso se tocó desde la bandeja con SmartCatalog en segundo plano. La app volvió al panel administrativo tras validar la sesión y Android retiró el aviso. Luego se apagaron Wi-Fi y datos del emulador. La sincronización indicó falta de conexión y no publicó otro aviso. SQLite confirmó 16 productos, 16 IDs únicos y un favorito después de las pruebas. Se restauraron la conectividad, el permiso y la preferencia temporal de solicitud.

Capturas principales: solicitud-permiso.png, permiso-denegado.png, sincronizacion-correcta.png, bandeja-aviso.png, apertura-desde-aviso.png y sin-internet.png.

Capturas y resúmenes en `evidencias/h15/`. Registros de la ejecución y auditoría Git en `.h1-verification/h15/`.
