# Registro de Prompts - Laboratorio 06 (Semana 06)

Este documento registra los prompts utilizados durante la Fase 2 (Mejora con IA) del Laboratorio 06 para implementar el buscador en tiempo real combinado con el filtro de categorías en `PantallaInicio.kt`.

---

### Prompt 1: Buscador en tiempo real y combinación de filtros en PantallaInicio
**Prompt utilizado:**
> *"Actúa como un desarrollador experto en Jetpack Compose. Necesito agregar un campo de búsqueda en `PantallaInicio.kt` que filtre la lista de productos en tiempo real a medida que el usuario escribe, combinándose correctamente con el filtro de categoría ya existente (LazyRow de chips). Ambos filtros deben funcionar juntos, de forma que si hay una categoría seleccionada y texto de búsqueda, se muestren solo los productos que cumplan ambas condiciones."*

**Resultado / Implementación generada:**
- Inclusión de `OutlinedTextField` con icono de búsqueda en la parte superior de `PantallaInicio`.
- Creación de la lógica de filtrado combinada (`filteredProducts`) que evalúa tanto `selectedCategory` como `searchQuery` de manera case-insensitive sobre el nombre y descripción de cada `Product`.
- Actualización reactiva inmediata mediante `mutableStateOf` en Compose.
