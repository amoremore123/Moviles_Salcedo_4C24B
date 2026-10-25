# Registro del trabajo asistido por IA

## Herramienta y referencias reales

Este rediseño se realizó con Codex el 25 de octubre de 2026 por solicitud expresa del estudiante. No se atribuye a Gemini ni a una fase de trabajo sin IA. Se revisaron el código existente, el PDF del laboratorio y la imagen oficial adjunta antes de modificar la interfaz.

## Solicitud recibida (síntesis, no transcripción)

Modernizar AyudaCampus manteniendo la estructura del laboratorio, estudiante cian y soporte violeta; integrar TopAppBar, tarjetas, formularios, navegación inferior, gráficos reales, animaciones moderadas y modo oscuro. Preservar MVVM, seguridad por roles y funcionalidades. Trabajar en mejora-ia, conservar main, crear cinco commits y no publicar todavía.

## Decisiones implementadas con asistencia de IA

- Unificar la barra superior con Material 3 y mantener un Scaffold por pantalla.
- Reutilizar los componentes y flujos existentes en lugar de introducir otra biblioteca visual.
- Proporcionar la navegación desde NavAyuda y permitir que cada pantalla la bloquee mientras guarda.
- Compartir tarjetas de indicadores y conversación, ajustar tipografía y superficies por rol.
- Mantener el splash como recuperación de sesión existente, sin retraso artificial.
- Conservar las pruebas originales, adaptarlas al nuevo modo de navegación y añadir un recorrido de evidencias reales.

## Revisión y correcciones

Se revisó que las pantallas no incorporaran acceso a DAO ni consultas del Repository; no se modificó la capa de datos ni los ViewModels. No se detectó una propuesta que exigiera romper MVVM. Durante las pruebas se corrigieron el desplazamiento de la lista y la navegación de pruebas hasta controles que habían quedado fuera del área visible. Los fallos se documentan en REDISENO_IA.md y no se ocultaron reduciendo cobertura.

Las capturas finales son de la aplicación compilada y ejecutada, no diseños generados. La IA asistió tanto el código visual como la adaptación de pruebas, la inspección de evidencias y esta documentación. No se inventaron autores, fechas históricas, resultados o una ejecución con Gemini.
