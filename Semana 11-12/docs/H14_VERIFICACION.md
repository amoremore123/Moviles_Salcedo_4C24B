# H14 — Panel de administrador de SmartCatalog

El panel reúne el estado del catálogo local y sus acciones de mantenimiento. Es el destino inicial de una sesión administrativa verificada. Su encabezado identifica el rol y ofrece acceso al catálogo y cierre de sesión.

## Cambios

| Archivo | Cambio |
| --- | --- |
| `ui/admin/PanelAdminScreen.kt` | Pantalla Material 3 con tarjetas, indicadores, estado de mantenimiento y diálogo de limpieza. |
| `ui/admin/PanelAdminViewModel.kt` | Observación de datos, estados de sincronización y control de permisos para cada acción. |
| `ui/admin/BienvenidaAdminScreen.kt` | Sustituida por el panel. |
| `ui/navigation/AppNav.kt` | Integra el panel en la ruta protegida y vuelve a la entrada existente desde el catálogo. |
| `AppContainer.kt` | Conecta el panel con el repositorio compartido y la caché de Coil. |
| `data/repository/ItemRepositoryImpl.kt` | Expone la hora de la última consulta exitosa del catálogo en el proceso. |
| `PanelAdminViewModelTest.kt` | Pruebas unitarias de estadísticas, estados, concurrencia, permisos y limpieza. |
| `PanelAdminTest.kt` | Pruebas instrumentadas con Room en memoria, API simulada y caché Coil real. |
| `docs/H14_VERIFICACION.md` | Informe y referencias a evidencias. |

Los archivos de producción se encuentran bajo `app/src/main/java/com/tecsup/smartcatalog/`. Las nuevas pruebas están en `app/src/test/` y `app/src/androidTest/`.

## Datos y acciones

El ViewModel deriva el total de productos y favoritos de la misma emisión del catálogo de Room. Los contadores se actualizan juntos al cambiar los registros. El estado de almacenamiento distingue carga, base disponible, catálogo vacío y fallo de lectura.

«Sincronizar catálogo» invoca el mismo repositorio Retrofit y Room del catálogo. Valida el lote antes de escribir y usa la transacción existente, que conserva los favoritos de los IDs almacenados. El panel evita enviar otra solicitud mientras está ocupado. El mutex del repositorio serializa también las solicitudes procedentes del catálogo. Un fallo de red muestra un aviso y deja disponibles los datos locales. La fecha muestra la última sincronización exitosa registrada por el repositorio compartido durante el proceso actual. Se reinicia al terminar el proceso.

«Limpiar caché» pide confirmación y vacía la caché de imágenes en memoria del ImageLoader de Coil. La caché en disco sigue deshabilitada. La acción no llama a las operaciones de borrado del repositorio ni toca Room, la sesión de Firebase o Firestore. Las imágenes se vuelven a cargar al necesitarlas.

La entrada al panel y sus acciones comprueban el rol de la sesión que H13 valida mediante Firestore. El cierre de sesión usa la navegación existente y descarta su pila. Los favoritos son locales y compartidos entre cuentas. Esta característica se indica en el panel.

## Pruebas y evidencias

La compilación de la app y el APK de pruebas terminó con `BUILD SUCCESSFUL`. Pasaron 122 pruebas unitarias, incluidas siete nuevas.

Se ejecutó la suite completa con AndroidJUnitRunner en el emulador Android 17 mediante ADB. De 41 casos, 40 pasaron y uno se omitió por requerir ausencia de google-services.json. Las cuatro pruebas nuevas comprobaron sincronización, contadores, cancelación y confirmación de limpieza, retirada efectiva de una imagen de Coil, conservación de Room y sesión, navegación y restricción según el rol.

La cuenta real de usuario recuperó su sesión, abrió el catálogo con 16 productos y conservó el favorito iPhone 5s. El acceso administrativo no apareció. Capturas: usuario-catalogo.png y usuario-favoritos.png. El sistema del emulador mostró un aviso de System UI al iniciar la aplicación. Después de elegir Wait se pudo verificar el catálogo.
La cuenta real de administrador abrió el panel con 16 productos y un favorito. La sincronización con DummyJSON recibió 16 productos y actualizó la hora del último éxito. Al desactivar Wi-Fi y datos del emulador, el intento mostró «Sin conexión» y conservó ambos contadores y la hora anterior. Se restauraron Wi-Fi y datos a su configuración inicial.

Se comprobó el diálogo, se canceló una vez y después se confirmó la limpieza. La sesión continuó activa y el favorito iPhone 5s siguió visible. Se navegó del panel al catálogo, a favoritos y de regreso al mismo panel. Cerrar sesión volvió al login y Atrás salió al lanzador. La consulta SQLite posterior confirmó 16 filas, 16 IDs distintos y un favorito.

Capturas principales: admin-panel.png, admin-sincronizado.png, admin-sin-red.png, admin-confirmacion.png, admin-cache-limpia.png, admin-catalogo.png, admin-favoritos.png y admin-logout.png.

Las evidencias locales están en `evidencias/h14/` y la auditoría de Git en `.h1-verification/h14/`.
