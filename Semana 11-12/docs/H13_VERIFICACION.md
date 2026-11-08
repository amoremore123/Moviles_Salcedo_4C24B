# H13 — login, sesión y navegación por roles

## Implementación y arquitectura

Se reutilizan `AuthRepository`, `AuthRepositoryFirebase`, los proveedores Firebase de H12 y los modelos `Sesion` y `Rol`. No se modifican las reglas, la configuración JSON, el repositorio de productos, el esquema de Room ni sus operaciones. No hay registro público ni funciones administrativas de H14.

`SesionViewModel` expone StateFlow con estados separados de verificación inicial, ausencia de sesión, login en curso, error, usuario y administrador. Al crearse llama a `AuthRepository.sesionActual()`: Firebase recupera su identidad persistida, renueva el token y el repositorio consulta de nuevo `usuarios/{uid}` con `Source.SERVER`. Mientras tanto solo se compone la pantalla de carga. Una sesión ausente o no validada no puede componer catálogo, favoritos, detalle ni administración.

El login Material 3 valida campos vacíos y formato básico del correo antes de llamar al repositorio. Tiene contraseña oculta con opción mostrar/ocultar, indicador de carga, prevención de doble envío y mensajes seguros de credenciales, red o perfil. El correo se recorta; la contraseña no se transforma. No hay llamadas de Firebase en los composables. La contraseña solo permanece temporalmente en el campo de Compose y en la llamada de autenticación: no usa rememberSaveable, SavedStateHandle, Room, DataStore ni SharedPreferences.

El repositorio H12 autentica Email/Password, obtiene el UID real y valida el perfil y rol remotos. Rechaza documentos ausentes, campos incorrectos, correos de perfil que no corresponden a Auth y roles distintos de `admin` o `usuario`. No concede un rol por defecto ni utiliza direcciones fijas para permisos. Si el perfil no puede verificarse no se concede acceso.

## Navegación y cierre de sesión

- Usuario: destino inicial catálogo; puede consultar detalle y gestionar favoritos.
- Administrador: bienvenida sencilla con acceso al catálogo y retorno a Administración. No hay edición, sincronización, panel completo ni notificaciones.
- `AccesoRutas` valida el rol al solicitar una navegación; `DestinoConPermiso` vuelve a validarlo antes de componer cada ruta, incluida una entrada administrativa invocada directamente.
- El NavController protegido existe solo dentro del estado autenticado y se identifica por UID y rol. Al cerrar sesión se llama al repositorio (FirebaseAuth.signOut), se invalida cualquier respuesta pendiente, se limpian sesión/rol y se descarta el NavController con su backstack. Atrás desde login no recupera las pantallas previas.
- La actividad conserva los ViewModels al rotar. Una nueva actividad/proceso crea el ViewModel de sesión y vuelve a verificar la identidad/rol mediante el repositorio, sin restaurar permisos desde el Bundle.
- Se conserva Room y los favoritos al cerrar sesión. La UI indica que son favoritos locales compartidos entre cuentas; no son una colección privada de cada usuario.
- La validación inicial requiere poder verificar el servidor. Un arranque sin red no concede acceso mediante un rol guardado. Los productos/favoritos siguen en Room y pueden usarse dentro de una sesión ya validada; no se elimina la política de seguridad para simular un login offline.

## Archivos de H13

Creados:

- `app/src/main/java/com/tecsup/smartcatalog/ui/auth/EstadoSesion.kt`
- `app/src/main/java/com/tecsup/smartcatalog/ui/auth/SesionViewModel.kt`
- `app/src/main/java/com/tecsup/smartcatalog/ui/auth/LoginScreen.kt`
- `app/src/main/java/com/tecsup/smartcatalog/ui/admin/BienvenidaAdminScreen.kt`
- `app/src/main/java/com/tecsup/smartcatalog/ui/navigation/AccesoRutas.kt`
- `app/src/test/java/com/tecsup/smartcatalog/ui/auth/SesionViewModelTest.kt`
- `app/src/test/java/com/tecsup/smartcatalog/ui/navigation/AccesoRutasTest.kt`
- `app/src/androidTest/java/com/tecsup/smartcatalog/testing/AuthDePrueba.kt`
- `app/src/androidTest/java/com/tecsup/smartcatalog/SesionNavegacionTest.kt`
- Este informe.

Modificados:

- `MainActivity.kt`: ViewModel de sesión y creación del ViewModel de catálogo solo al acceder al catálogo validado.
- `AppContainer.kt`: fábrica de sesión e inyección del contrato AuthRepository para pruebas; producción conserva la fábrica Firebase H12.
- `ui/navigation/AppNav.kt`: login, carga, destinos por rol, guardas y cierre de sesión.
- `ui/favoritos/FavoritosScreen.kt`: aclaración de que los favoritos son locales y compartidos.
- `app/build.gradle.kts`: dependencia de pruebas Compose UI con la misma BoM 2023.10.01 que la app y Espresso 3.7.0 solo para compatibilidad de pruebas con Android 17.
- `androidTest/.../testing/SmartCatalogTestApplication.kt`: proveedor Auth simulado solo en el APK de pruebas, junto a Retrofit simulado y Room en memoria ya existentes. La prueba anterior de rotación se conserva sin cambios.

## Compilación, pruebas y problemas resueltos

La compilación inicial y las 115 pruebas unitarias terminaron con BUILD SUCCESSFUL. Se conservan las 91 anteriores y se agregan 20 del ViewModel de sesión y 4 de permisos de rutas. Cubren login usuario/admin, estados pendientes, validaciones, credenciales/perfil/rol/red, mensajes seguros, recuperación, logout, cancelaciones, doble envío y autorización de destinos.

La primera preparación de pruebas UI detectó que la BoM de la app no aplicaba automáticamente a la configuración androidTest. Se agregó la misma BoM al APK de pruebas, sin actualizar versiones. La comprobación Atrás se implementó mediante el dispatcher real de la actividad. El Espresso transitivo de Compose falló en Android 17 con NoSuchMethodException para InputManager.getInstance; se declaró Espresso Core 3.7.0, ya disponible en caché, únicamente en androidTest. No se actualizaron Kotlin, Compose de producción, Gradle, AGP, Room, KSP ni Firebase.

La suite instrumentada finalizó con BUILD SUCCESSFUL en 3 min 15 s. El informe XML contiene 37 casos: **36 aprobados, 0 fallos, 0 errores y 1 omitido**. Se conservaron las 29 pruebas previas y se agregaron 7 de UI/navegación. La prueba H12 sin configuración se omite intencionalmente porque existe google-services.json. El contador de progreso de Gradle no se usa como total; se utiliza el XML de resultados.

## Verificación visual y Firebase real

APK debug real instalado y abierto: login visible, sin mostrar catálogo antes de autenticar. Se comprobaron visualmente nombre, bienvenida, campos, opción mostrar contraseña, botón y validación de campos vacíos. Capturas reales: evidencias/h13/login-real.png y login-validacion.png. Logcat no registró un cierre fatal de SmartCatalog durante esa comprobación. Los flujos usuario/admin, detalle, favoritos, restauración y logout aprobaron en pruebas instrumentadas con proveedores simulados. La cuenta real de administrador se comprobó tras la entrada manual del usuario: bienvenida administrativa, catálogo con 16 modelos, guardado local de un favorito y detalle del iPhone 5s (ID 121). Después de detener y reiniciar el proceso se observó primero la verificación de sesión y luego la bienvenida administrativa sin volver a introducir credenciales. Cerrar sesión desde el detalle regresó al login; Atrás salió al lanzador y al reabrir la app volvió a aparecer el login. En la primera prueba manual con la cuenta usuario, el APK mostró «La cuenta no tiene un perfil autorizado» y mantuvo la pantalla de login. El repositorio usa ese mensaje cuando el perfil falta o sus datos no coinciden con la identidad autenticada. Tras revisar el perfil en Firebase Console, el usuario repitió el ingreso manual con éxito. El APK mostró el rol Usuario, el catálogo de 16 modelos y el favorito local guardado con la cuenta administradora. El acceso administrativo no apareció. Un reinicio del proceso recuperó el catálogo con el rol Usuario sin volver a introducir la contraseña. No se identificó la causa exacta del primer rechazo. Capturas: usuario-real.png, usuario-favoritos.png y usuario-restaurado.png.

Los proveedores simulados usan exclusivamente valores reservados `example.invalid` y cadenas explícitas de prueba. No representan las cuentas reales del laboratorio ni sus contraseñas. Las credenciales reales solo pueden introducirse manualmente en el emulador; no se solicitan en el chat ni se incorporan a archivos.

## Integridad de Git

El estado inicial se registró en `.h1-verification/h13/before.json`, incluyendo hashes de archivos, JSON e índice normal. Los 447 archivos ajenos y las cuatro entradas preparadas se conservan. El commit se preparará con índice temporal independiente desde HEAD y rutas explícitas exclusivas de H13. Las evidencias de `evidencias/h13/`, la auditoría, el JSON y los secretos permanecen excluidos.

Commit previsto tras completar las comprobaciones posibles: `feat: implementar login y sesion con Firebase`, con autor y committer `amoremore123 <amoresalcmace@gmail.com>`. No se incluye el JSON ni las evidencias. La lista exacta y la verificación del commit se guardan en la auditoría local.

Sin push ni avance a H14. No se usa Chrome ni control de navegador.