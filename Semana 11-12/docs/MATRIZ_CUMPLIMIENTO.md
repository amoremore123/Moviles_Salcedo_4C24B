# Matriz de cumplimiento del Laboratorio 11–12

Fuente local consultada: **GLAB-S11-JLEONS-2026.pdf**, guía de Juan José León Suiyon. Los códigos R están en la sección 4, los PA en la sección 9 y los requisitos de repositorio y entrega en las secciones 7 y 11. La matriz resume los requisitos con sus diferencias respecto al alcance solicitado. Los resultados actuales están en el [informe H16](H16_VERIFICACION.md).

| Código | Requisito resumido | Componente | Evidencia | Estado y observación |
| --- | --- | --- | --- | --- |
| R01 | Login y validación | LoginScreen, SesionViewModel | SesionNavegacionTest y sesión real | Verificado |
| R02 | Roles en Firestore | AuthRepositoryFirebase, AccesoRutas | Pruebas de roles y sesiones reales | Verificado para las cuentas probadas |
| R03 | Lista y estados de UI | ApiService, ListaViewModel, ListaScreen | Pruebas de lista y captura del catálogo | Verificado |
| R04 | Detalle con segundo endpoint | ApiService, DetalleViewModel | Pruebas de detalle y captura | Verificado |
| R05 | MVVM y contratos | UiState, ViewModels, ItemRepository, AuthRepository | Arquitectura y código | Implementado. Sesión y panel tienen estados propios y el contrato de avisos usado se llama NotificadorCatalogo |
| R06 | Persistencia Room | AppDatabase, ItemEntity, ItemDao | ItemDaoTest, ItemRepositoryImplTest | Verificado |
| R07 | Favoritos persistentes | ItemDao, FavoritosViewModel | FavoritosPersistenciaTest, capturas | Verificado. Compartidos por dispositivo |
| R08 | Datos guardados sin red | ItemRepositoryImpl, ListaScreen, DetalleScreen | Pruebas offline y capturas | Verificado dentro de una sesión validada. La recuperación inicial exige red |
| R09 | Aviso local tras sincronizar | PanelAdminViewModel, NotificacionesCatalogo | NotificacionesCatalogoTest, bandeja | Verificado desde el panel con permiso |
| R10 | Panel, totales y mantenimiento | PanelAdminScreen, PanelAdminViewModel | PanelAdminTest, capturas del panel | Verificado. La limpieza segura solo vacía imágenes |
| R11 | Persistencia y cierre de sesión | SesionViewModel, AppNav | SesionNavegacionTest y emulador | Verificado |
| R12 | Al menos tres pruebas de lista con dobles | ListaViewModelTest, ListaViewModelLocalTest, ListaViewModelRemotoTest | Suite unitaria | Verificado |
| R13 | Rama de mejora con IA y PROMPTS.md | Requisito de proceso | Alcance H16 y auditoría Git | No acreditado en esta entrega local. H16 exige permanecer en main |

| Caso original | Evidencia | Estado y observación |
| --- | --- | --- |
| PA-01 Usuario abre catálogo | Cuenta normal y prueba de navegación | Verificado |
| PA-02 Admin entra al catálogo con acceso al panel | Cuenta admin y PanelAdminTest | Variante. Admin abre primero el panel y puede ir al catálogo |
| PA-03 Credenciales incorrectas | SesionNavegacionTest | Verificado con proveedor simulado |
| PA-04 Campos vacíos | SesionNavegacionTest y captura de validación | Verificado |
| PA-05 Primera carga con aviso | ListaViewModel y NotificacionesCatalogoTest | Parcial. La carga inicial muestra la lista, el aviso corresponde a la sincronización manual del panel |
| PA-06 Detalle remoto | Pruebas de repositorio y detalle en emulador | Verificado |
| PA-07 Favorito tras reabrir | FavoritosPersistenciaTest y emulador | Verificado |
| PA-08 Sin red con datos | Pruebas offline y catálogo guardado | Verificado dentro de una sesión validada |
| PA-09 Sin red y sin datos | Pruebas offline con base vacía e histórico H11 | Parcial. En una instalación actual sin sesión, el acceso requiere Firebase en línea |
| PA-10 Favorito tras sincronizar | PanelAdminTest y recuento local | Verificado |
| PA-11 Limpieza deja el catálogo vacío | PanelAdminTest y comprobación de Room | Diferencia de alcance. H14 conserva Room y limpia solo Coil |
| PA-12 Salir y reabrir | SesionNavegacionTest y evidencia de logout | Verificado |
| PA-13 Rotación sin repetir descarga | RotacionSinRecargaTest | Verificado mediante instrumentación |
| PA-14 Permiso denegado | NotificacionesCatalogoTest y evidencia de permiso | Verificado |

Los entregables de la guía incluyen trabajo en pareja, nombre de repositorio específico, colaboración en GitHub, fase de mejora, Pull Request y versión con APK. Este proyecto usa el repositorio de laboratorios y la modalidad individual indicada por el usuario. Antes de H16 se encontraron 15 commits de SmartCatalog en main bajo un solo autor. La rama local mejora-ia existe en el repositorio, pero no contiene historial ni PROMPTS.md de Semana 11-12. La modalidad sin IA, la cuota por integrante, la visibilidad, el acceso del docente y la entrega en el aula no se acreditan mediante las pruebas de la app. La publicación, el PR y la versión remota quedan fuera de la autorización de H16.

## Mejoras adicionales para nota extra

| Mejora | Criterio | Verificación |
| --- | --- | --- |
| M1 | Validar formulario y recuperar un registro pendiente | Preparación local verificada. Activación del servidor y alta real pendientes |
| M2 | Rediseñar el acceso y las pantallas de ambos roles | Verificado en el emulador, incluido el flujo de acceso en pantalla estrecha |
| M3 | Buscar, filtrar y ordenar sobre Room con la API offline | Verificado con una combinación de criterios y conservación de favoritos |
| M4 | Consultar perfil y conservar la preferencia de tema | Verificado con datos controlados de ambos roles. Edición de nombre deshabilitada |
