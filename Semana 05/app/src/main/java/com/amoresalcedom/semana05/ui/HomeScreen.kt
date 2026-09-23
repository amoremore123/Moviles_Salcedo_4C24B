package com.amoresalcedom.semana05.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReservations: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onClassSelected: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedGoal by remember { mutableIntStateOf(0) }
    val filters = listOf("Hoy", "Esta semana")
    val selectedGoalName = recommendationGoals[selectedGoal]
    val recommendedClass = recommendClass(selectedGoalName)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TECSUP Fit") }
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                    },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        onNavigateToReservations()
                    },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Reservas") },
                    label = { Text("Reservas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        onNavigateToRoutines()
                    },
                    icon = { Icon(Icons.Default.Timer, contentDescription = "Rutinas") },
                    label = { Text("Rutinas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        onNavigateToProfile()
                    },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Encuentra tu clase ideal",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2FF)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Recomendación IA",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F6FEB)
                    )
                    Text(
                        text = "Elige tu objetivo y encuentra una clase para ti.",
                        modifier = Modifier.padding(top = 4.dp),
                        color = Color(0xFF4A5E7A)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        items(recommendationGoals) { goal ->
                            val goalIndex = recommendationGoals.indexOf(goal)
                            Button(
                                onClick = { selectedGoal = goalIndex },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (selectedGoal == goalIndex) {
                                        Color(0xFF1F6FEB)
                                    } else {
                                        Color.White
                                    }
                                )
                            ) {
                                Text(
                                    text = goal,
                                    color = if (selectedGoal == goalIndex) {
                                        Color.White
                                    } else {
                                        Color(0xFF1F6FEB)
                                    }
                                )
                            }
                        }
                    }
                    Text(
                        text = "Te recomendamos: ${recommendedClass.name}",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B2A41)
                    )
                    Button(
                        onClick = { onClassSelected(recommendedClass.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Ver recomendación")
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                items(filters) { filter ->
                    val selected = filter == "Hoy"
                    Button(
                        onClick = { },
                        modifier = Modifier.height(40.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = if (selected) Color(0xFF1F6FEB) else Color(0xFFEAF2FF)
                        )
                    ) {
                        Text(
                            text = filter,
                            color = if (selected) Color.White else Color(0xFF1F6FEB)
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gymClasses) { gymClass ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClassSelected(gymClass.id) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FB)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = gymClass.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE7F5EA), RoundedCornerShape(50))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = gymClass.level,
                                        color = Color(0xFF2E7D32),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Text(
                                text = gymClass.schedule,
                                color = Color(0xFF4A5E7A),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                text = gymClass.info,
                                modifier = Modifier.padding(top = 6.dp),
                                color = Color(0xFF5F6F86)
                            )
                            Text(
                                text = "Instructor: ${gymClass.instructor}",
                                modifier = Modifier.padding(top = 8.dp),
                                color = Color(0xFF2C3E50)
                            )
                        }
                    }
                }
            }
        }
    }
}
