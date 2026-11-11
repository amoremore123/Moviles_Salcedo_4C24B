# M3 — Búsqueda, filtros y ordenamiento

El catálogo busca por nombre al escribir, con coincidencia parcial y normalización de espacios y mayúsculas. Se pueden combinar precios mínimo y máximo inclusivos, solo favoritos y orden por precio o nombre en ambos sentidos. Los empates mantienen el orden de origen. Se muestra el número de coincidencias, el estado sin resultados y un aviso para rangos inválidos. Limpiar filtros restaura el orden y todos los productos disponibles.

El ViewModel combina los criterios con la lista observada desde Room. Cambiar un criterio no inicia consultas de red ni cambia el orden persistido. El modelo local dispone de nombre, precio y favorito para los criterios solicitados. Marca y valoración quedarían sujetas a incorporar esos campos al modelo.

## Verificación

Gradle terminó con BUILD SUCCESSFUL. Pasaron 137 pruebas unitarias y diez instrumentadas. La prueba del catálogo cargó cuatro productos en Room y simuló un fallo de la API. La combinación de criterios dejó una coincidencia, limpiar restauró las cuatro y se comprobó que hubo una única consulta de sincronización. También pasaron las regresiones de favoritos, persistencia y respaldo offline.

Las capturas `filtros-offline.png` y `resultado-offline.png` están en `evidencias/mejoras/m3/`. Fueron tomadas en el emulador con datos de prueba guardados en Room.
