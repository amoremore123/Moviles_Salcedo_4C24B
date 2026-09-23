package com.amoresalcedom.semana05.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val reservations = listOf(
    Reservation(
        classId = 1,
        className = "CrossFit Express",
        schedule = "Hoy · 07:00",
        status = "Confirmada",
        slot = "07:00"
    ),
    Reservation(
        classId = 2,
        className = "Yoga Flow",
        schedule = "Mañana · 18:30",
        status = "Pendiente",
        slot = "18:30"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(onBackToHome: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis reservas") },
                navigationIcon = {
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F8FF))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F8FF))
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val confirmedCount = reservations.count { it.status == "Confirmada" }
            Text(
                text = "$confirmedCount reservas confirmadas de ${reservations.size}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B2A41),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reservations) { reservation ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Reserva",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reservation.className,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B2A41)
                                )
                                Text(
                                    text = reservation.schedule,
                                    color = Color(0xFF6E7C90),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Text(
                                text = reservation.status,
                                color = if (reservation.status == "Confirmada") Color(0xFF2E7D32) else Color(0xFFB7791F),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
