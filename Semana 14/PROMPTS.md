# PROMPTS.md – Fase 2 (rama `mejora-ia-s14`)

> **Nota de transparencia:** la guía propone usar **Gemini en Android Studio**. En esta fase el asistente que se usó fue **Claude (Claude Code)**. Esta tabla registra los pedidos reales que se le hicieron y cómo se revisó, adaptó o descartó cada propuesta.

## Prompts y decisiones

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| "Sigamos con la fase 2, usa `mejora-ia-s14` y hazlo tú también, no traigas problemas y sigue la guía" | Propuso hacer 4 de las 6 mejoras de la guía: progreso desde el Repository, primos con progreso, reintento automático y tabla comparativa. Cada una en su propio commit y probada en el emulador antes de commitear. | Se descartó **Tiempo máximo con `withTimeout(2 s)`**. | El perfil tarda 2 s y los pedidos 3 s, así que con un límite de 2 s **toda** carga terminaría en "Tardó demasiado", incluso sin error simulado. Eso rompería las pruebas de la Fase 1. |
| (mismo pedido) | También evaluó **Historial con DataStore**. | Se descartó. | Exige una dependencia nueva y persistencia en disco. La guía pide mínimo 3 mejoras y ya había 4 que no agregan dependencias ni riesgo. |
| Mejora 1: progreso desde el Repository | Mover el ciclo de 100 × 40 ms al Repository como `flow { emit(...) }.flowOn(Dispatchers.Default)` y que el ViewModel haga `collect`. | Se conservaron los `Log.d("HILO", ...)`: `Antes`/`Despues` siguen en el ViewModel y `Dentro` pasó al `flow`. | Así la prueba de Logcat de la Fase 1 sigue mostrando `main` → `DefaultDispatcher-worker` → `main`. La cancelación sigue funcionando porque `delay` revisa la cancelación dentro del flow. |
| Mejora 2: primos con progreso | Agregar un parámetro `alAvanzar: (Float) -> Unit` a `contarPrimos` y un `StateFlow` de progreso en el ViewModel. | Se avisa el avance **cada 1 %**, no en cada número. | Avisar en cada número significaría ~3 millones de actualizaciones de estado, una carga inútil. Con 1 % son 100 avisos y la barra se ve igual de fluida. |
| Mejora 3: reintento automático | Una función `conReintentos` en el ViewModel: hasta 3 intentos con espera creciente (500 ms, 1000 ms) solo para los pedidos, que son los que fallan. | Dentro del helper se mantiene `catch (e: CancellationException) { throw e }` antes del `catch (e: Exception)`. Además, cada intento vuelve a leer el interruptor *Simular error*. | Una cancelación nunca debe reintentarse (regla de la guía). Leer el interruptor en cada intento permite ver la recuperación: si lo apagas durante los reintentos, el 2.º intento tiene éxito. |
| Mejora 4: tabla comparativa | Acumular en el ViewModel los tiempos de *Una por una* y *A la vez* y mostrar veces, último tiempo, promedio y el ahorro de *A la vez*. | La tabla **no cuenta las cargas que necesitaron reintentos**. Se agregó un botón *Limpiar tabla*. | Las esperas de los reintentos inflarían los tiempos y la comparación dejaría de ser justa. |

## Corrección después de revisar la guía

| Prompt que escribí | Qué respondió la IA | Qué cambié o descarté | Por qué |
|---|---|---|---|
| "Ahora haz la interfaz como esta imagen" (maqueta con pestañas, saludo y anillo de progreso) | Rediseñó la app con 3 pestañas (Inicio, Resultados, Acerca de), un saludo, un anillo de progreso y un trofeo | Al releer la guía **se descartaron las pestañas, el saludo, el anillo y el trofeo**. Se volvió a la pantalla única de la sección 7 y solo se conservó el estilo visual (tarjetas redondeadas con ícono y paleta azul). | La sección 7 pide "los mismos elementos, el mismo orden y los mismos estados". Con las pestañas, Tócame quedaba separado de la tarea y la app perdía sentido. |
| "Vuelve a analizar la guía, hay cosas que no están bien" | Comparó cada imagen de la guía con la app | Se corrigieron el campo **"Hasta el número"**, el resultado **"Primos hasta N: X"**, **Reintentar debajo** del recuadro de error y el mensaje **"No se pudo cargar: " + mensaje**, que es el contrato del Paso 7 (antes decía "tras 3 intentos"). | Ajustarse exactamente a los contratos y al diseño de la guía. |

## Revisión de reglas (después de cada mejora)

- `git grep` no encuentra `GlobalScope`, `runBlocking` ni `Thread.sleep`.
- Todo `catch (e: Exception)` va precedido de `catch (e: CancellationException) { throw e }`.
- Se mantuvo MVVM + Repository: la View no lanza corrutinas y el ViewModel solo conoce la interface `TareasRepository`.
- Se repitieron en el emulador todas las pruebas de aceptación de la Fase 1: progreso, cancelar, primos, una por una (≈ 5000 ms), a la vez (≈ 3000 ms), error con Reintentar, rotación y doble toque.
