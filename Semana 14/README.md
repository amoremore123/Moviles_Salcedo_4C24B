# CentroTareas

App de la Semana 14 (Hilos en Android): tres tareas que tardan sin congelar la pantalla, usando corrutinas, MVVM y Repository.

## Observación del Paso 1

- **Qué pasa con la pantalla:** al tocar *Tarea larga (MAL)* la app se queda congelada unos 4 segundos. El botón *Tócame* no suma, los botones no responden y la pantalla no se redibuja. Pasado ese tiempo, el texto cambia directamente a *Terminó*.
- **Por qué no se ve "Trabajando...":** `tareaLargaMala()` se ejecuta en el hilo principal, que es el mismo que dibuja la pantalla. El mensaje sí cambia a "Trabajando...", pero `Thread.sleep(4000)` bloquea ese hilo antes de que Compose pueda volver a dibujar. Cuando el hilo se libera, el mensaje ya vale "Terminó", así que solo se dibuja ese último valor.
- **Qué pasaría con 10 segundos:** Android mostraría el diálogo **ANR (Application Not Responding)**, "La app no responde", con las opciones de esperar o cerrar la app. Esto ocurre cuando el hilo principal no atiende un toque durante unos 5 segundos.
