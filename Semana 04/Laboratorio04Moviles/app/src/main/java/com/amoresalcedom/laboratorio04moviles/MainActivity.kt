package com.amoresalcedom.laboratorio04moviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amoresalcedom.laboratorio04moviles.ui.theme.Laboratorio04MovilesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Laboratorio04MovilesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PantallaTareas(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TemperatureDisplay(modifier: Modifier = Modifier) {
    var temperature by remember { mutableStateOf(20) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Temperatura: $temperature grados",
            color = when {
                temperature > 30 -> Color.Red
                temperature < 10 -> Color.Blue
                else -> Color.Unspecified
            }
        )
        Button(onClick = { temperature++ }) {
            Text("Subir")
        }
        Button(onClick = { temperature-- }) {
            Text("Bajar")
        }
        Button(onClick = { temperature = 20 }) {
            Text("Resetear")
        }
    }
}

data class Tarea(
    val id: Int,
    val nombre: String,
    val completada: Boolean = false
)

@Composable
fun ItemTarea(
    tarea: Tarea,
    onEliminar: () -> Unit,
    onCambiarEstado: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = tarea.completada,
            onCheckedChange = onCambiarEstado
        )
        Text(
            text = tarea.nombre,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onEliminar) {
            Text("Eliminar")
        }
    }
}

@Composable
fun PantallaTareas(modifier: Modifier = Modifier) {
    var textoTarea by remember { mutableStateOf("") }
    var siguienteId by remember { mutableStateOf(0) }
    val listaTareas = remember { mutableStateListOf<Tarea>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Lista de tareas - Tecsup")
        TextField(
            value = textoTarea,
            onValueChange = { textoTarea = it },
            label = { Text("Ingrese una tarea") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (textoTarea.isNotBlank()) {
                    listaTareas.add(Tarea(siguienteId++, textoTarea.trim()))
                    textoTarea = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar tarea")
        }
        Text("Total de tareas: ${listaTareas.size}")
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(listaTareas, key = { it.id }) { tarea ->
                ItemTarea(
                    tarea = tarea,
                    onEliminar = { listaTareas.remove(tarea) },
                    onCambiarEstado = { completada ->
                        val indice = listaTareas.indexOf(tarea)
                        if (indice != -1) {
                            listaTareas[indice] = tarea.copy(completada = completada)
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Laboratorio04MovilesTheme {
        PantallaTareas()
    }
}