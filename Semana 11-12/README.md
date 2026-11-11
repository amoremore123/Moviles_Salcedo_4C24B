# SmartCatalog

SmartCatalog es una aplicación Android con Kotlin para explorar smartphones. El objetivo del laboratorio de las semanas 11 y 12 de **Programación en Móviles** es integrar un catálogo remoto, almacenamiento local, autenticación y administración en una misma app.

## Qué hace la aplicación

- Inicia sesión con correo y contraseña y verifica el rol en Firestore.
- Presenta un catálogo de smartphones, un detalle por producto y favoritos persistentes.
- Guarda los productos en Room para consultar el catálogo y los detalles guardados si falla la red, dentro de una sesión ya validada.
- El administrador puede consultar contadores reales, sincronizar el catálogo y limpiar las imágenes temporales de Coil.
- La sincronización manual del panel puede emitir un aviso local tras guardar los productos.

## Tecnologías y arquitectura

| Tecnología | Uso |
| --- | --- |
| Kotlin, Jetpack Compose y Material 3 | Interfaz, estados y componentes visuales |
| ViewModel, coroutines y StateFlow | Coordinación de la UI, operaciones suspendidas y estados observables |
| Navigation Compose | Rutas de login, catálogo, favoritos, detalle y panel |
| Retrofit, OkHttp y Gson | Solicitudes y lectura de respuestas de DummyJSON |
| Room y SQLite | Productos y favoritos locales |
| Firebase Authentication y Cloud Firestore | Identidad, recuperación de sesión y lectura del perfil y rol |
| Coil | Imágenes de productos y caché en memoria |
| AndroidX NotificationCompat | Avisos locales y apertura segura de la app |

En MVVM, los composables muestran estados y envían acciones a los ViewModels. Estos usan repositorios para consultar la API, leer Room o validar la sesión. `AppContainer` conecta los componentes. El catálogo observa Room como fuente de datos. [Arquitectura y diagrama](docs/ARQUITECTURA.md) · [Base de datos](docs/BASE_DE_DATOS.md) · [API](docs/API.md) · [Avisos locales](docs/NOTIFICACIONES.md)

## Estructura

```text
Semana 11-12/
├── app/
│   ├── schemas/
│   └── src/
│       ├── main/
│       │   ├── java/com/tecsup/smartcatalog/
│       │   │   ├── data/
│       │   │   ├── domain/model/
│       │   │   └── ui/
│       │   ├── res/
│       │   └── AndroidManifest.xml
│       ├── test/
│       └── androidTest/
├── docs/
├── firestore.rules
└── README.md
```

## Configuración inicial y Firebase

Abre esta carpeta en Android Studio. Usa JDK 17 para Gradle, instala el SDK de compilación 36 y configura un dispositivo o emulador Android. El mínimo de ejecución es API 24 y el objetivo es API 36. Usa el Gradle Wrapper del proyecto.

El registro Android de Firebase usa el paquete `com.tecsup.smartcatalog`. Coloca el JSON descargado desde Firebase Console en `app/google-services.json` y habilita Email/Password. Cada cuenta necesita su perfil `usuarios/{uid}` con `correo` y `rol`. El JSON se mantiene local. [Configuración de Firebase](docs/FIREBASE_CONFIGURACION.md)

## Ejecución y roles

Desde la raíz del proyecto, en Windows:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:installDebug
```

También puedes usar **Run** en Android Studio. El usuario accede a catálogo, detalle y favoritos. El administrador tiene además el panel y sus operaciones de mantenimiento. Cerrar sesión vuelve al login y descarta las pantallas protegidas.

## Pruebas y evidencias

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebugAndroidTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

La verificación H16 obtuvo **BUILD SUCCESSFUL**, **130 pruebas unitarias aprobadas y 45 instrumentadas aprobadas**. Se omitió un caso que requiere ejecutar sin configuración Firebase. La suite instrumentada usa Room en memoria y proveedores controlados. Las pruebas de publicación y denegación requieren ejecutar con estados opuestos del permiso. El informe especifica el procedimiento usado y separa aprobadas, omitidas y verificaciones con cuentas reales. [Informe final H16](docs/H16_VERIFICACION.md)

Las capturas originales están en las carpetas de cada hito. La selección para la entrega se guarda localmente en `evidencias/final/`, excluida de Git. [Matriz de cumplimiento](docs/MATRIZ_CUMPLIMIENTO.md) · [Guía de exposición](docs/GUIA_EXPOSICION.md)

## Limitaciones

- Los favoritos se comparten entre cuentas del mismo dispositivo.
- La última hora de sincronización se conserva en memoria durante el proceso.
- Se necesita red para descargar productos o validar una sesión nueva o recuperada. Las imágenes pueden usar un marcador cuando no se pueden cargar.
- Los avisos dependen del permiso y la configuración de Android.
- El alcance se limita al catálogo, sin compras ni pagos.

## Conclusiones

1. Room permite consultar el catálogo guardado aunque falle la API.
2. Firebase identifica la cuenta y Firestore determina qué pantallas puede abrir.
3. Las pruebas ayudan a comprobar que sincronización, favoritos y avisos funcionan juntos.

## Mejoras adicionales

- **M1**. Formulario de registro y recuperación local preparados. El alta real queda pendiente de la integración y activación del servidor.
- **M2**. Bienvenida y rediseño de acceso, catálogo, detalle, favoritos y panel con Material 3.
- **M3**. Búsqueda, filtros combinados y orden sobre productos de Room, incluso si falla la conexión a la API.
- **M4**. Perfil de la sesión validada y temas claro, oscuro y del sistema. La edición de nombre permanece deshabilitada.

[Cambios, evidencias y verificación de las mejoras](docs/MEJORAS_ADICIONALES.md)
