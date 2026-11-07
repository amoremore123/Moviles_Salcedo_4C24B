# H8 — Room: entidad, DAO y pruebas

## Alcance

Se prepara la persistencia local sin conectar Room al catálogo ni al detalle. `ItemRepositoryRemoto`, Retrofit, navegación, pantallas y el modelo de dominio permanecen sin cambios. `AppContainer` expone `database` e `itemDao` mediante `lazy`, utilizando el contexto de aplicación.

## Configuración

- Room Runtime, KTX, Compiler y Testing: **2.7.2**.
- KSP: **2.2.10-2.0.2**, con KSP2 habilitado. Su [release oficial](https://github.com/google/ksp/releases/tag/2.2.10-2.0.2) declara Kotlin 2.2.10.
- Se conservan Kotlin **2.2.10**, AGP **8.13.2**, Gradle **8.13**, JDK **17**, compile/target SDK **36** y min SDK **24**.
- Base `smartcatalog.db`, versión **1**, `exportSchema = true`. El esquema generado se versiona en `app/schemas/com.tecsup.smartcatalog.data.local.AppDatabase/1.json` para preparar futuras migraciones.
- KSP generó `AppDatabase_Impl.kt` e `ItemDao_Impl.kt`. Esas clases generadas permanecen en `build/`, fuera de Git.
- Sin consultas en el hilo principal, borrado al iniciar ni migración destructiva automática.

## Modelo y operaciones

`ItemEntity`, tabla `items`, contiene `id: Int` como clave primaria, `nombre`, `descripcion`, `imagen`, `categoria` como `String`, `precio: Double` y `favorito: Boolean = false`.

`ItemDao` implementa Flow de todos los productos y de favoritos, consulta por ID, inserción/actualización transaccional, cambio de favorito, conteos y eliminación explícita de todos los registros. Las consultas de listas ordenan por ID.

La inserción ignora IDs existentes y después actualiza únicamente sus datos descriptivos: nunca sobrescribe su favorito. Se comprueban tanto favorito verdadero como falso. Los registros nuevos conservan el valor recibido. No hay sincronización ni borrado/reinserción de catálogo en H8.

Los mappers locales son funciones puras que conservan los siete campos en ambos sentidos. Las conversiones remotas existentes permanecen intactas.

## Verificación realizada

Comando ejecutado desde la raíz del proyecto:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --console=plain
```

Resultado: **BUILD SUCCESSFUL**, 73 tareas ejecutadas, 8 min 34 s.

- **43 pruebas unitarias aprobadas**, sin fallos, errores ni omitidas: las **40 anteriores** y **3 nuevas** de mappers locales.
- **10 pruebas instrumentadas de Room aprobadas**, sin fallos, errores ni omitidas, en `Medium_Phone`, Android 17, `emulator-5554`.
- Cada prueba de Room utiliza una base independiente **en memoria**, cerrada al finalizar, sin Internet ni base persistente del usuario.
- Casos: inserción individual y múltiple, orden por ID, consulta existente/ausente, cambio de favorito, filtro de favoritos, conteos, borrado, emisiones sucesivas del Flow y conservación del favorito en actualizaciones por ID.
- APK debug instalado y ejecutado en el emulador headless. Se observaron **16 smartphones reales de DummyJSON**, se abrió el detalle de **iPhone 5s, ID 121**, y se volvió al catálogo.
- Tras las pruebas y navegación, no existe `smartcatalog.db` en el almacenamiento privado de la aplicación: la UI sigue usando el repositorio remoto y no inicializa Room anticipadamente.

Los reportes de Gradle quedan en `app/build/test-results/testDebugUnitTest/` y `app/build/outputs/androidTest-results/connected/debug/`. Los registros de auditoría quedan en `.h1-verification/h8/` y las capturas reales en `evidencias/h8/`; estas carpetas están excluidas de Git.

## Límite del hito

No se implementan fuente de verdad Room, sincronización Retrofit–Room, modo offline persistente, favoritos en UI, Firebase ni otras funciones de H9. No se hace push.
