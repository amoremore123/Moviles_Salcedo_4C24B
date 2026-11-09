# Integración con DummyJSON

`RetrofitClient` usa `https://dummyjson.com/` como base. OkHttp administra conexiones y tiempos de espera. Gson convierte las respuestas a `ProductosResponseDto` y `ProductoDto`.

| Solicitud GET | Método de ApiService | Uso |
| --- | --- | --- |
| `https://dummyjson.com/products/category/smartphones` | `obtenerSmartphones()` | Descargar el catálogo |
| `https://dummyjson.com/products/{id}` | `obtenerDetalle(id)` | Consultar un smartphone |

`toItemSeguro` convierte un DTO a `Item`. Exige un ID positivo y usa valores seguros para campos descriptivos ausentes o precios inválidos. El repositorio rechaza elementos nulos, IDs duplicados y un detalle con ID distinto del solicitado. Convierte los productos a entidades y guarda el lote en Room. Los importes se muestran según la API, sin conversión de moneda.

El catálogo y el panel usan `ItemRepositoryImpl.refrescar()`. El repositorio serializa las escrituras de sincronización, valida el lote antes de persistir y registra el éxito en memoria. Un error HTTP, de red, de respuesta o de almacenamiento llega a la UI como un fallo controlado. Una cancelación se propaga. Cuando falla la actualización, la UI puede mostrar los datos guardados y un aviso. El detalle remoto tiene respaldo por ID local. [Persistencia](BASE_DE_DATOS.md)
