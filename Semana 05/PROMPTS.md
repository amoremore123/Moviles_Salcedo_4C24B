# Registro de Prompts – Desarrollo con IA (Semana 05)

Este documento registra los prompts y peticiones clave utilizados con la asistencia de Inteligencia Artificial para el desarrollo de la aplicación **TECSUP Fit** (Módulo de Gimnasio / Opción B) y el **Portal Académico** en el proyecto Android Studio.

---

## 1. Diseño de la Interfaz de Inicio de Sesión (Login)
* **Prompt del usuario:** 
  > *"en la rama mejora-ia puedes cambiar la interfaz del login y hacer una interfaz mas amigable en esa aplicacion de gym en general pero haciendolo con los requerimientos que pide"*
* **Resultado con IA:** Rediseño completo de la pantalla de inicio de sesión (`LoginScreen.kt`) incorporando un degradado deportivo en tonos azules, tarjeta flotante con esquinas redondeadas, emblema circular de fitness, validaciones de entrada en tiempo real y tipografía moderna en Jetpack Compose.

## 2. Implementación de la Opción B (TECSUP Fit)
* **Prompt del usuario:** 
  > *"Actualizar la interfaz de la aplicación para que se vea igual a las imágenes, manteniendo la funcionalidad básica de inicio de sesión y la pantalla principal..."*
* **Resultado con IA:** 
  * Creación del modelo de datos de clases y reservas (`GymData.kt`).
  * Implementación de la pantalla principal (`HomeScreen`) con **BottomBar de Scaffold** (pestañas fijas abajo: *Inicio*, *Reservas*, *Rutinas*, *Perfil*).
  * Inclusión de recomendación por IA basada en objetivos (*Energía*, *Fuerza*, *Movilidad*).
  * Visualización detallada de **horarios y cupos disponibles** por cada clase de gimnasio.

## 3. Separación de Actividades y Módulos
* **Prompt del usuario:** 
  > *"es que el problema es que tengo dos actividades diferentes y ahora solo puedo ejecutar el unico que hice, crees que puedas separarlos"*
* **Resultado con IA:** Configuración de actividades independientes (`MainActivity` para el Portal Académico y `GymActivity` para TECSUP Fit) declaradas en el `AndroidManifest.xml` con sus respectivos grafos de navegación (`AcademicNavigation` y `GymNavigation`).

---

## Estructura del Proyecto
- **Actividad Principal (Académico):** `MainActivity.kt`
- **Actividad de Gimnasio (TECSUP Fit):** `GymActivity.kt`
- **Navegación:** `AppNavigation.kt`, `GymNavigation.kt`, `Screen.kt`
- **UI Components:** `HomeScreen.kt`, `DetailScreen.kt`, `ReservationsScreen.kt`, `ProfileScreen.kt`, `LoginScreen.kt`, `ConfirmationScreen.kt`
