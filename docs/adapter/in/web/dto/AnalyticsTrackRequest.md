# `AnalyticsTrackRequest`

> El DTO más simple del proyecto. Cuatro campos, cero anotaciones.
> Empezá por acá.

## El código completo

```java
package com.estebangitpro.portfolio.adapter.in.web.dto;

public record AnalyticsTrackRequest(
        String type,
        String path,
        String projectSlug,
        String sessionId
) {
}
```

Eso es todo. Ocho líneas. Ahora desarmémoslas.

---

## Línea por línea

### `public record AnalyticsTrackRequest(`

`record` en vez de `class`. El compilador te genera:

- un campo `private final` por cada componente,
- un constructor que los recibe todos en orden (el *constructor canónico*),
- un accesor por campo, sin prefijo `get`: `request.type()`,
- `equals`, `hashCode` y `toString` coherentes entre sí.

Jackson usa el constructor canónico para deserializar. Por eso no necesitás constructor
vacío ni setters: **el objeto nace completo o no nace.**

### `String type,`

Acá hay una decisión de diseño que vale la pena discutir, porque no es obviamente correcta.

El dominio ya tiene un enum:

```java
public enum AnalyticsType {
    PAGE_VIEW,
    PROJECT_VIEW
}
```

Y sin embargo el DTO recibe un `String`. La conversión ocurre después, en el controller:

```java
AnalyticsType type = AnalyticsType.valueOf(request.type());
```

**¿Por qué no declarar el campo directamente como `AnalyticsType`?**

Si lo hicieras, Jackson fallaría al deserializar un valor desconocido lanzando
`HttpMessageNotReadableException` — y ese caso *sí* está manejado:

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<Map<String, Object>> handleUnreadableBody(...) {
    return ResponseEntity.badRequest().body(...);   // 400
}
```

Con `String`, en cambio, `valueOf()` lanza `IllegalArgumentException`. Y esa excepción
**no** tiene handler propio, así que cae en el último recurso:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) throws Exception {
    if (ex instanceof ErrorResponse) {
        throw ex;
    }
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)   // 500
            .body(Map.of("error", "Internal server error"));
}
```

`IllegalArgumentException` no implementa `ErrorResponse`, entonces no se relanza: se
aplasta en un **500 Internal Server Error**.

> **Esto es un defecto real, no un ejercicio inventado.**
> Enviar `{"type":"BOGUS"}` a `/api/analytics/track` devuelve 500.
> Un 500 significa "me equivoqué yo, el servidor". Pero acá el que se equivocó fue el
> cliente: mandó un valor que no existe. Eso es un 400.
>
> Es la diferencia entre *"rompí algo"* y *"me pediste algo imposible"*. Confundirlas hace
> que tu monitoreo grite por errores que no son tuyos, y que el cliente no entienda qué
> corregir.

Hay dos formas de arreglarlo, y ninguna es obviamente mejor:

| Opción | Cómo | Costo |
|---|---|---|
| Declarar el campo como `AnalyticsType` | Jackson valida y tira 400 solo | El DTO pasa a depender del enum de dominio |
| Agregar `@ExceptionHandler(IllegalArgumentException.class)` | Un handler más | Atrapa *todas* las `IllegalArgumentException`, incluidas las que sí son bugs tuyos |

La primera es más precisa. La segunda es más amplia — y esa amplitud es justamente su
problema: enmascararía errores internos legítimos como si fueran culpa del cliente.

### `String path,`

La ruta tal como la ve el visitante: `/`, `/projects/mi-portfolio-web`. La manda el
cliente porque **el servidor no puede saberla**: en una SPA la navegación ocurre en el
navegador sin pegarle al servidor. El `POST /api/analytics/track` siempre llega a la misma
URL, sin importar qué página esté mirando el usuario.

### `String projectSlug,`

Sólo tiene valor cuando `type` es `PROJECT_VIEW`. Para un `PAGE_VIEW` viaja como `null`.

Esto expone una limitación honesta de los DTOs planos: **el tipo no puede expresar
"este campo es obligatorio sólo si aquel otro vale X"**. Esa regla existe, pero vive en el
cliente y en la cabeza del que lee el código, no en el compilador.

Cuando esas reglas condicionales se multiplican, es señal de que hacen falta dos endpoints
o dos DTOs distintos en vez de uno con campos condicionales.

### `String sessionId`

Un identificador opaco generado por el cliente, que agrupa las vistas de una misma visita.

No identifica a una persona. No sobrevive a un refresh del navegador. Sólo permite decir
"estas cuatro vistas pasaron juntas".

---

## Lo que NO está en este DTO, y es lo más importante

Fijate lo que el cliente **no** manda:

```java
// AnalyticsController.track()
String referrer  = httpRequest.getHeader("Referer");
String userAgent = httpRequest.getHeader("User-Agent");
String ip        = clientIpResolver.resolve(httpRequest);
```

Referrer, User-Agent e IP los deriva el servidor de las cabeceras HTTP.

**El principio: nunca le pidas al cliente un dato que podés obtener vos.** Si el DTO
tuviera un campo `ip`, cualquiera podría mandar la que se le antoje y envenenar tus
métricas. Al leerlo de la conexión, el cliente no participa de esa decisión.

(Con la salvedad que documentamos en `ClientIpResolver`: detrás de un proxy, la IP también
llega en una cabecera y tampoco es prueba de nada. Por eso ese valor sólo alimenta
analítica y se hashea antes de guardarse — nunca autoriza nada.)

---

## Lo que falta, y por qué importa

Este DTO **no tiene ninguna anotación de validación**. Comparalo con `ProjectRequest`:

```java
public record ProjectRequest(
        @NotBlank String title,     // <-- validado
        ...
```

Consecuencia concreta: `{"type":"PAGE_VIEW","path":null,"sessionId":null}` se acepta sin
protestar y genera un documento con campos nulos.

¿Es grave? Depende del criterio. Para analítica, un registro incompleto es preferible a
perder la visita entera. Pero es una decisión que debería ser **explícita**, y hoy no lo
es: no está escrita en ningún lado. Se infiere de la ausencia de anotaciones, que es la
peor forma de documentar una decisión.

---

## Para fijar el concepto

Antes de seguir, respondete sin mirar:

1. ¿Por qué `type` es `String` y no `AnalyticsType`? ¿Qué código HTTP produce hoy un valor
   inválido, y cuál debería producir?
2. ¿Por qué `ip` no es un campo del DTO?
3. Si mañana agregás `SCROLL_DEPTH` al enum, ¿qué archivos tenés que tocar?

**Siguiente:** [`AnalyticsResponse.md`](./AnalyticsResponse.md)
