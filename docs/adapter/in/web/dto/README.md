# `adapter/in/web/dto`

> Paquete: `com.estebangitpro.portfolio.adapter.in.web.dto`
> Contiene: `ProjectRequest`, `ProjectResponse`, `AnalyticsTrackRequest`, `AnalyticsResponse`

---

## 1. El problema que resuelve este paquete

Una API recibe JSON y devuelve JSON. El dominio, en cambio, está hecho de objetos con
reglas de negocio. Alguien tiene que traducir entre esos dos mundos.

La pregunta que casi nadie se hace: **¿por qué no exponer directamente la entidad de
dominio?** Es decir, ¿por qué no recibir y devolver `Project` y ahorrarse estas clases?

Hay tres razones, y ninguna es estética.

### Razón 1: tienen motivos distintos para cambiar

El JSON cambia cuando el frontend necesita otro nombre de campo, otro formato de fecha,
un campo más para una pantalla nueva. El dominio cambia cuando cambia una regla de
negocio.

Si son la misma clase, renombrar un campo para el frontend obliga a tocar el dominio.
Eso es acoplar dos cosas que evolucionan a ritmos distintos y por razones distintas.

### Razón 2: el dominio deriva datos que el cliente no puede fijar

Comparar los dos records lo deja a la vista:

| | `ProjectRequest` | `ProjectResponse` |
|---|---|---|
| Campos | 16 | 20 |
| Incluye `id` | no | sí |
| Incluye `slug` | no | sí |
| Incluye `createdAt` / `updatedAt` | no | sí |

Esos cuatro campos los **deriva el sistema**, no los provee quien llama. `slug` se calcula
a partir del título; `createdAt` lo estampa el `Clock`; `id` lo asigna Mongo.

Si la API aceptara un `Project` completo, un cliente podría enviar
`{"id": "otro-id", "createdAt": "1999-01-01"}` y pisar datos que no le corresponden. Eso
tiene nombre: *mass assignment*. Acá es imposible por construcción, porque el tipo que
entra sencillamente no tiene esos campos.

**La restricción está en el tipo, no en un `if`.** Un `if` se puede olvidar; un campo que
no existe no se puede llenar.

### Razón 3: el dominio no puede saber que existe HTTP

El proyecto tiene una regla de arquitectura que lo verifica automáticamente:

```java
// HexagonalArchitectureTest.java
static final ArchRule domain_is_free_of_frameworks = noClasses()
        .that().resideInAPackage("..core.domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..", "jakarta..", "com.mongodb..", ...)
        .because("the domain must stay pure: no web, no persistence, no DI container");
```

Si pusieras `@NotBlank` (que es `jakarta.validation`) sobre un campo de `Project`, **ese
test falla y el build se cae**. Las anotaciones de serialización y validación viven acá,
en el DTO, porque son detalles del transporte.

---

## 2. El flujo completo, capa por capa

Este es el recorrido de un `POST /api/admin/projects`. Seguilo con el dedo:

```
 1. Cliente HTTP
        │  {"title":"Mi Portfolio","order":null}
        ▼
 2. Jackson                        deserializa el JSON
        │                          ──> construye ProjectRequest
        ▼
 3. @Valid                         dispara Bean Validation
        │                          ──> ¿title en blanco? -> 400, se corta acá
        ▼
 4. ProjectAdminController         recibe ProjectRequest  [ADAPTADOR DE ENTRADA]
        │
        ▼
 5. ProjectMapper.toDraft()        traduce DTO -> modelo de puerto
        │                          ──> Integer null se vuelve int 0
        ▼
 6. ProjectDraft                   ya NO es un DTO: es lenguaje del núcleo  [PUERTO]
        │
        ▼
 7. ProjectService.createProject()  aplica reglas: slug, timestamps  [APLICACIÓN]
        │
        ▼
 8. Project                        la entidad, con sus invariantes  [DOMINIO]
        │
        ▼
 9. ProjectMapper.toResponse()     traduce dominio -> DTO de salida
        │
        ▼
10. ProjectResponse                20 campos, incluye id/slug/timestamps
        │
        ▼
11. Jackson                        serializa a JSON
        │
        ▼
12. Cliente HTTP                   201 Created
```

**El punto clave está entre el paso 5 y el 6.** Ahí el DTO muere. De la línea 6 hacia
adentro, nadie sabe que existe HTTP, ni JSON, ni Jackson. El núcleo podría ser llamado
desde una CLI, desde un test o desde una cola de mensajes sin cambiar una sola línea.

Y también hay una regla que lo verifica:

```java
static final ArchRule core_does_not_depend_on_adapters = noClasses()
        .that().resideInAPackage("..core..")
        .should().dependOnClassesThat().resideInAnyPackage("..adapter..", "..config..")
        .because("the core is the centre: adapters depend on it, never the other way around");
```

---

## 3. ¿Por qué `record` y no `class`?

Los cuatro DTOs son `record`. Un `record` de Java te da:

- **Inmutabilidad**: los campos son `final`. Nadie puede mutar el DTO a mitad de camino.
- **Constructor canónico**: Jackson lo usa directamente para deserializar.
- **`equals`, `hashCode`, `toString`** generados: gratis, y correctos.
- **Accesores sin `get`**: se escribe `request.title()`, no `request.getTitle()`.

La inmutabilidad importa especialmente en un borde. Un objeto que representa "lo que el
cliente envió" no debería poder cambiar después de haber sido recibido: si cambia, ya no
representa lo que el cliente envió.

Hay además una regla de arquitectura que exige que los modelos de puerto sean interfaces
o records, justamente para forzar esta disciplina:

```java
static final ArchRule ports_are_interfaces_or_models = classes()
        .that().resideInAPackage("..core.application.port..").and().areTopLevelClasses()
        .should().beInterfaces()
        .orShould().beRecords()
        .because("a port is either a contract or the model that contract exchanges");
```

---

## 4. Los principios SOLID, aterrizados en este paquete

No como definición de manual, sino señalando la línea exacta donde aparecen.

### S — Single Responsibility

`ProjectRequest` tiene **una** razón para cambiar: que cambie el contrato HTTP de entrada.
No cambia si cambia Mongo. No cambia si cambia una regla de negocio.

Contraste: si usaras `Project` como DTO, esa clase tendría tres razones para cambiar
(contrato HTTP, persistencia, reglas de negocio). Tres motivos = tres equipos distintos
pisándose el mismo archivo.

### I — Interface Segregation

Es el principio más visible acá, y suele explicarse mal.

Existe `ProjectRequest` **y** `ProjectResponse` en lugar de un único `ProjectDTO` con
`id` nullable. ¿Por qué? Porque quien crea un proyecto no debe siquiera *poder* hablar de
`id`. Un cliente no está obligado a depender de campos que no usa ni debe usar.

Un `ProjectDTO` único te obligaría a documentar "el `id` se ignora en el POST" — y la
documentación no se compila. Dos tipos separados hacen que el compilador lo garantice.

### D — Dependency Inversion

Mirá la dirección de los `import`:

```java
// ProjectRequest.java — el DTO importa del dominio
import com.estebangitpro.portfolio.core.domain.ProjectStatus;
```

El adaptador conoce al núcleo. **El núcleo jamás conoce al adaptador.** Buscá un
`import ...adapter...` dentro de `core/` y no vas a encontrar ninguno — porque el
ArchUnit test lo prohíbe.

### O — Open/Closed

Agregar `AnalyticsResponse.viewsByRegion` (lo hicimos esta semana) no obligó a modificar
`ProjectResponse` ni ningún DTO existente. Se extendió el contrato sin tocar lo que ya
funcionaba.

### L — Liskov Substitution

Es el que menos se manifiesta acá, porque no hay herencia: son records finales. **Eso es
intencional.** Composición sobre herencia; sin jerarquías, no hay subtipo que pueda
romper el contrato del padre.

---

## 5. Qué leer después

Un archivo por clase, en orden de dificultad creciente:

1. [`AnalyticsTrackRequest.md`](./AnalyticsTrackRequest.md) — el más simple: 4 campos.
   Empezá acá.
2. [`AnalyticsResponse.md`](./AnalyticsResponse.md) — sólo salida, y por qué `Map`.
3. [`ProjectRequest.md`](./ProjectRequest.md) — validación y el caso `Integer` vs `int`.
4. [`ProjectResponse.md`](./ProjectResponse.md) — la asimetría de campos derivados.
