# H16 — Verificación y entrega final de SmartCatalog

Verificación del **8 de noviembre de 2026**, hora de Lima. El objetivo es dejar una guía reproducible de instalación, funcionamiento y exposición. La revisión se realizó sobre el código de H15 en main, commit `a4920e18980d147b11d2e811f9cf58d6643ca4f2`. H16 modifica documentación y organiza evidencias locales.

## Compilación y pruebas

Se volvió a ejecutar Gradle con JDK 17 y `--rerun-tasks` para `:app:assembleDebug`, `:app:testDebugUnitTest` y `:app:assembleDebugAndroidTest`. Gradle informó **BUILD SUCCESSFUL** y 79 tareas ejecutadas. La advertencia de símbolos de `libdatastore_shared_counter.so` indica que la biblioteca se empaquetó sin retirar sus símbolos. No impidió compilar ni ejecutar.

| Suite | Aprobadas | Fallos | Omitidas |
| --- | ---: | ---: | ---: |
| Unitarias, informes XML de Gradle | 130 | 0 | 0 |
| Instrumentadas en Android 17, casos únicos | 45 | 0 | 1 |

La instrumentación se ejecutó mediante ADB sobre los APK recién construidos. En la primera ejecución sin permiso pasaron 44 casos y se omitieron dos. El caso que necesitaba permiso se ejecutó por separado y pasó. Se restauró el permiso inicial. La única omisión definitiva comprueba la variante sin `google-services.json`, que no corresponde a la instalación real de esta entrega. Las pruebas instrumentadas usan cuentas, API y base controladas.

## APK real en emulador

La compilación instalada recuperó la sesión real de administrador tras detener y reabrir el proceso. El panel mostró 16 productos y un favorito. DummyJSON devolvió 16 productos y la notificación de Android mostró esa cantidad. Al tocarla desde la bandeja, se volvió a validar la sesión y se abrió el panel. La limpieza confirmada conservó el rol y ambos contadores. Con Wi-Fi y datos desactivados, la actualización falló de forma controlada. Catálogo, favorito y detalle local siguieron disponibles. Se restauró la red. Cerrar sesión desde el detalle regresó al login. También se repitió el login manual de administrador en H16. El panel abrió con 16 productos y un favorito y el cierre volvió al login. El usuario ingresó manualmente con la cuenta normal. La app mostró el catálogo sin Administración, el favorito iPhone 5s y su detalle remoto ID 121. Al reiniciar el proceso recuperó el rol Usuario, los 16 productos y el favorito. Cerrar sesión volvió al login y Atrás salió al lanzador. La consulta SQLite final confirmó 16 filas, 16 IDs distintos y un favorito. La red y el permiso de notificaciones se restauraron.

## Revisión de seguridad y alcance

Se inspeccionaron el manifiesto, dependencias, repositorios, UI, modelos, DAOs y pruebas. El código de producción no contiene contraseñas literales, patrones de claves privadas o tokens ni llamadas que registren contraseñas. El JSON está ignorado. Una búsqueda acotada en Logcat de FirebaseAuth y NotificacionesCatalogo no encontró valores explícitos con etiquetas de contraseña o token. El rol viene del documento Firestore y controla la ruta y las acciones del administrador. El aviso abre la actividad de entrada y revalida la sesión. La limpieza usa la caché en memoria de Coil.

Se encontró la guía original del Laboratorio 11–12. La [matriz](MATRIZ_CUMPLIMIENTO.md) recoge los requisitos R01 a R13 y PA-01 a PA-14. Identifica las diferencias entre el alcance actual y la guía, incluida la limpieza que conserva Room y la notificación que solo se emite en la sincronización manual administrativa. La publicación y los entregables remotos no se verificaron en esta entrega local.

## Evidencias y limitaciones

La selección final contiene 32 capturas auténticas y 39 archivos en total, incluido el índice. Las capturas y registros resumidos se organizan en `evidencias/final/`, con un índice de origen. Las copias históricas conservan el prefijo del hito y los archivos originales permanecen en su ubicación. El JSON, las credenciales y las evidencias quedan fuera del commit.

Room conserva favoritos compartidos entre cuentas. La hora de sincronización dura el proceso. Se necesita red para actualizar la API y validar el acceso inicial. Los avisos respetan Android. No hay compras ni pagos. Las reglas completas desplegadas en Firestore no se inspeccionaron. El archivo del repositorio documenta la política esperada.

## Conclusiones

1. Los resultados reproducidos distinguen la suite controlada del uso de los servicios reales.
2. Room, los roles y los avisos se pueden explicar y demostrar a partir de los flujos documentados.
3. La matriz indica qué requisitos se verificaron y qué diferencias debe conocer quien evalúe la entrega.
