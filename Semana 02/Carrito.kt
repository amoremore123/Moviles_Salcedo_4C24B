data class Producto(
    val nombre: String,
    val precio: Double,
    var cantidad: Int
)

fun main() {
    println("=========================================")
    println("   CARRITO DE COMPRAS - TIENDA TECSUP   ")
    println("=========================================")

    val nombreCliente = "Salcedo"  // Reemplaza con tu apellido
    val carrito = mutableListOf<Producto>()

    println("Cliente: $nombreCliente")
    println()

    // Agrega 4 productos
    carrito.add(Producto("Laptop HP", 2500.0, 1))
    carrito.add(Producto("Mouse Logitech", 45.5, 2))
    carrito.add(Producto("Teclado Mecanico", 150.0, 1))
    carrito.add(Producto("Monitor Samsung", 800.0, 1))

    for (producto in carrito) {
        println("Producto agregado: ${producto.nombre}")
    }
    println()

    // Calcular totales
    val subtotal = calcularSubtotal(carrito)
    val igv = calcularIGV(subtotal)
    val total = calcularTotal(subtotal, igv)

    mostrarDetalle(carrito)

    println("Cantidad de productos: ${carrito.size}")
    println(String.format("Subtotal: S/ %.2f", subtotal))
    println(String.format("IGV (18%%): S/ %.2f", igv))
    println(String.format("TOTAL A PAGAR: S/ %.2f", total))
    println()

    // Producto mas caro
    val masCaro = carrito.maxByOrNull { it.precio }
    if (masCaro != null) {
        println("Producto mas caro: ${masCaro.nombre} " +
            String.format("(S/ %.2f)", masCaro.precio))
    }
    println()

    // Descuento
    val descuento = calcularDescuento(total)
    if (descuento > 0) {
        println(String.format("Descuento aplicado (%%): S/ %.2f", descuento))
        println(String.format("TOTAL CON DESCUENTO: S/ %.2f", (total - descuento)))
    } else {
        println("No se aplicó descuento")
    }
}

fun calcularSubtotal(productos: List<Producto>): Double {
    var subtotal = 0.0
    for (p in productos) {
        subtotal += p.precio * p.cantidad
    }
    return subtotal
}

fun calcularIGV(subtotal: Double): Double {
    return subtotal * 0.18
}

fun calcularTotal(subtotal: Double, igv: Double): Double {
    return subtotal + igv
}

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

fun calcularDescuento(total: Double): Double {
    return when {
        total > 5000 -> total * 0.10
        total > 3000 -> total * 0.05
        else -> 0.0
    }
}
