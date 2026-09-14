# Laboratorio 02: Carrito de compras en Kotlin

## Tema: Variables, funciones y operaciones

**Instructor:** Juan José León Suiyon  
**Curso:** Programación en Móviles  
**Ciclo:** 4to

---

## I. Logro de aprendizaje

Al finalizar el laboratorio, el estudiante programa la lógica de un carrito de compras en Kotlin utilizando variables, tipos de datos, funciones, colecciones y estructuras de decisión, ejecutando el programa por consola y registrando su avance en GitHub mediante commits descriptivos.

## II. Recordatorio: laboratorio SIN inteligencia artificial

Este laboratorio pertenece a la Fase 1 del curso (sin IA). No uses Gemini, ChatGPT ni Copilot. Puedes consultar la documentación oficial (kotlinlang.org) y a tu docente. Tu historial de commits es la evidencia de tu proceso y habrá preguntas orales sobre tu propio código.

## III. Resultado final esperado

El programa debe mostrar en consola:
- Menú del carrito de compras
- Productos agregados con sus detalles
- Subtotal, IGV (18%) y total
- Descuento del 5% si el total supera S/ 3000
- Producto más caro

**Detalles importantes:**
- Los montos siempre con 2 decimales
- Columnas del detalle alineadas
- IGV es el 18% del subtotal
- Descuento del 5% solo si total > S/ 3000

## IV. Conceptos clave de Kotlin

| Concepto | Descripción |
|----------|-------------|
| Tipos básicos | String (texto), Int (enteros), Double (decimales), Boolean (true/false). Kotlin infiere el tipo automáticamente. |
| val / var | val declara un valor que NO cambia (inmutable); var una variable que SÍ puede cambiar. Regla: usa val siempre que puedas. |
| data class | Clase pensada para guardar datos. Kotlin le genera automáticamente toString(), equals() y copy(). Ideal para modelar un Producto. |
| MutableList | Lista a la que se le pueden agregar y quitar elementos con add() y remove(). listOf() crea listas fijas; mutableListOf() listas modificables. |
| fun | Palabra clave para declarar funciones. Pueden recibir parámetros y devolver un valor con un tipo de retorno. |
| Plantillas de String | Insertar valores dentro de un texto con $variable o ${expresión}. Ejemplo: "Hola $nombre". |
| when | Estructura de decisión múltiple de Kotlin (similar a switch, pero más poderosa). La usaremos para el descuento. |

## V. Desarrollo paso a paso

### Parte 1: Proyecto y repositorio (15 min)

1. Crea un nuevo proyecto en Android Studio: plantilla Empty Activity, nombre **Lab02CarritoKotlin**, lenguaje Kotlin.

2. En el panel de proyecto, haz clic derecho sobre el paquete `com.tuapellido.lab02carritokotlin` → New → Kotlin Class/File → tipo File → nómbralo **Carrito**.

3. Dentro del archivo escribe tu primera función main y ejecútala:

```kotlin
fun main() {
    println("=========================================")
    println("   CARRITO DE COMPRAS - TIENDA TECSUP   ")
    println("=========================================")
}
```

4. **COMMIT 1:** "Proyecto inicial con menú del carrito"

---

### Parte 2: Modelo de datos y variables (30 min)

Un carrito guarda productos. Modela un producto con una data class **ENCIMA** de la función main:

```kotlin
data class Producto(
    val nombre: String,
    val precio: Double,
    var cantidad: Int
)
```

**Pregunta para el README:** ¿Por qué nombre y precio son val pero cantidad es var? ¿Qué pasaría si intentas cambiar el precio después de crear el producto?

1. Dentro de main, declara las variables del cliente y la lista del carrito:

```kotlin
val nombreCliente = "Juan Leon"  // String (inferido)
val carrito = mutableListOf<Producto>()  // lista vacía de productos

println("Cliente: $nombreCliente")
println()
```

2. Crea 4 productos y agrégalos a la lista con add():

```kotlin
carrito.add(Producto("Laptop HP", 2500.0, 1))
carrito.add(Producto("Mouse Logitech", 45.5, 2))
// ... agrega 2 productos más
```

3. Ejecuta el programa y agrega un println dentro de un bucle for:

```kotlin
for (producto in carrito) {
    println("Producto agregado: ${producto.nombre}")
}
```

---

### Parte 3: Funciones de cálculo (45 min)

Escribe las funciones **fuera de main** (debajo de la data class):

```kotlin
fun calcularSubtotal(productos: List<Producto>): Double {
    var subtotal = 0.0
    for (p in productos) {
        subtotal += p.precio * p.cantidad
    }
    return subtotal
}

fun calcularIGV(subtotal: Double): Double {
    // TODO: devuelve el 18% del subtotal
}

fun calcularTotal(subtotal: Double, igv: Double): Double {
    // TODO: devuelve la suma de ambos
}
```

1. Completa las dos funciones marcadas con TODO.

2. Llama a las tres funciones desde main e imprime los resultados.

3. **COMMIT 2:** "Agrega data class Producto y funciones de subtotal, IGV y total" — **Commit and Push**

---

### Parte 4: Reporte con formato (30 min)

El detalle tiene columnas alineadas y montos con 2 decimales. Usa String.format:

```kotlin
fun mostrarDetalle(productos: List<Producto>) {
    println("--------- DETALLE DEL CARRITO ---------")
    var i = 1
    for (p in productos) {
        val importe = p.precio * p.cantidad
        println(String.format("%d. %-20s x%d  S/ %8.2f",
            i, p.nombre, p.cantidad, importe))
        i++
    }
    println("---------------------------------------")
}
```

**¿Qué significa el formato?**
- `%-20s` reserva 20 espacios para el nombre (alineado a la izquierda)
- `%8.2f` reserva 8 espacios para el número con 2 decimales (alineado a la derecha)

1. Llama a mostrarDetalle desde main (antes de los totales).

2. Agrega la línea "Cantidad de productos" usando `carrito.size`.

3. Alinea también los totales (Subtotal, IGV, TOTAL A PAGAR) con String.format.

4. **COMMIT 3:** "Agrega reporte de detalle con columnas alineadas"

---

### Parte 5: Lógica adicional — producto más caro y descuento (30 min)

1. **Producto más caro:** Usa maxByOrNull:

```kotlin
val masCaro = carrito.maxByOrNull { it.precio }
if (masCaro != null) {
    println("Producto mas caro: ${masCaro.nombre} " +
        String.format("(S/ %.2f)", masCaro.precio))
}
```

2. **Descuento con when:** si el total supera S/ 3000 aplica 5%; si supera S/ 5000 aplica 10%; en otro caso no hay descuento.

```kotlin
fun calcularDescuento(total: Double): Double {
    return when {
        total > 5000 -> total * 0.10
        total > 3000 -> // TODO
        else -> 0.0
    }
}
```

3. Imprime el mensaje del descuento aplicado y el TOTAL CON DESCUENTO.

4. **COMMIT 4:** "Agrega producto mas caro y descuento por monto con when"

---

### Parte 6: README y entrega (15 min)

1. Ejecuta tu programa completo y toma una captura de la consola.

2. Crea o edita el README.md con:
   - Título del laboratorio
   - Tu nombre completo
   - Descripción breve (qué hace el programa y qué funciones implementaste)
   - Captura de la consola
   - Respuesta a la pregunta de la Parte 2 (val vs var)

3. **Commit and Push** del README. Verifica en el navegador que tu repositorio muestre al menos **4 commits**.

4. Envía la URL de tu repositorio por el medio que indique el docente.

---

## VI. Reto adicional

- **Buscar producto:** Crea una función `buscarProducto(productos: List<Producto>, nombre: String): Producto?` que devuelva el producto si existe o null si no. Investiga la función `find` de las listas.

- **Eliminar producto:** Elimina un producto del carrito con `removeIf` y vuelve a mostrar el detalle y los totales actualizados.

## VII. Entregables

1. URL del repositorio público `lab02-carrito-tuapellido` con el proyecto completo.
2. Mínimo **4 commits** con mensajes descriptivos (uno por cada parte del desarrollo).
3. README.md con captura de la consola final y la respuesta de val vs var.

## VIII. Rúbrica de evaluación (20 puntos)

| Criterio | Descripción | Puntaje |
|----------|-------------|---------|
| Funciones de cálculo | Las funciones de subtotal, IGV y total están bien implementadas, con parámetros y tipos de retorno correctos, y los montos coinciden con el resultado esperado. | 5 |
| Modelo de datos y variables | Define correctamente la data class Producto y usa val/var y tipos de forma adecuada. | 3 |
| Reporte del carrito | El detalle en consola muestra productos, cantidades e importes con el formato solicitado (columnas alineadas y 2 decimales). | 3 |
| Lógica adicional | Implementa producto más caro y descuento con when funcionando correctamente. | 3 |
| Historial de commits en GitHub | Mínimo 4 commits con mensajes descriptivos que reflejan el avance por partes (no un solo commit al final). | 4 |
| README.md | Incluye título, nombre, descripción y captura de la consola con el resultado final. | 2 |
| **TOTAL** | | **20** |

## IX. Preguntas de reflexión (para la defensa oral)

- ¿Cuál es la diferencia entre val y var? Da un ejemplo de tu propio código.
- ¿Qué ventajas tiene una data class frente a declarar variables sueltas para cada producto?
- ¿Qué recibe y qué devuelve tu función calcularIGV? ¿Qué significa ": Double" en su firma?
- ¿Por qué usamos mutableListOf y no listOf para el carrito?
- Explica con tus palabras cómo funciona el when de tu función calcularDescuento.
- Muestra tu historial de commits y explica qué avance representa cada uno.

---

**Instructor:** Juan León Suiyon  
**Email:** jleons@tecsup.edu.pe
