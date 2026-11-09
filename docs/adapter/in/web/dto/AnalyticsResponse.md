# `AnalyticsResponse`

> Un DTO que sólo sale, nunca entra. Y un caso claro de "duplicación" que no es duplicación.

## El código completo

```java
package com.estebangitpro.portfolio.adapter.in.web.dto;

import java.util.Map;

public record AnalyticsResponse(
        long totalViews,
        long projectViews,
        Map<String, Long> viewsByProject,
        Map<String, Long> viewsByDay,
        Map<String, Long> viewsByCountry,
        Map<String, Long> viewsByRegion,
        Map<String, Long> viewsByDevice,
        Map<String, Long> viewsByOs,
        Map<String, Long> viewsByBrowser,
        Map<String, Long> viewsByTimezone
) {
}
```

---

## 1. ¿Por qué no existe un `AnalyticsRequest` que le haga juego?

Porque este DTO responde a un `GET`:

```java
@GetMapping("/admin/analytics")
public ResponseEntity<AnalyticsResponse> getDashboard() {
    return ResponseEntity.ok(mapper.toResponse(queryUseCase.getDashboard()));
}
```

Un `GET` no lleva cuerpo. No hay nada que deserializar, así que no hace falta un DTO de
entrada.

Regla práctica: **los DTOs no vienen en pares por simetría, vienen por necesidad.** Si no
hay nada que recibir, no inventes una clase para recibir nada.

---

## 2. La pregunta incómoda: esto es idéntico a `AnalyticsSummary`

Mirá el modelo del puerto de entrada, en el núcleo:

```java
// core/application/port/in/AnalyticsSummary.java
public record AnalyticsSummary(
        long totalViews,
        long projectViews,
        Map<String, Long> viewsByProject,
        Map<String, Long> viewsByDay,
        Map<String, Long> viewsByCountry,
        Map<String, Long> viewsByRegion,
        Map<String, Long> viewsByDevice,
        Map<String, Long> viewsByOs,
        Map<String, Long> viewsByBrowser,
        Map<String, Long> viewsByTimezone
) {
}
```

Campo por campo, **exactamente lo mismo**. Y para traducir entre ambos hace falta un mapper:

```java
@Mapper(componentModel = "spring")
public interface AnalyticsMapper {
    AnalyticsResponse toResponse(AnalyticsSummary summary);
}
```

Todo el mundo que ve esto por primera vez piensa lo mismo: *"esto es duplicación tonta,
devolvé el `AnalyticsSummary` y listo"*.

Es una objeción legítima. Y la respuesta no es dogma, es una consecuencia concreta.

### Qué pasaría si devolvieras `AnalyticsSummary` directamente

`AnalyticsSummary` vive en `core.application.port.in`. Es el lenguaje interno de tus casos
de uso. Si lo devolvés por HTTP, **tu contrato público pasa a ser tu modelo interno**.

Entonces, el día que un caso de uso necesite un campo más para uso interno —digamos
`rawEventCount`, útil para un cálculo pero irrelevante para el frontend— ese campo aparece
en el JSON público sin que nadie lo haya decidido.

Y al revés: si el frontend pide renombrar `viewsByOs` a `viewsByOperatingSystem`, tenés que
tocar un record del núcleo por un motivo puramente cosmético del transporte.

**Son dos contratos con dos públicos distintos que hoy, por casualidad, tienen la misma
forma.** El mapper es lo que les permite dejar de tenerla sin que se entere el otro lado.

### El costo real

Sé honesto sobre el precio: son dos records que hay que mantener sincronizados, y MapStruct
mapea por nombre. Si renombrás un campo en uno solo, **MapStruct falla en tiempo de
compilación** — no en runtime. Eso hace el costo tolerable.

Y esa es la pregunta que conviene hacerse siempre: *¿el error se detecta al compilar o en
producción?* Acá, al compilar.

---

## 3. ¿Por qué `Map<String, Long>` y no un tipo propio?

Porque las claves **no se conocen en tiempo de compilación**.

```java
Map<String, Long> viewsByCountry   // "Colombia" -> 12, "Germany" -> 3
Map<String, Long> viewsByDay       // "2026-08-22" -> 9
```

No podés declarar un campo por país: los países que aparezcan dependen de quién visite el
sitio. La forma es dinámica por naturaleza, y el `Map` la representa con honestidad.

Contraste con `viewsByDevice`, que **sí** tiene un conjunto cerrado (`DESKTOP`, `MOBILE`,
`TABLET`). Ahí un `Map<DeviceType, Long>` sería más preciso. Se usa `String` por
consistencia con los demás — una decisión de uniformidad, no de necesidad.

Y así se construyen, en el servicio:

```java
private Map<String, Long> buildDistribution(List<Analytics> list,
                                            Function<Analytics, String> classifier) {
    Map<String, Long> result = new LinkedHashMap<>();
    for (Analytics a : list) {
        String key = classifier.apply(a);
        if (key != null) {            // <-- clave: los null NO entran al mapa
            result.merge(key, 1L, Long::sum);
        }
    }
    return result;
}
```

Dos detalles que vale la pena que registres:

- **`LinkedHashMap`**, no `HashMap`: preserva el orden de inserción. Un `HashMap` te
  devolvería las claves en orden impredecible y el dashboard se vería distinto en cada
  request sin ninguna razón.
- **`if (key != null)`**: las vistas sin dato simplemente no aparecen. Por eso las cuentas
  de los mapas **no suman `totalViews`**.

---

## 4. Un caso real de este proyecto

Esta fue la respuesta de producción hace un rato:

```json
{"totalViews":9,
 "viewsByCountry":{"Local":7},
 "viewsByRegion":{"Hesse":1},
 "viewsByTimezone":{}}
```

Leelo con lo que acabás de aprender:

- `totalViews` = 9, pero `viewsByCountry` suma 7. → 2 vistas tienen `country == null`, y
  por el `if (key != null)` no aparecen.
- `viewsByTimezone` está **vacío**. → *ninguna* vista tiene timezone.
- `country` y `timezone` salen del mismo objeto `location` del proveedor de geolocalización.
  Si alguna consulta hubiera funcionado, habría timezone.

**Conclusión que se deduce sólo del JSON: el proveedor de geo está fallando el 100% de las
veces.** No hizo falta entrar al servidor ni mirar la base de datos.

Eso es lo que te da entender la estructura de tus datos: el JSON deja de ser un volcado y
pasa a ser evidencia.

---

## Para fijar el concepto

1. ¿Por qué no existe `AnalyticsRequest`? por que solo son datos de entrada
2. Si `AnalyticsSummary` y `AnalyticsResponse` son idénticos, ¿qué te protege el mapper? que al momento de hacer cambios el respose se puede cambiar con mas facilidad
   ¿Y cuándo se entera de que rompiste algo? en el momento de compilar 
3. Si `totalViews` es 100 y `viewsByCountry` suma 60, ¿qué pasó con las otras 40?

**Siguiente:** [`ProjectRequest.md`](./ProjectRequest.md)
