package com.amoresalcedom.semana05.ui

data class GymClass(
    val id: Int,
    val name: String,
    val schedule: String,
    val info: String,
    val level: String,
    val instructor: String,
    val capacity: String = "Cupos disponibles"
)

data class Reservation(
    val classId: Int,
    val className: String,
    val schedule: String,
    val status: String,
    val slot: String
)

val recommendationGoals = listOf("Energía", "Fuerza", "Movilidad")

fun recommendClass(goal: String): GymClass {
    return when (goal) {
        "Fuerza" -> gymClasses.first { it.name == "CrossFit Express" }
        "Movilidad" -> gymClasses.first { it.name == "Yoga Flow" }
        else -> gymClasses.first { it.name == "HIIT Burn" }
    }
}

val gymClasses = listOf(
    GymClass(
        id = 1,
        name = "CrossFit Express",
        schedule = "Hoy · 07:00",
        info = "Entrenamiento funcional para fuerza y resistencia.",
        level = "Nivel intermedio",
        instructor = "Ana García",
        capacity = "Cupos: 5 / 20 disponibles"
    ),
    GymClass(
        id = 2,
        name = "Yoga Flow",
        schedule = "Hoy · 18:30",
        info = "Rutina de movilidad y control respiratorio.",
        level = "Nivel inicial",
        instructor = "Sofía Torres",
        capacity = "Cupos: 12 / 15 disponibles"
    ),
    GymClass(
        id = 3,
        name = "HIIT Burn",
        schedule = "Esta semana · 19:15",
        info = "Intervalos de alta intensidad para mejorar resistencia.",
        level = "Nivel avanzado",
        instructor = "Marco Ramos",
        capacity = "Cupos: 3 / 25 disponibles"
    ),
    GymClass(
        id = 4,
        name = "Pilates Core",
        schedule = "Esta semana · 12:00",
        info = "Fortalecimiento del abdomen y postura correcta.",
        level = "Nivel inicial",
        instructor = "Elena Cruz",
        capacity = "Cupos: 8 / 20 disponibles"
    )
)
