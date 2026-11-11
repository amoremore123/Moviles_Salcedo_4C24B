# Guía de exposición de SmartCatalog

## Presentación de cinco minutos

1. **Contexto.** SmartCatalog reúne smartphones de DummyJSON en un catálogo Android. Permite consultar detalles y conservar favoritos cuando la conexión falla. El objetivo es integrar datos remotos, persistencia y acceso por roles.
2. **Arquitectura.** Muestra el diagrama de MVVM. Compose dibuja, el ViewModel coordina y el repositorio conecta Retrofit con Room. Firebase Authentication identifica la cuenta y Firestore entrega el rol.
3. **Usuario.** Inicia sesión en el emulador, abre un producto, toca el corazón y revisa Mis favoritos. Cierra y reabre con conexión para mostrar que sesión y datos se recuperan. Introduce la contraseña fuera de la proyección o mantén el campo oculto.
4. **Sin conexión.** Con una sesión validada, apaga la red del emulador e intenta actualizar. El catálogo y los favoritos guardados siguen disponibles. Recupera la red antes de cambiar de cuenta.
5. **Administrador.** Cierra sesión, ingresa como admin y explica los contadores de Room. Sincroniza y muestra que el favorito sigue guardado. Abre el diálogo de limpieza y explica qué elimina. Activa avisos si Android lo requiere, sincroniza y toca la notificación.
6. **Pruebas.** Presenta el informe H16. Distingue las pruebas con repositorios simulados de las comprobaciones con Firebase y DummyJSON reales. Explica la prueba omitida por la configuración presente.

## Preguntas que podría hacer el profesor

| Pregunta | Respuesta corta |
| --- | --- |
| ¿Para qué sirve Retrofit? | Ejecuta las solicitudes HTTP de lista y detalle y convierte las respuestas a DTO. |
| ¿Por qué Room? | Guarda los productos y favoritos y entrega sus cambios a la UI por Flow. |
| ¿Qué es MVVM? | La pantalla muestra el estado, el ViewModel coordina y el repositorio maneja los datos. |
| ¿Qué hace Firebase Authentication? | Identifica a la cuenta con correo y contraseña y recupera la sesión. |
| ¿Qué guarda Firestore? | Un perfil por UID con correo y rol. |
| ¿Qué pasa sin Internet? | Dentro de una sesión validada, se muestran los productos guardados. Una sesión nueva o recuperada requiere red. |
| ¿Cómo se protege el panel? | Se verifica el rol en Firestore y se comprueba en la ruta y en las acciones administrativas. |
| ¿Cómo se genera un aviso? | El panel lo publica después de un guardado manual exitoso con permiso de Android. |
| ¿Qué ocurre al cerrar sesión? | Firebase cierra la sesión y la app descarta la pila protegida. |
| ¿Cómo se guarda un favorito? | Como un booleano de la fila del producto en Room. |
| ¿Por qué se comparte un favorito entre cuentas? | La base pertenece al dispositivo y no incluye el UID en la tabla. |
| ¿Por qué usar interfaces de repositorio? | Los ViewModels pueden usar un doble de prueba sin depender de servicios reales. |
| ¿Qué borra Limpiar caché? | Imágenes temporales de Coil en memoria. Los registros y la sesión se conservan. |
| ¿Qué ocurre si se deniega el permiso? | La sincronización guarda los datos y Android no muestra el aviso. |

## Tres conclusiones

1. Una lista respaldada por Room sigue siendo útil sin red.
2. Verificar el rol desde Firestore separa las operaciones del usuario y del administrador.
3. Las pruebas y las capturas permiten reproducir el comportamiento de la app.

## Demostración de las mejoras adicionales

1. Abre SmartCatalog con la sesión cerrada y muestra la bienvenida.
2. Entra a «Crear cuenta» y muestra la validación local. Explica que el alta real queda pendiente de activar la función.
3. Inicia sesión con una cuenta de usuario existente. Busca un smartphone, combina un rango de precio con favoritos y cambia el orden.
4. Quita y vuelve a guardar un favorito. Tras cargar el catálogo, puedes repetir la búsqueda sin conexión.
5. Abre «Perfil», elige «Oscuro» y reabre la aplicación con conexión para comprobar que el tema se conserva. Cierra sesión.
6. Inicia sesión con una cuenta de administrador, abre el panel y sincroniza. Si Android tiene el permiso de avisos, muestra la notificación de guardado.

| Pregunta | Respuesta |
| --- | --- |
| ¿Por qué el formulario está deshabilitado para altas? | La función con App Check sigue pendiente de integración y despliegue. El formulario solo valida sus campos. |
| ¿Quién asignaría el rol a una cuenta nueva? | La callable local está preparada para asignar siempre `usuario` tras verificar Auth y el App ID. |
| ¿Qué consulta la app al cambiar un filtro? | Aplica los criterios a los productos observados desde Room. |
| ¿El tema está vinculado a Firebase? | La preferencia pertenece a la instalación y se guarda en el dispositivo. |
| ¿Por qué el nombre puede estar vacío? | Algunos documentos existentes solo tienen correo y rol. La edición requiere permisos de escritura. |
