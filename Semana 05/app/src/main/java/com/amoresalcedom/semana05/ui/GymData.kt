package com.amoresalcedom.semana05.ui

data class GymClass(
    val id: Int,
    val name: String,
    val schedule: String,
    val info: String,
    val level: String,
    val instructor: String
)

data class Reservation(
    val classId: Int,
    val className: String,
    val schedule: String,
    val status: String,
    val slot: String
)

val gymClasses = listOf(
    GymClass(
        id = 1,
        name = "CrossFit Express",
        schedule = "Hoy · 07:00",
        info = "Entrenamiento funcional para fuerza y resistencia.",
        level = "Nivel intermedio",
        instructor = "Ana García"
    ),
    GymClass(
        id = 2,
        name = "Yoga Flow",
        schedule = "Hoy · 18:30",
        info = "Rutina de movilidad y control respiratorio.",
        level = "Nivel inicial",
        instructor = "Sofía Torres"
    ),
    GymClass(
        id = 3,
        name = "HIIT Burn",
        schedule = "Esta semana · 19:15",
        info = "Intervalos de alta intensidad para mejorar resistencia.",
        level = "Nivel avanzado",
        instructor = "Marco Ramos"
    ),
    GymClass(
        id = 4,
        name = "Pilates Core",
        schedule = "Esta semana · 12:00",
        info = "Fortalecimiento del abdomen y postura correcta.",
        level = "Nivel inicial",
        instructor = "Elena Cruz"
    )
)
