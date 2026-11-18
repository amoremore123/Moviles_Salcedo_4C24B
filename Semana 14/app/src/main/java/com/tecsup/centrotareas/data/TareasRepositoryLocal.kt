package com.tecsup.centrotareas.data

import android.util.Log
import com.tecsup.centrotareas.model.ResultadoPrimos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    // Mejora 1: 100 pasos de 40 ms (≈ 4 s); flowOn mueve el trabajo a Dispatchers.Default
    override fun progresoTarea(): Flow<Float> = flow {
        Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
        for (i in 1..100) {
            delay(40) // delay revisa la cancelación
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    // Paso 5: cálculo pesado de CPU, fuera del hilo principal
    override suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit): Int =
        contarYSumarPrimos(hasta, alAvanzar).cantidad

    // Mejora 5: el mismo recorrido del Paso 5, pero además va sumando cada primo
    override suspend fun contarYSumarPrimos(hasta: Int, alAvanzar: (Float) -> Unit): ResultadoPrimos =
        withContext(Dispatchers.Default) {
            var cantidad = 0
            var suma = 0L // Long: la suma pasa de 2 mil millones muy rápido
            // Mejora 2: avisa el avance cada 1 % (no en cada número, para no saturar la UI)
            val paso = maxOf(1, hasta / 100)
            for (n in 2..hasta) {
                ensureActive() // permite cancelar a mitad del cálculo
                if (esPrimo(n)) {
                    cantidad++
                    suma += n
                }
                if (n % paso == 0) alAvanzar(n.toFloat() / hasta)
            }
            alAvanzar(1f)
            ResultadoPrimos(cantidad, suma)
        }

    // Prueba divisores desde 2 hasta la raíz de n (d * d <= n, en Long para no desbordar)
    private fun esPrimo(n: Int): Boolean {
        var d = 2
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
