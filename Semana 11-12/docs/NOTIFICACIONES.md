# Avisos locales

`NotificacionesCatalogo` usa NotificationCompat. En API 26 o superior crea de forma idempotente el canal `smartcatalog_catalog_updates`, «Actualizaciones del catálogo», con importancia predeterminada. En API 24 y 25 utiliza la prioridad compatible sin canal.

En Android 13 o superior, el panel explica el permiso `POST_NOTIFICATIONS`. **Activar avisos** abre el diálogo por acción del usuario y la app recuerda la solicitud. Si se bloquea el permiso, el botón de ajustes permite abrir la configuración del sistema. Android 12 y anteriores no solicitan ese permiso en tiempo de ejecución. Se respetan el bloqueo global y el del canal.

`PanelAdminViewModel` publica tras recibir el resultado de una sincronización manual exitosa que guardó un lote no vacío. El texto usa la cantidad confirmada y distingue singular y plural. Los fallos de red, API, almacenamiento, las cancelaciones y los lotes vacíos quedan sin aviso. Las actualizaciones fuera del panel tampoco generan avisos.

El ViewModel bloquea el doble envío mientras hay una operación en curso. La publicación forma parte de la operación y no se repite por recomposición. Una operación posterior puede publicar otra vez en el mismo ID de bandeja.

El PendingIntent explícito e inmutable abre `MainActivity` con una pila nueva. La app vuelve a validar la sesión y el rol antes de mostrar el destino correspondiente. El aviso se retira al tocarlo y solo muestra el estado y la cantidad de productos. [Pruebas del hito](H15_VERIFICACION.md)
