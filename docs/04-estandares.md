# Estándares del equipo

## Git Flow

```
main         → producción (solo merges desde develop)
  develop    → integración
    feature/*  → nuevas funcionalidades
    fix/*      → correcciones
```

## Convención de commits

Usamos [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: HU-003 crear proyecto endpoint
fix: corregir validación de slug duplicado
docs: agregar HU-004 a la documentación
refactor: extraer lógica de slug a helper
```

## Arquitectura

Arquitectura hexagonal (puertos y adaptadores). Las dependencias apuntan siempre hacia
adentro: los adaptadores dependen del core, nunca al revés.

```
com.estebangitpro.portfolio
├── core/
│   ├── domain/            entidades, value objects y reglas de negocio
│   └── application/
│       ├── port/
│       │   ├── in/        casos de uso + sus modelos de entrada/salida
│       │   └── out/       contratos de infraestructura
│       ├── service/       implementación de los casos de uso
│       └── exception/     errores de aplicación (ej. ProjectNotFoundException)
├── adapter/
│   ├── in/                web (controllers, DTO, mappers), bootstrap
│   └── out/               persistence, geo, parser
└── config/                composition root
```

El dominio es el anillo más interno: debe poder describirse sin saber que existen los
casos de uso. Por eso los puertos viven en `application/`, no en `domain/`.

Reglas obligatorias:

- El dominio no importa Spring, Jakarta, MapStruct ni MongoDB.
- El dominio no depende de `application/`.
- Los puertos hablan tipos de dominio; los DTO no cruzan hacia el core.
- La traducción DTO ↔ dominio ocurre en `adapter/in/web/mapper`.
- Un adaptador de entrada entra **solo** por `port/in`. Tocar `port/out` desde `adapter/in`
  saltea las reglas de negocio del caso de uso.
- Un adaptador de salida nunca llama casos de uso.
- El adaptador de entrada no accede al de salida directamente.

Estas reglas están automatizadas en `HexagonalArchitectureTest` (ArchUnit). Si un cambio
las rompe, el build falla — no es una convención opcional.

## Tests

```bash
./mvnw clean test
```

Requiere Docker corriendo: los tests de integración levantan un MongoDB efímero con
Testcontainers (`MongoIntegrationTest`), así que no dependen de la base local ni del
profile `local`.

`HexagonalArchitectureTest` valida las reglas de arquitectura en cada build.

> **Nota**: el pom fija `api.version=1.44` para Testcontainers. Docker Engine 29+ exige
> `MinAPIVersion 1.40` y el cliente docker-java negocia v1.32, lo que falla con HTTP 400.

## Nombramiento

- **Clases**: PascalCase (`ProjectService`, `ProjectRequest`)
- **DTO web**: sin sufijo `DTO` (`ProjectRequest`, `ProjectResponse`, `AnalyticsResponse`)
- **Servicios**: sin sufijo `Impl` — el contrato es el puerto (`ProjectService`, no `ProjectServiceImpl`)
- **Puertos de entrada**: sufijo `UseCase` (`ProjectQueryUseCase`)
- **Puertos de salida**: nombre del rol (`ProjectRepository`, `GeoResolver`, `DeviceParser`)
- **Adaptadores de salida**: tecnología + rol (`ProjectMongoAdapter`, `IpGuideGeoResolver`)
- **Métodos/variables**: camelCase (`findBySlug`, `getAllProjects`)
- **Archivos**: mismo nombre que la clase
- **Colecciones MongoDB**: plural en inglés (`projects`, `comments`)

## Pull Requests

- Título descriptivo con número de HU si aplica
- Máximo 400 líneas por PR
- Siempre apuntar a `develop`
- Requiere al menos 1 approve
