# M4 — Perfil y tema

El perfil se abre desde el catálogo o el panel de administración. La ruta requiere una sesión validada. Muestra el correo de Firebase Authentication, el rol validado en Firestore y el campo opcional `nombre` del documento `usuarios/{uid}`. Si el documento previo carece de nombre, muestra «Sin nombre registrado».

El botón «Editar nombre» está deshabilitado porque las reglas actuales bloquean las escrituras desde la app. No se añadieron escrituras, permisos ni controles para modificar el rol.

Claro, Oscuro y Seguir sistema se aplican al instante a la app y a los iconos de las barras del sistema. La opción se guarda en preferencias privadas del dispositivo y se conserva al reabrir la aplicación o cambiar de cuenta. Seguir sistema es la opción predeterminada.

## Evidencias

`evidencias/mejoras/m4/` contiene el perfil claro, oscuro y con la opción Seguir sistema, además del panel oscuro. Se usaron identidades de prueba en el emulador. La lectura desde Firestore sigue el flujo de validación de sesión existente.

## Verificación

Gradle terminó con BUILD SUCCESSFUL y pasaron 140 pruebas unitarias. La suite instrumentada completa aprobó 48 casos con el permiso de notificaciones denegado. Se repitió y aprobó el caso que requiere el permiso concedido, y se restauró el estado previo. En total pasaron 49 casos distintos. Solo quedó omitida la prueba de la variante sin `google-services.json`.

La prueba de perfil verificó el acceso desde el catálogo y el panel, los datos de cada sesión, las tres opciones de tema, la conservación del modo oscuro al volver a abrir la actividad, la lectura del valor guardado desde otra instancia de preferencias y el cierre de sesión. Los casos de notificaciones comprobaron la sincronización con y sin permiso y el respeto del rol al abrir el aviso.
