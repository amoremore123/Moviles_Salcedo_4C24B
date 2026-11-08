# H12 — Firebase Auth, Firestore y roles

## Estado de configuración

**No hay configuración Firebase real verificada.** No existe `app/google-services.json` en esta copia de SmartCatalog; no se dispone de conector ni CLI Firebase configurada para comprobar proyectos. No se crearon cuentas, documentos ni recursos remotos y no se publicaron reglas. Las instrucciones exactas de intervención están en `FIREBASE_CONFIGURACION.md`.

Sin ese archivo la integración permanece preparada, pero no se afirma que Firebase funcione en la nube. El catálogo público con Room y Retrofit y su navegación permanecen intactos.

## Dependencias y compilación sin configuración ficticia

- Firebase BoM **34.18.0**.
- Firebase Authentication, módulo principal **firebase-auth**, versión administrada por BoM **24.2.0**.
- Cloud Firestore, módulo principal **firebase-firestore**, versión administrada por BoM **26.6.0**.
- Google Services Gradle Plugin **4.5.0**.
- Adaptador de tareas `kotlinx-coroutines-play-services` **1.7.3**, compatible con la línea de coroutines existente. No es un módulo Firebase KTX.

Se conservan Kotlin **2.2.10**, AGP **8.13.2**, Gradle **8.13**, JDK **17**, Room **2.7.2**, KSP **2.2.10-2.0.2**, SDK de compilación/objetivo **36** y mínimo **24**. Se verificaron las versiones Firebase en la documentación oficial y en el BoM disponible en caché. La verificación de compilación comprueba la compatibilidad efectiva.

El plugin Google Services está declarado y se aplica únicamente si existe el archivo real en `app/`. Su presencia genera `BuildConfig.FIREBASE_CONFIGURADO=true`; sin él es false y no hay una tarea que exija un JSON inventado. Un archivo real debe corresponder a `com.tecsup.smartcatalog`; el procesamiento del plugin valida el cliente Android. El archivo está excluido de Git por la regla ya existente, que no se modificó.

## Autenticación y roles

`AuthRepositoryFirebase` implementa el contrato existente `AuthRepository`. Su constructor de producción utiliza `FirebaseAuth` y `FirebaseFirestore`; los proveedores pequeños permiten probar la política sin SDK activo, Internet ni cuentas reales.

El inicio de sesión llama a Email/Password y después consulta `usuarios/{uid}` mediante `Source.SERVER`. Solo se aceptan campos de tipo String y roles exactamente `admin` o `usuario`. El correo del perfil debe coincidir con la identidad autenticada. No se determina el rol mediante un correo fijo, no hay rol por defecto y no se escribe ni crea el perfil desde el cliente.

La sesión actual comprueba la identidad, fuerza la renovación del token de Auth y consulta el perfil en el servidor. Un perfil inexistente, inválido o no verificable no publica una sesión y provoca cierre de Auth. No se recupera admin desde una copia local antigua. Se serializan las operaciones y un contador invalida un login pendiente si se cierra sesión mientras se verifica el rol.

Los errores públicos utilizan categorías y mensajes seguros, sin conservar mensajes internos ni causas del SDK. Las contraseñas solo se pasan temporalmente al SDK durante la llamada; no se guardan en Room, propiedades del repositorio, perfiles, registros ni archivos de configuración. No se implementa registro público en la app.

`AppContainer.authRepository` se crea de forma perezosa. Cuando no hay configuración, devuelve `AuthRepositorySinConfigurar`: login falla de forma controlada, sesión actual es null y cerrar sesión no provoca errores. No se invoca FirebaseAuth.getInstance sin una FirebaseApp válida ni se altera la entrada al catálogo.

## Reglas preparadas

`firestore.rules` permite únicamente obtener el documento propio a un usuario autenticado. Deniega listar usuarios y todas las escrituras de perfil/rol desde clientes, incluidos clientes con rol admin; las cuentas y perfiles se preparan desde Console o un entorno administrativo confiable. El resto de colecciones y subcolecciones queda denegado por defecto.

El archivo se preparó y revisó, pero **no se desplegó ni se validó contra un proyecto o emulador Firebase**. La guía incluye publicación en Console y casos para su simulador de reglas. La colección y sus dos documentos también están pendientes en la nube.

## Archivos

Creados:

- `app/src/main/java/com/tecsup/smartcatalog/data/auth/ProveedoresAutenticacion.kt`.
- `app/src/main/java/com/tecsup/smartcatalog/data/auth/ErrorAutenticacion.kt`.
- `app/src/main/java/com/tecsup/smartcatalog/data/auth/ProveedoresFirebaseSdk.kt`.
- `app/src/main/java/com/tecsup/smartcatalog/data/auth/FabricaAuthFirebase.kt`.
- `app/src/main/java/com/tecsup/smartcatalog/data/repository/AuthRepositoryFirebase.kt`.
- `app/src/test/java/com/tecsup/smartcatalog/data/repository/AuthRepositoryFirebaseTest.kt`.
- `app/src/androidTest/java/com/tecsup/smartcatalog/FirebaseSinConfigurarTest.kt`.
- `firestore.rules`.
- `docs/FIREBASE_CONFIGURACION.md`.
- Este informe.

Modificados: `build.gradle.kts`, `app/build.gradle.kts`, `AppContainer.kt` y README. El contrato AuthRepository, el modelo Rol/Sesion, el repositorio de productos, las pantallas, la navegación y las pruebas anteriores permanecen sin cambios de fuente.

## Resultados técnicos y ejecución

Se ejecutó `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:connectedDebugAndroidTest --console=plain` con JDK 17: **BUILD SUCCESSFUL en 9m 14s**, 79 tareas (43 ejecutadas, 36 actualizadas).

La primera selección de BoM 35.1.0 no compiló: Auth 25.0.0 contiene metadatos Kotlin 2.4 incompatibles con Kotlin 2.2.10. Se cambió únicamente la BoM a 34.18.0 y se repitió la verificación completa con éxito; Kotlin, Gradle, AGP, Room y KSP permanecieron iguales. No se omitieron comprobaciones de metadatos.

- **91 pruebas JVM**, sin fallos, errores ni omisiones: se conservan las 76 anteriores y se agregan 15 de autenticación/roles con proveedores simulados.
- **30 pruebas instrumentadas**, sin fallos, errores ni omisiones: se conservan las 29 anteriores y se agrega una que comprueba la respuesta controlada sin configuración y que no se inicializa una FirebaseApp ficticia.
- APK debug real instalado en Medium_Phone, Android 17, emulador sin ventana. Arranque frío correcto; catálogo con **16 modelos**; iPhone 5s marcado como favorito; pantalla de favoritos y detalle **ID 121** accesibles.
- Tras detener y volver a abrir el proceso se conservaron los 16 modelos y el favorito. Consulta de la base Room: **16 filas, 16 IDs distintos, 1 favorito**.
- No hubo excepciones fatales del proceso SmartCatalog en AndroidRuntime durante la comprobación. Al inicio el sistema del emulador mostró un ANR de **System UI**; se eligió Wait y la app funcionó normalmente. El emulador iniciado para esta verificación se cerró al finalizar.

Evidencias reales locales (excluidas de Git): `evidencias/h12/catalogo.png`, `catalogo-favorito.png`, `favoritos.png`, `detalle.png` y `catalogo-reinicio.png`. Logs, XML, resúmenes de pruebas, consulta de Room y estado inicial se guardaron en `.h1-verification/h12/`, también excluido.

**Pendiente de intervención real:** proyecto/app de Firebase, JSON propio, habilitar Email/Password, crear las dos cuentas elegidas por el usuario, sus perfiles `usuarios/{uid}` y publicar las reglas. No se verificaron inicio de sesión, permisos ni conexión contra la nube; las pruebas de política usan dobles y las reglas fueron revisadas localmente, sin ejecución en Firebase Rules Simulator ni despliegue. Los pasos y la matriz de comprobación están en `FIREBASE_CONFIGURACION.md`.

## Alcance y referencias

No se implementan login UI, navegación obligatoria por sesión, panel Admin, sincronización administrativa ni notificaciones. No se avanza a H13 ni se hace push.

Referencias oficiales: [Firebase Android y requisitos](https://firebase.google.com/docs/android/setup), [BoM y módulos principales vigentes](https://firebase.google.com/support/release-notes/android), [Google Services](https://firebase.google.com/docs/android/google-services-plugin-and-file), [Email/Password](https://firebase.google.com/docs/auth/android/password-auth), [reglas de Firestore](https://firebase.google.com/docs/firestore/security/rules-conditions) y [Source.SERVER](https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/Source).
