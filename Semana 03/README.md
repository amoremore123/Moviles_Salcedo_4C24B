# Laboratorio 03: Registro de Producto

**Estudiante:** [Tu Nombre Completo]  
**Curso:** Programación en Móviles  
**Docente:** Juan José León Suiyon  


## Preguntas de Reflexión

### ¿Qué pasaría si declaras las variables de los campos SIN `remember`?
Si se declaran las variables sin `remember` (por ejemplo, usando solo `mutableStateOf("")`), cada vez que la pantalla realiza una recomposición (se redibuja debido a un cambio de estado), la variable se vuelve a inicializar a su valor original (vacío `""`). Como consecuencia, el texto introducido por el usuario desaparece inmediatamente al escribir la primera letra y no se puede interactuar con el formulario.
