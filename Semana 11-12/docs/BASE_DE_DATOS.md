# Base de datos local

`AppDatabase` abre `smartcatalog.db` con Room. El esquema es versión 1 y se exporta en `app/schemas/`. La única entidad es `ItemEntity`, tabla `items`. No hay relaciones ni tabla de cuentas.

| Campo | Tipo Kotlin | Uso |
| --- | --- | --- |
| `id` | Int | Clave primaria del producto |
| `nombre` | String | Nombre visible |
| `descripcion` | String | Descripción |
| `precio` | Double | Importe recibido de la API |
| `imagen` | String | URL de la miniatura |
| `categoria` | String | Categoría |
| `favorito` | Boolean | Marcador local, falso por defecto |

`ItemDao` observa todos los productos y los favoritos ordenados por ID, consulta un registro por ID y cuenta filas y favoritos. La alternancia del favorito es una actualización SQL atómica. El panel deriva ambos contadores de una misma emisión de productos.

La sincronización valida el lote antes de escribir. `insertarTodos` inserta IDs nuevos y actualiza los datos descriptivos de los existentes mediante una transacción. Conserva el favorito y deja los IDs ausentes de la respuesta en la base. Un lote vacío no cambia los datos. Si falla la descarga o la escritura, los datos previos siguen disponibles.

El catálogo observa Room. El detalle intenta primero el endpoint remoto y usa el registro local si falla. Sin datos guardados, se muestra un error con opción de reintento. Room conserva datos entre reinicios y cierres de sesión. Los favoritos son compartidos entre cuentas del dispositivo. Limpiar caché en el panel vacía las imágenes en memoria de Coil, por lo que los registros de Room y la sesión se conservan.
