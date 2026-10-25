# Fase 2: rediseño visual asistido por IA

Fecha de trabajo: 25 de octubre de 2026 (America/Lima).

## Alcance y referencias

Rediseño de presentación sobre la base `7e9b0b0`, exclusivamente en `mejora-ia`. Referencias: páginas 6 y 7 del PDF original del Laboratorio 08 y la imagen oficial suministrada por el estudiante. La fila superior corresponde al estudiante; la inferior, a Soporte TI.

La autorización concreta fue modernizar las pantallas existentes, mantener sus operaciones y crear cinco commits locales. No se añadieron funcionalidades de negocio de otras propuestas de Fase 2 (registro, SLA, notas internas ni consultas nuevas). No se hizo push.

## Cambios por pantalla

| Pantalla | Mejora |
| --- | --- |
| Recuperación de sesión / Splash | Presentación de marca, iconos tecnológicos y carga existente; sin espera artificial ni pantalla extra |
| Login compartido | Degradado sutil, mensajes por perfil, iconos de campos y validación animada; mismo acceso para ambos roles |
| Mis tickets | Barra cian, bienvenida con contador real, filtros, tarjetas y navegación inferior; la lista vuelve al inicio al cambiar búsqueda o filtro |
| Nuevo ticket | Secciones, iconos, prioridad con color semántico, acción de guardado fija y navegación bloqueada mientras se guarda |
| Detalle y resolución | Información agrupada, progreso animado, burbujas, tarjeta de resolución y diálogo de estrellas existente |
| Perfil | Avatar, estadísticas reales, preferencias con iconos y cierre de sesión diferenciado |
| Panel de soporte | Barra violeta, indicadores en dos columnas, alerta urgente y tarjetas para gráficos reales |
| Bandeja | Encabezado propio, búsqueda y filtros destacados, tarjetas compartidas y navegación inferior |
| Atención | Información agrupada, selectores, conversación compartida y acciones existentes |
| Categorías | Tarjetas lavanda, iconos, estado, activación, edición y eliminación existentes |
| Nueva / editar categoría | Selector de iconos con previsualización y acción de guardado fija |
| Ajustes | Identidad violeta, preferencias y cambio de perfil conservados |

## Componentes y navegación

`AyudaVisual.kt` reúne `AyudaTopAppBar`, `AyudaNavigationBar`, `TarjetaBienvenida`, `TarjetaIndicador`, `EncabezadoSeccion`, `EstadoVacio` y el espacio de navegación inferior proporcionado por `NavAyuda`.

Cada pantalla usa un único encabezado Material 3 dentro de su `Scaffold`. Se conserva `safeDrawingPadding`, y la barra no vuelve a añadir los insets del sistema. Los formularios son desplazables y el botón principal permanece visible. En teléfonos de menor altura es necesario desplazarse hasta Prioridad.

- Estudiante: Tickets, Nuevo, Perfil.
- Soporte: Panel, Tickets, Categorías, Ajustes.
- La bandeja continúa usando el ViewModel del panel y conserva sus filtros al volver de un ticket.
- Se mantienen todas las rutas originales. Las acciones de navegación permanecen en `NavAyuda`; las pantallas reciben callbacks.
- Guardar, cerrar o cambiar preferencias deshabilita la barra cuando hay una operación pendiente.

## Color y movimiento

| Uso | Estudiante | Soporte |
| --- | --- | --- |
| Principal claro | `#0891B2` | `#7C3AED` |
| Contenedor | `#CFFAFE` | `#EDE4FF` |
| Fondo claro | `#F7FDFE` | `#FAF8FF` |
| Fondo oscuro | `#0D1B22` | `#151020` |

Se conservan ámbar, azul, verde y gris para estados, y rojo, café y gris para prioridades. Los chips muestran texto además del color. El modo oscuro usa superficies y acentos específicos por rol; no hay colores dinámicos que sustituyan la paleta del laboratorio.

Se usa `AnimatedVisibility` para validaciones, `animateContentSize` en tarjetas y filtros, y `animateFloatAsState` en progreso y barras de categorías. Los botones mantienen el feedback de Material 3. Un `Snackbar` confirma la creación del ticket sin reemplazar el resultado de Room.

## Arquitectura preservada

No se modificaron MainActivity, entidades, DAO, base de datos, Repository, DataStore ni ViewModels. No se añadieron dependencias. Los gráficos, categorías, tickets y contadores siguen procediendo de los flujos existentes de Room. Las pantallas mantienen los controles de rol y no hacen consultas de negocio.

## Verificación

Ejecutado sobre el código final del rediseño:

```powershell
.\gradlew.bat :app:assembleDebug :app:connectedDebugAndroidTest
```

**BUILD SUCCESSFUL en 7 min 34 s. 57 pruebas ejecutadas: 57 aprobadas, 0 fallos y 0 ignoradas.** Se contrastó el resultado de Gradle con los 57 casos del XML de resultados instrumentados.

| Clase | Pruebas aprobadas |
| --- | --- |
| `DetalleTicketViewModelTest` | 7 |
| `GestionSoporteViewModelTest` | 12 |
| `MisTicketsViewModelTest` | 2 |
| `NuevoTicketViewModelTest` | 4 |
| `PanelViewModelTest` | 7 |
| `PerfilPreferenciasTest` | 6 |
| `SesionLoginTest` | 14 |
| `SesionViewModelTest` | 3 |
| `AyudaRepositoryRoomTest` | 2 |

Las 56 pruebas previas permanecen, más `redisenioNavegacionPorRolesModoOscuroYEvidenciasReales`. El informe HTML reproducible queda localmente en `app/build/reports/androidTests/connected/debug/index.html`; los archivos generados de compilación no se incorporan a Git.

La primera ejecución tuvo 54 aprobadas y 3 fallidas por desplazamientos de interfaz. Tras corregirlos se ejecutaron las 3 afectadas (3/3 aprobadas) y luego esta suite completa (57/57). No se sustituyó la ejecución completa por resultados parciales.

Incidencias detectadas y corregidas durante el trabajo:

1. La lista retenía un desplazamiento previo al cambiar filtros: ahora vuelve al inicio.
2. Dos pruebas de creación asumían que Prioridad siempre estaba visible: ahora desplazan el formulario hasta el control, sin eliminar aserciones.
3. Las antiguas pestañas Resumen/Bandeja fueron sustituidas por Panel/Tickets: las pruebas usan los nuevos identificadores de navegación y comprueban el título de Bandeja de tickets.
4. El renderizador gráfico del emulador producía capturas negras: se reinició el mismo AVD con `-gpu software`, conservando sus datos. No es un cambio del código ni una dependencia de la aplicación. Las evidencias entregadas fueron revisadas visualmente.

Entorno: Medium Phone, Android 17 / API 37, JDK 17 y Gradle Wrapper del proyecto. Las 56 pruebas existentes se conservaron; se añadió una prueba de navegación y evidencias. El recorrido nuevo no guarda la categoría de vista previa ni cierra el ticket al visitar el diálogo de calificación; comprueba que Room conserva sus seis tickets de demostración.

## Evidencias

Las imágenes son capturas PNG directas del emulador, obtenidas durante pruebas instrumentadas con `screencap`; no son imágenes generadas ni montajes. Los valores visibles corresponden al estado real de Room durante la captura. El reloj de Android puede diferir de la zona horaria del equipo.

[Ver las 19 capturas agrupadas por perfil y modo](capturas/fase2/README.md).

## Límites de la verificación

La comprobación visual se realizó en un emulador 1080 × 2400. No se afirma una prueba en dispositivo físico, tablet, todas las escalas de fuente o todas las versiones Android. No se realizó una auditoría automática de contraste o de accesibilidad completa. El splash de recuperación es transitorio y no se añade un botón de continuar que retrase el flujo de sesión.

## Publicación

`main` conserva `7e9b0b0` y sus 14 commits del laboratorio. Los cambios de esta fase pertenecen a cinco commits locales de `mejora-ia`. La publicación queda pendiente de autorización.

## Archivos de implementación y pruebas

- `app/src/androidTest/java/com/tecsup/ayudacampus/SesionLoginTest.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/cliente/DetalleTicketScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/cliente/LoginScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/cliente/MisTicketsScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/cliente/NuevoTicketScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/cliente/PerfilScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/components/AyudaVisual.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/components/TicketCard.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/components/graficos/GraficosTickets.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/navigation/NavAyuda.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/soporte/AtenderTicketScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/soporte/BandejaScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/soporte/CategoriasScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/soporte/FormularioCategoriaScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/soporte/PanelScreen.kt`
- `app/src/main/java/com/tecsup/ayudacampus/ui/theme/Theme.kt`

Documentación y evidencias añadidas: `REDISENO_IA.md`, `PROMPTS.md`, la sección final de `README.md`, el índice `capturas/fase2/README.md` y 19 PNG reales.
