# Mi Carrito TECSUP

Autor: Amore Salcedo Macedo

## Preguntas

### 1. ¿Por qué `mutableStateListOf` y no una `MutableList` normal?

`mutableStateListOf` es observable por Compose. Cuando se agregan o eliminan productos, Compose detecta el cambio y actualiza la interfaz. Una `MutableList` normal no notifica esos cambios automáticamente.

### 2. ¿Por qué la lista es `val`?

`val` impide reasignar la referencia de la lista, pero no vuelve inmutable al objeto. Por eso podemos cambiar su contenido usando `add()` y `remove()`.

### 3. ¿Qué hace `weight(1f)` en la `LazyColumn`?

Hace que la lista ocupe el espacio disponible restante dentro del `Column`, dejando el espacio necesario para el panel de totales.
