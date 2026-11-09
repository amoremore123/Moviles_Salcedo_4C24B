# Configuración de Firebase para SmartCatalog

El proyecto utilizado es **mitecsup** y el paquete Android es **com.tecsup.smartcatalog**. El mismo proyecto también tiene registrada MyTecsup. Usa la entrada Android de SmartCatalog al descargar el archivo de configuración y conserva los datos de la otra aplicación.

1. En Firebase Console abre la configuración del proyecto, selecciona la app Android con ese paquete o regístrala si estás preparando otro entorno.
2. Descarga `google-services.json` desde la ficha de esa app y colócalo en `Semana 11-12/app/`. El archivo se excluye de Git.
3. En Authentication habilita **Correo electrónico/Contraseña**. Para un entorno de prueba, un administrador puede crear las cuentas en la consola y gestionar sus contraseñas por separado.
4. En Firestore, crea un documento `usuarios/{uid}` por cuenta. Usa como ID el UID exacto de Authentication. Los campos `correo` y `rol` son de tipo texto.

| Cuenta de laboratorio | correo | rol |
| --- | --- | --- |
| Administrador | `admin@correo.com` | `admin` |
| Usuario | `usuario@correo.com` | `usuario` |

`FabricaAuthFirebase` conecta el SDK con `AuthRepositoryFirebase`. Al iniciar sesión, el proveedor usa Email/Password. Firestore lee el documento propio desde el servidor. El repositorio comprueba que el correo corresponda a la identidad y que el rol sea válido. Para recuperar sesión, el SDK refresca el token y se consulta el perfil nuevamente. Si falla la validación, se deniega la entrada. Al cerrar sesión se invoca `signOut()` y se descarta la navegación protegida. El panel exige rol `admin` tanto en su ruta como en sus acciones.

## Reglas

El archivo [firestore.rules](../firestore.rules) documenta una política esperada. Permite obtener el perfil propio con un UID autenticado y deniega listar o escribir perfiles desde el cliente. La app no escribe documentos. Las pruebas reales de inicio de sesión verifican que las cuentas pueden leer sus perfiles. El contenido completo de las reglas desplegadas no se verificó. Como el proyecto es compartido, el archivo local no debe desplegarse sin revisar el impacto sobre MyTecsup.
