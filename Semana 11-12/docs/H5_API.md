# H5 — DummyJSON, DTO y mapeo

## Comprobación HTTP real

Verificación realizada desde el equipo el 11/07/2026 08:05:27, mediante solicitudes HTTPS independientes de las pruebas unitarias:

| Endpoint | HTTP | Resultado observado |
|---|---:|---|
| `https://dummyjson.com/products/category/smartphones` | 200 | 16 productos |
| `https://dummyjson.com/products/121` | 200 | ID numérico 121, iPhone 5s, precio 199.99 |

La lista es un objeto con `products` (array), `total`, `skip` y `limit`. El detalle es un objeto de producto, sin envoltorio `products`. Se comprobaron `id`, `title`, `description`, `price`, `category` y `thumbnail`. El JSON tiene otros campos que estos DTO no necesitan y Gson ignora.

Ejemplo de campos seleccionados del detalle real:

```json
{
  "id": 121,
  "title": "iPhone 5s",
  "price": 199.99,
  "category": "smartphones",
  "thumbnail": "https://cdn.dummyjson.com/product-images/smartphones/iphone-5s/thumbnail.webp"
}
```

Las respuestas completas y el registro de estados HTTP están en `.h1-verification/h5/`, excluidos de Git. La fixture `app/src/test/resources/dummyjson/smartphones-response.json` conserva los seis campos necesarios de los 16 productos y los metadatos consultados. Es una instantánea local: las pruebas nunca consultan Internet.

## Configuración

- Retrofit y converter-gson 2.11.0.
- Gson 2.11.0.
- OkHttp 4.12.0: conexión 15 s, lectura/escritura 20 s y llamada completa 30 s.
- Base URL: `https://dummyjson.com/`.
- `ApiService.obtenerSmartphones()` y `obtenerDetalle(id: Int)` son suspend.
- El cliente y el servicio son perezosos; construirlos no inicia HTTP.
- Único permiso añadido: `android.permission.INTERNET`.

## Contrato de datos

Los DTO aceptan campos ausentes o null. Para el contenedor se utiliza `products.orEmpty()` y se pueden filtrar elementos null con `filterNotNull()`.

`ProductoDto.toItem()` conserva el ID y mapea title/nombre, description/descripcion, price/precio, thumbnail/imagen y category/categoria. `favorito` siempre empieza en false, aunque el JSON incluya un campo con ese nombre.

Un ID ausente o no positivo es un dato inválido y no se sustituye por un identificador inventado. `toItemSeguro()` devuelve `Result.failure` en ese caso; el futuro repositorio puede tratarlo sin propagar excepciones a la interfaz. Los textos ausentes se convierten a cadenas vacías; el título vacío a «Producto sin nombre»; un precio ausente, negativo o no finito se normaliza técnicamente a 0.0. Esta normalización no indica un precio real ni una oferta gratuita.

## Alcance y pruebas

Se mantienen intactos `MainActivity`, `ItemRepositoryPrueba`, `ListaViewModel`, `ListaScreen` y los cuatro productos ficticios de H4. H5 no conecta la API al catálogo ni añade Room, Firebase o servicios de autenticación.

Las pruebas nuevas comprueban DTO, mappers y las dos rutas Retrofit. Para probar el cliente se inyecta un interceptor que devuelve JSON local sin ejecutar DNS ni conexiones HTTP. Las pruebas anteriores de H4 también se ejecutan.
