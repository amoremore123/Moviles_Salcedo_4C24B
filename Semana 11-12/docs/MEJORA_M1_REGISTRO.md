# M1 — Preparación local del registro

Desde el login se puede abrir el formulario de nombre, correo, contraseña y confirmación. En producción, **Validar formulario** solo comprueba los datos localmente. El botón permanece deshabilitado con campos vacíos. La pantalla indica que el alta aún no está disponible y las contraseñas permanecen en memoria de la pantalla, fuera de preferencias, registros y estado guardado.

## Flujo preparado

`RegistroViewModel` coordina validación, carga, error, envío del enlace y confirmación del perfil. `ServicioRegistroFirebase` está preparado para crear una cuenta de Auth, enviar el enlace, recargar el usuario y exigir `email_verified` en un token actualizado. El adaptador recibe el contrato `FinalizadorRegistro`. La app publicada todavía no proporciona una implementación de ese contrato ni instancia el adaptador.

Cuando Auth confirma la creación, el adaptador guarda el UID y el nombre en preferencias privadas antes de enviar el correo. Al reabrir la app con ese mismo usuario de Firebase, el flujo preparado recupera el formulario pendiente antes de verificar la sesión normal. El usuario puede reenviar el enlace o reintentar la confirmación sin recrear la cuenta. El marcador se elimina al confirmar el perfil. La recuperación requiere que Firebase conserve la sesión local y que la respuesta de creación haya llegado al dispositivo. Una respuesta perdida antes de guardar el marcador requiere recuperación adicional en la integración real.

## Función local

`functions/index.js` define la callable `finalizarRegistroSmartCatalog` con `enforceAppCheck: true`. El handler exige Auth, correo verificado y el App ID de la entrada Android `com.tecsup.smartcatalog`. El cliente solo envía `nombre`. El servidor toma UID y correo del token, fija `rol: "usuario"` y crea `usuarios/{uid}` en una transacción. Un reintento reconoce un perfil válido y preserva su contenido. Un perfil existente incompatible requiere revisión.

Las reglas de Firestore se mantienen como están. La función local usa Admin SDK y no requiere habilitar escrituras directas. Las pruebas Node usan un almacén controlado y contexto de callable simulado. Comprueban las decisiones del handler, no la validación de tokens por un servidor desplegado.

## Configuración futura en Firebase Console

1. Confirma el App ID de SmartCatalog en **Configuración del proyecto → Tus apps** de `mitecsup`. Revisa que **Authentication → Métodos de acceso** mantenga Correo electrónico/Contraseña y que el mensaje de verificación de **Plantillas** sea adecuado para el proyecto compartido.
2. En **App Check**, registra solo SmartCatalog con Play Integrity y la huella SHA-256 de su certificado. Ajusta los requisitos de integridad a la distribución del APK. Para pruebas aprobadas en emulador se puede usar el proveedor debug y registrar su token de forma privada.
3. Si se aprueba Blaze, vincula una cuenta de Cloud Billing a `mitecsup` desde **Uso y facturación**. Revisa el consumo de MyTecsup y configura alertas de presupuesto. Las alertas avisan del gasto y no lo bloquean.
4. En un paso separado, tras aprobación, instala dependencias del servidor, prueba el flujo en emuladores Firebase y configura la región y la identidad de ejecución con permisos mínimos sobre Firestore. Despliega exclusivamente `finalizarRegistroSmartCatalog`. La exigencia de App Check se configura en esa callable. Habilitarla para todo Auth o Firestore puede afectar clientes de MyTecsup.
5. Solo después de verificar el servidor, integra en Android el SDK de Functions y el proveedor de App Check, conecta `FinalizadorRegistro` a la callable y comprueba explícitamente que el resultado sea `creado` o `existente`. Prueba la recuperación preparada con Auth, correo y Firestore reales antes de habilitar el alta pública.

Blaze es de pago por uso. Vincular facturación permite cargos en el proyecto compartido. Hay cuotas sin costo, pero la ejecución, Firestore, Cloud Build, Artifact Registry y el tráfico pueden generar gastos según consumo y ubicación. El volumen de MyTecsup cuenta en las cuotas del proyecto. El proveedor de App Check también tiene límites de uso. [Precios](https://firebase.google.com/pricing/) · [App Check Android](https://firebase.google.com/docs/app-check/android/play-integrity-provider) · [App Check en callable](https://firebase.google.com/docs/app-check/cloud-functions)

## Verificación local

Gradle terminó con BUILD SUCCESSFUL. Pasaron 133 pruebas unitarias Android y cinco pruebas Node del handler. En el emulador se validó el envío del formulario inactivo con datos válidos, la ausencia de cambios de sesión y la recuperación de preferencias para el UID correcto. La captura del formulario vacío se guardó en `evidencias/mejoras/m1/formulario-registro.png`. El alta real, el envío de correo y la atestación de App Check en el servidor requieren un entorno de integración aprobado.
