# Firebase para SmartCatalog — configuración real pendiente

No se encontró `app/google-services.json` para SmartCatalog y no hay una conexión de Firebase Console o CLI autorizada disponible en esta sesión. Esto no demuestra que no exista un proyecto en la nube: su existencia no pudo verificarse. No se creó ningún proyecto, cuenta ni documento remoto, ni se publicaron reglas.

El código y las dependencias quedan preparados para Email/Password y Firestore. Sin configuración, el catálogo sigue abierto y `AuthRepository` devuelve un error controlado al intentar iniciar sesión. No hay pantalla de login ni registro público en la app.

## Pasos que debes realizar en Firebase Console

1. Abre [Firebase Console](https://console.firebase.google.com/) con la cuenta que administrará este laboratorio. Selecciona un proyecto dedicado a SmartCatalog, si ya lo tienes, o crea uno. No reutilices la configuración de MyTecsup, AyudaCampus ni de otra aplicación. No es necesario habilitar Analytics para este hito.
2. En Configuración del proyecto, agrega una aplicación **Android** con el paquete exacto **`com.tecsup.smartcatalog`** y el nombre visible SmartCatalog. Para este flujo básico de correo/contraseña no se está implementando Google Sign-In ni autenticación por teléfono.
3. Descarga el `google-services.json` que Firebase genere para esa app y colócalo en **`Semana 11-12/app/google-services.json`**. No escribas el archivo a mano ni cambies sus identificadores. Está excluido por `.gitignore`; no uses `git add -f` para incluirlo.
4. En **Authentication → Sign-in method / Proveedores**, habilita **Email/Password**. No se implementa enlace por correo ni otros proveedores en H12.
5. En **Authentication → Users / Usuarios → Add user**, crea las **dos cuentas reales que elijas para el laboratorio**, una destinada a admin y otra a usuario. Elige sus correos y contraseñas personalmente; no los guardes en README, código, pruebas, commits ni capturas. Esta guía no propone ni inventa credenciales. Copia únicamente el **UID** de cada cuenta para el siguiente paso.
6. En **Firestore Database**, crea una base de datos nativa de edición Standard para este proyecto, en **modo producción**, y elige su ubicación. Usa la base `(default)` que utiliza esta implementación. Si ya existe para SmartCatalog, conserva los datos y revisa sus reglas antes de publicar.
7. En **Firestore → Rules / Reglas**, revisa y publica el contenido de `firestore.rules` de este proyecto. Las reglas son para SmartCatalog; no reemplaces reglas de otro laboratorio o proyecto.
8. En **Firestore → Data / Datos**, crea la colección **`usuarios`**. Crea dos documentos utilizando como ID el **UID exacto copiado de Authentication**, sin generar IDs automáticos:

| Documento | Campo | Tipo | Valor que debes ingresar |
| --- | --- | --- | --- |
| `usuarios/{UID de la cuenta admin}` | `correo` | string | Correo exacto de esa cuenta en Authentication |
| Mismo documento | `rol` | string | `admin` |
| `usuarios/{UID de la cuenta usuario}` | `correo` | string | Correo exacto de esa cuenta en Authentication |
| Mismo documento | `rol` | string | `usuario` |

No incluyas contraseñas en Firestore. Los nombres de campos y los roles son sensibles a su escritura: solo se aceptan `admin` y `usuario`, sin variantes ni valores por defecto. El repositorio comprueba además que el correo del perfil coincida con la identidad autenticada. El rol no se obtiene mediante una lista de correos en el código.

9. Sincroniza Gradle y recompila. La presencia del archivo real habilita el plugin Google Services y genera `BuildConfig.FIREBASE_CONFIGURADO=true`. El plugin debe aceptar el cliente Android para `com.tecsup.smartcatalog`; un archivo de otro paquete produce un error de configuración en lugar de utilizarse silenciosamente.

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
git check-ignore app/google-services.json
```

Ejecuta esos comandos desde la raíz de SmartCatalog. El archivo de configuración solo se consume localmente, nunca debe formar parte del commit.

## Política de roles y reglas

El archivo `firestore.rules` permite únicamente `get` del perfil propio a un usuario autenticado cuyo UID coincida con el ID del documento. Deniega enumerar la colección, crear, actualizar y eliminar perfiles desde **cualquier cliente**, incluso cuando su perfil diga admin. También deniega por defecto el resto de colecciones y subcolecciones.

La preparación y edición de cuentas y roles se hace desde Firebase Console con los permisos administrativos del proyecto, o desde un servidor administrativo confiable. No se incluye un SDK Admin ni una cuenta de servicio en Android. Un eventual usuario de Auth sin perfil autorizado no recibe sesión de aplicación ni un rol por defecto.

El repositorio no implementa `createUserWithEmailAndPassword`. No se ofrecen registro público ni escritura de perfiles desde la app. La ausencia de ese flujo en Android no sustituye las reglas de Firestore: son ellas las que impiden autoasignarse permisos, aunque alguien use un cliente diferente.

Para revisar las reglas con el simulador de Firebase Console, comprueba:

| Operación | Identidad | Resultado esperado |
| --- | --- | --- |
| Obtener perfil propio | UID autenticado igual al documento | Permitido |
| Obtener perfil ajeno | UID autenticado diferente | Denegado |
| Obtener un perfil | Sin autenticación | Denegado |
| Listar `usuarios` | Cualquier cliente | Denegado |
| Crear, editar o eliminar un perfil/rol | Cualquier cliente, incluido admin | Denegado |

No se afirma haber ejecutado estas comprobaciones contra Firebase Console ni un emulador Firebase: en esta sesión las reglas se prepararon y revisaron, pero no se publicaron.

## Uso de la infraestructura

`AppContainer.authRepository` se crea de forma perezosa. Con configuración real utiliza `AuthRepositoryFirebase`, `FirebaseAuth` y `FirebaseFirestore`; sin ella utiliza una implementación segura que informa configuración pendiente. El repositorio de productos con Room y Retrofit y el flujo de navegación no cambian.

El inicio de sesión autentica primero y después obtiene `usuarios/{uid}` con `Source.SERVER`. La consulta de sesión existente refresca el token de Auth y vuelve a verificar el perfil. Si no puede verificar el servidor, el documento no existe o el rol/correo es inválido, no publica una sesión autorizada y cierra Auth. No se concede admin desde una copia local antigua. Las contraseñas solo se pasan al SDK durante el inicio de sesión y no se almacenan en Room ni en propiedades del repositorio.

Las pruebas automatizadas usan dobles con valores explícitamente simulados del dominio reservado `example.invalid`; no son cuentas reales ni credenciales para esta configuración. La conexión real, los dos inicios de sesión y la lectura efectiva de sus perfiles quedan pendientes hasta completar los pasos anteriores. No se deben incorporar contraseñas reales a las pruebas para realizar esa verificación.

## Referencias oficiales

- [Configurar Firebase en Android](https://firebase.google.com/docs/android/setup).
- [Notas de versiones y BoM](https://firebase.google.com/support/release-notes/android).
- [Google Services y archivo JSON](https://firebase.google.com/docs/android/google-services-plugin-and-file).
- [Autenticación Email/Password](https://firebase.google.com/docs/auth/android/password-auth).
- [Reglas con identidad autenticada](https://firebase.google.com/docs/firestore/security/rules-conditions).
- [Origen SERVER de Firestore](https://firebase.google.com/docs/reference/android/com/google/firebase/firestore/Source).
