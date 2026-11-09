# `ProjectRequest`

> El DTO más rico del proyecto. Acá aparecen la validación, un bug real de producción,
> y una decisión discutible que conviene ver con los ojos abiertos.

## El código completo

```java
package com.estebangitpro.portfolio.adapter.in.web.dto;

import com.estebangitpro.portfolio.core.domain.ProjectLinks;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import com.estebangitpro.portfolio.core.domain.StrategyItem;
import com.estebangitpro.portfolio.core.domain.TechStackItem;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/**
 * Create/update payload. {@code order} and {@code published} are boxed on purpose:
 * as primitives, omitting them made Jackson fail the whole request instead of falling
 * back to a default. The mapper supplies 0 and false when they are absent.
 */
public record ProjectRequest(
        @NotBlank String title,
        String summary,
        String description,
        ProjectStatus status,
        List<String> tags,
        String thumbnail,
        String coverImage,
        String videoUrl,
        List<TechStackItem> techStack,
        List<StrategyItem> strategies,
        List<String> learnings,
        ProjectLinks links,
        Integer order,
        Boolean published,
        LocalDate startDate,
        LocalDate endDate
) {
}
```

---

## 1. `@NotBlank String title` — la cadena de validación completa

La anotación sola **no hace nada**. Este es el error más común al aprender Bean Validation:
poner la anotación y esperar que funcione por magia.

Hacen falta tres piezas, y si falta una el campo pasa sin validar:

### Pieza 1 — la anotación en el DTO

```java
@NotBlank String title,
```

### Pieza 2 — el `@Valid` en el parámetro del controller

```java
// ProjectAdminController.java
public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest requestDTO) {
```

**Sin `@Valid`, Spring ni siquiera ejecuta el validador.** La anotación queda ahí,
decorativa, dando una falsa sensación de seguridad. Es peor que no tenerla.

### Pieza 3 — el handler que traduce el fallo a HTTP

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> detail(error.getField(), error.getDefaultMessage()))
            .toList();

    return ResponseEntity.badRequest().body(errorBody("Validation failed", fieldErrors));
}
```

Sin este handler, la excepción caería en `handleGeneric` y devolvería un 500 — el mismo
problema que documentamos en [`AnalyticsTrackRequest.md`](./AnalyticsTrackRequest.md).

Con las tres piezas, `{"title": "  "}` produce:

```json
{"error":"Validation failed","details":[{"field":"title","message":"must not be blank"}]}
```

### `@NotBlank` vs `@NotNull` vs `@NotEmpty`

Se confunden todo el tiempo. La diferencia importa:

| Anotación | `null` | `""` | `"   "` | `"ok"` |
|---|---|---|---|---|
| `@NotNull` | ✗ | ✓ | ✓ | ✓ |
| `@NotEmpty` | ✗ | ✗ | ✓ | ✓ |
| `@NotBlank` | ✗ | ✗ | ✗ | ✓ |

`@NotBlank` es el correcto para texto que un humano escribe, porque un título de espacios
en blanco es tan inútil como uno vacío. `@NotNull` sobre un `String` es casi siempre
insuficiente.

---

## 2. `Integer order` y `Boolean published` — el bug real

Este es el punto más valioso del archivo. Leé el javadoc otra vez:

> `order` and `published` are boxed on purpose: as primitives, omitting them made Jackson
> fail the whole request instead of falling back to a default.

### Qué pasó

Originalmente los campos eran primitivos: `int order`, `boolean published`. Y el `POST`
devolvía **500** cuando el cliente mandaba `{"order": null}` o hacía explícito el nulo.

La razón: **un primitivo en Java no puede ser `null`.** No existe forma de representar
"ausente" en un `int`. Cuando Jackson encuentra `null` y tiene que meterlo en un `int`,
no tiene salida: falla.

### Por qué la solución es el tipo, no un `if`

Cambiar a `Integer` y `Boolean` (los *wrappers*) hace que el campo **pueda** ser `null`.
Y `null` acá significa algo preciso: *"el cliente no lo mandó"*.

Eso es exactamente lo que un DTO de entrada tiene que poder expresar. Un DTO representa
**lo que llegó**, incluyendo lo que no llegó.

### Dónde se resuelve la ausencia

En el mapper, en el borde exacto entre el transporte y el núcleo:

```java
@Mapping(target = "order", source = "order", defaultValue = "0")
@Mapping(target = "published", source = "published", defaultValue = "false")
ProjectDraft toDraft(ProjectRequest requestDTO);
```

Y del otro lado, `ProjectDraft` vuelve a usar primitivos:

```java
public record ProjectDraft(
        ...
        int order,          // <-- primitivo: en el dominio SIEMPRE hay un valor
        boolean published,
        ...
) {
}
```

### El concepto, que vale para toda tu carrera

```
ProjectRequest          ProjectDraft
 Integer order    ──>    int order
 (permisivo)             (estricto)
 "puede faltar"          "siempre hay uno"
        │                     │
        └──── el mapper ──────┘
           decide el default
```

**El borde es permisivo. El núcleo es estricto. El mapper es donde uno se convierte en el
otro.**

Si el núcleo fuera permisivo (`Integer order`), cada método que lo usa tendría que
preguntarse "¿y si es null?". Esa pregunta se repetiría por todo el dominio. Al resolver la
ausencia **una vez** en el borde, adentro nadie vuelve a pensar en `null`.

Es el mismo principio que aplicamos en `ClientIpResolver`: normalizar en la entrada para
que el resto del sistema trabaje con datos ya garantizados.

---

## 3. Los campos que NO están, y por qué

`ProjectRequest` tiene 16 campos. `ProjectResponse` tiene 20. Faltan estos cuatro:

| Campo | Quién lo produce |
|---|---|
| `id` | MongoDB, al insertar |
| `slug` | `ProjectService`, derivado del `title` |
| `createdAt` | `ProjectService`, con el `Clock` inyectado |
| `updatedAt` | `ProjectService`, en cada modificación |

Ninguno puede venir de afuera. Y no están bloqueados por una validación **sino por
ausencia**: el tipo no los tiene, entonces Jackson no tiene dónde ponerlos.

> Una regla que conviene grabarse: **si un dato no debe poder ser fijado desde afuera, no
> le pongas una validación — sacale el campo.**
>
> Una validación es una promesa que alguien puede olvidar de renovar cuando refactoriza.
> Un campo inexistente no se puede llenar ni por accidente ni a propósito.

---

## 4. Una decisión discutible: importar tipos de dominio

Fijate en los `import`:

```java
import com.estebangitpro.portfolio.core.domain.ProjectLinks;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import com.estebangitpro.portfolio.core.domain.StrategyItem;
import com.estebangitpro.portfolio.core.domain.TechStackItem;
```

El DTO reutiliza objetos de valor del dominio en vez de definir versiones propias.

**¿Viola la arquitectura hexagonal?** No. La dirección de la dependencia es la correcta: el
adaptador conoce al núcleo, no al revés. Ninguna regla de ArchUnit se queja, y con razón.

**¿Tiene un costo?** Sí, y conviene tenerlo presente. Significa que la forma del JSON
público queda atada a la forma de tus objetos de valor de dominio. El día que quieras
agregarle un campo interno a `TechStackItem`, ese campo aparece en el JSON público sin que
lo hayas decidido.

Es exactamente el mismo riesgo que evitamos separando `AnalyticsResponse` de
`AnalyticsSummary` — sólo que acá se aceptó, a cambio de no duplicar cuatro clases.

**Es un trade-off, no un error.** Lo importante no es cuál elegiste, sino que sepas que
elegiste. La mayoría de la gente que escribe esto no sabe que tomó una decisión.

---

## Para fijar el concepto

1. Si borrás el `@Valid` del controller pero dejás el `@NotBlank`, ¿qué pasa con
   `{"title": ""}`? ¿Por qué es peor que no haber puesto nada?
2. ¿Por qué `Integer` en el DTO y `int` en el `ProjectDraft`? ¿Qué se rompe si ponés
   `Integer` en los dos?
3. ¿Por qué `slug` no está en el request, y por qué es mejor que tenerlo con una validación
   que lo rechace?

**Siguiente:** [`ProjectResponse.md`](./ProjectResponse.md)
