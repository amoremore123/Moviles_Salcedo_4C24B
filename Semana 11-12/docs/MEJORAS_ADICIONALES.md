# Mejoras adicionales de SmartCatalog

H1–H16 corresponden al laboratorio original. M1–M4 son mejoras adicionales para la exposición.

| Mejora | Estado | Archivos principales | Evidencia local |
| --- | --- | --- | --- |
| [M1 Registro](MEJORA_M1_REGISTRO.md) | Formulario local y reanudación preparados. Activación del servidor y pruebas reales pendientes | `RegistroViewModel.kt`, `ServicioRegistro.kt`, `functions/index.js` | `evidencias/mejoras/m1/` |
| [M2 Interfaz](MEJORA_M2_INTERFAZ.md) | Bienvenida y pantallas rediseñadas con Material 3 | `BienvenidaScreen.kt`, `Theme.kt`, `ItemCard.kt`, `PanelAdminScreen.kt` | `evidencias/mejoras/m2/` |
| [M3 Búsqueda](MEJORA_M3_BUSQUEDA.md) | Búsqueda, filtros combinados y ordenamiento verificados con Room offline | `ListaViewModel.kt`, `CriteriosCatalogo.kt`, `ControlesCatalogo.kt` | `evidencias/mejoras/m3/` |
| [M4 Perfil y tema](MEJORA_M4_PERFIL.md) | Lectura de perfil y tema persistente. Edición de nombre deshabilitada | `PerfilScreen.kt`, `PreferenciasTema.kt`, `AuthRepositoryFirebase.kt` | `evidencias/mejoras/m4/` |

Jetpack Compose y Material 3 componen la interfaz. StateFlow combina los criterios de búsqueda con los productos que observa Room. Las preferencias privadas guardan el tema y, en el adaptador de registro preparado, el marcador del UID pendiente. La callable local usa Firebase Authentication, App Check y Firestore mediante Admin SDK. El proyecto compartido conserva sus reglas desplegadas.

Durante M2 se aisló la base en memoria de la prueba de rotación para que no heredara favoritos de otras pruebas. Las capturas de navegación se sincronizaron con el renderizado de Compose. M3 usa los campos presentes en Room, por lo que la búsqueda y el ordenamiento quedan disponibles cuando falla la API.

## Pruebas

| Etapa | Compilación | Unitarias Android | Instrumentadas |
| --- | --- | --- | --- |
| M1 | BUILD SUCCESSFUL | 133 aprobadas | 9 aprobadas |
| M2 | BUILD SUCCESSFUL | 133 aprobadas | 14 aprobadas y 1 repetición en pantalla estrecha |
| M3 | BUILD SUCCESSFUL | 137 aprobadas | 10 aprobadas |
| M4 y regresión final | BUILD SUCCESSFUL | 140 aprobadas | 49 casos distintos aprobados y 1 omitido |

También pasaron cinco pruebas Node de la callable local. La omisión final corresponde a la variante sin configuración de Firebase. Las verificaciones en el emulador usaron cuentas de prueba. El alta pública, la verificación de correo y la atestación reales de M1 siguen pendientes de activación.

## Commits locales

| Mejora | Commit |
| --- | --- |
| M1 | `b82e6b167260d5a614712064ff286c76020effec` |
| M2 | `499afc7f5d907db34df7ddb01b5fd72b96675184` |
| M3 | `fa48309c9cf971be6a8ca8fad4b9c6b42925f337` |
| M4 | `feat: agregar perfil y modo oscuro` |

Los cambios de las cuatro etapas se prepararon con índices Git temporales. Las capturas quedan en el equipo bajo la carpeta de evidencias excluida de Git.
