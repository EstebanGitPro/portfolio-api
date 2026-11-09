# `ProjectResponse`

> Cierra el círculo. Acá se ve por qué la asimetría entrada/salida no es descuido
> sino diseño.

## El código completo

```java
package com.estebangitpro.portfolio.adapter.in.web.dto;

import com.estebangitpro.portfolio.core.domain.ProjectLinks;
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
import com.estebangitpro.portfolio.core.domain.StrategyItem;
import com.estebangitpro.portfolio.core.domain.TechStackItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        String id,
        String slug,
        String title,
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
        int order,
        boolean published,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
```

---

## 1. Dos diferencias con `ProjectRequest`, y las dos son intencionales

### Diferencia A: cuatro campos de más

```
id, slug, createdAt, updatedAt
```

Son los campos **derivados**. Entran en la respuesta porque el cliente los necesita para
navegar (`slug`), para referenciar (`id`) y para mostrar (`createdAt`). No entran en el
request porque no le corresponde al cliente decidirlos.

Esa asimetría **es** el Interface Segregation Principle, no una ilustración de él. El que
escribe no ve lo que no puede escribir.

### Diferencia B: primitivos en vez de wrappers

```java
// ProjectRequest
Integer order,
Boolean published,

// ProjectResponse
int order,
boolean published,
```

Y esto es coherente, no una inconsistencia:

| | Significado de `null` | Tipo correcto |
|---|---|---|
| **Request** | "el cliente no lo mandó" | `Integer` — necesita representar la ausencia |
| **Response** | *no existe* | `int` — el proyecto ya está creado, siempre tiene valor |

Un proyecto que salió de la base de datos **siempre** tiene un `order`. Si el tipo fuera
`Integer`, estarías afirmando que podría no tenerlo — y obligando a todo consumidor a
chequear un `null` que nunca va a llegar.

> **El tipo es documentación que el compilador verifica.** Un `Integer` donde nunca hay
> `null` es una mentira que hace trabajar de más a todo el que lo lea después.

Volvé al diagrama del request:

```
 Integer order    ──>    int order    ──>    int order
 (request)              (draft)             (response)
 permisivo              estricto            estricto
      │                     │
      └── el mapper ────────┘
        resuelve la ausencia
```

La permisividad existe **sólo** en el punto de entrada. Ni un milímetro más adentro.

---

## 2. El camino de vuelta

Este DTO se construye en el sentido inverso al del request:

```java
// ProjectMapper.java
ProjectResponse toResponse(Project project);
```

Sin `@Mapping`. Cero configuración. ¿Por qué?

Porque MapStruct mapea **por nombre**, y `Project` ya tiene todos los campos con los
nombres que `ProjectResponse` espera. No hay defaults que resolver: la entidad de dominio
está completa por definición.

Compará con el camino de ida, que sí necesitó configuración:

```java
@Mapping(target = "order", source = "order", defaultValue = "0")
@Mapping(target = "published", source = "published", defaultValue = "false")
ProjectDraft toDraft(ProjectRequest requestDTO);
```

**La asimetría en el mapper refleja la asimetría del problema.** Entrar cuesta trabajo
porque hay que validar, completar y normalizar lo que llegó de afuera. Salir es fácil
porque adentro los datos ya están garantizados.

Un detalle que vale oro: MapStruct **genera el código en tiempo de compilación**. Si
renombrás un campo en `Project` y te olvidás de `ProjectResponse`, el build falla. No es
reflexión en runtime, no es un `null` misterioso en producción. Es un error de compilación.

Podés leer el código generado en `target/generated-sources/annotations/.../ProjectMapperImpl.java`.
Hacelo al menos una vez — ver que es un `new ProjectResponse(project.getId(), ...)`
perfectamente aburrido desmitifica la anotación para siempre.

---

## 3. `LocalDate` vs `LocalDateTime`

No es un descuido que convivan:

```java
LocalDate     startDate,   // 2026-01-15          — una fecha
LocalDate     endDate,
LocalDateTime createdAt,   // 2026-01-15T10:30:00 — un instante
LocalDateTime updatedAt,
```

- `startDate` / `endDate` son **hechos del negocio**: un proyecto empezó un día. La hora no
  aporta nada.
- `createdAt` / `updatedAt` son **hechos de auditoría**: importa el instante exacto, porque
  sirven para ordenar y para depurar.

Elegir el tipo más chico que expresa el dato completo es una forma de precisión. Un
`LocalDateTime` en `startDate` te obligaría a inventar una hora que no significa nada, y
alguien terminaría dependiendo de ese `00:00` inventado.

> **Nota sobre lo que vimos en producción:** el dashboard mostró
> `"viewsByDay":{"2026-08-22":9}` cuando la fecha real era el 21. Eso apunta a un desfase
> de reloj o de zona horaria en el servidor. Vale la pena revisarlo, porque afecta la
> ventana de 30 días del dashboard — es la clase de bug que sólo se nota cuando ya
> corrompió datos.

---

## 4. Lo que este DTO expone, visto con ojo crítico

`ProjectResponse` devuelve **todo**, incluido `published`:

```java
boolean published,
```

El endpoint público filtra por ese campo antes de devolver la lista. Pero el DTO no
distingue entre "lo que ve un administrador" y "lo que ve un visitante": es el mismo tipo
para los dos.

Hoy no es un problema. Se volvería uno el día que un proyecto tenga un campo que el
público no deba ver — notas internas, un costo, un cliente. En ese momento la respuesta
correcta **no** es agregar un `if` en el controller, sino un segundo DTO:
`ProjectAdminResponse` y `ProjectPublicResponse`.

Es el mismo razonamiento que separó `ProjectRequest` de `ProjectResponse`: **cuando dos
públicos necesitan ver cosas distintas, necesitan tipos distintos.** El `if` se olvida; el
tipo no.

---

## 5. Recapitulación del paquete

Ya viste los cuatro DTOs. El mapa mental que deberías llevarte:

```
                    BORDE HTTP                        NÚCLEO
                    (permisivo)                      (estricto)

  JSON  ──>  ProjectRequest   ──mapper──>  ProjectDraft  ──>  Project
             Integer order                 int order          entidad con
             @NotBlank title               ya normalizado     invariantes
             sin id/slug
                                                                 │
  JSON  <──  ProjectResponse  <──mapper──────────────────────────┘
             int order
             con id/slug/timestamps
```

Las cuatro ideas, en orden de importancia:

1. **El tipo expresa la regla.** Sacar un campo es más fuerte que validarlo.
2. **El borde es permisivo, el núcleo es estricto**, y el mapper es la frontera exacta.
3. **Dos públicos distintos = dos tipos distintos**, aunque hoy se vean iguales.
4. **Lo que falla al compilar no falla en producción.** MapStruct, primitivos y ArchUnit
   están todos al servicio de esa idea.

---

## Para fijar el concepto

1. ¿Por qué `int order` en la respuesta y `Integer order` en el request? ¿Qué le estarías
   diciendo al consumidor si pusieras `Integer` en la respuesta?
2. ¿Por qué `toResponse` no necesita ninguna anotación `@Mapping` y `toDraft` sí?
3. Si mañana agregás un campo `internalNotes` que sólo el admin debe ver, ¿cuál es la
   solución correcta y por qué no alcanza con un `if` en el controller?

---

**Volver:** [`README.md`](./README.md)
