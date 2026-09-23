package com.amoresalcedom.semana05.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class StudentItem(
    val name: String,
    val career: String,
    val studentId: String,
    val email: String,
    val faculty: String,
    val biography: String
)

val sampleStudents = listOf(
    StudentItem("Juan León", "Ingeniería de Sistemas", "2024-0001", "juan.leon@example.com", "Ingeniería y Tecnología", "Estudiante destacado con interés en desarrollo Android."),
    StudentItem("Maria Garcia", "Arquitectura", "2024-0002", "maria.garcia@example.com", "Arquitectura y Urbanismo", "Apasionada por el diseño sostenible y modelado 3D."),
    StudentItem("Carlos Perez", "Medicina", "2024-0003", "carlos.perez@example.com", "Ciencias de la Salud", "Enfocado en investigación clínica y anatomía humana."),
    StudentItem("Ana Lopez", "Derecho", "2024-0004", "ana.lopez@example.com", "Derecho y Ciencias Políticas", "Interesada en derecho corporativo y propiedad intelectual."),
    StudentItem("Luis Ramirez", "Administración", "2024-0005", "luis.ramirez@example.com", "Negocios y Gestión", "Especializado en gestión de proyectos y finanzas corporativas.")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    onBack: () -> Unit,
    onItemSelected: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Directorio de Alumnos", fontWeight = FontWeight.Bold, color = Color(0xFF311B92)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF311B92)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F5FC))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F5FC))
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(sampleStudents) { index, student ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(index) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0EBF6)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar Circle
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFD1C4E9),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = student.name,
                                        tint = Color(0xFF512DA8),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = student.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF311B92)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = student.career,
                                    fontSize = 13.sp,
                                    color = Color(0xFF7E57C2)
                                )
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Ver detalle",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
